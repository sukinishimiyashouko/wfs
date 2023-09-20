package com.wbu.services.impl;

import com.wbu.BO.ServerInfo;
import com.wbu.DO.FileChunkMeta;
import com.wbu.DO.MetaFile;
import com.wbu.DTO.CompleteChunkFileDTO;
import com.wbu.DTO.FileMeta;
import com.wbu.VO.BucketVO;
import com.wbu.VO.FileVO;
import com.wbu.config.MetaConfig;
import com.wbu.error.EnumMetaException;
import com.wbu.errors.BusinessException;
import com.wbu.services.DiscoveryService;
import com.wbu.services.MetaService;
import com.wbu.util.RequestUtil;
import com.wbu.utils.FileNameGenerator;
import com.wbu.utils.ServerSelector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@Service
@Slf4j
public class MetaServiceImpl implements MetaService {
    private final FileNameGenerator fileNameGenerator;
    private final HttpServletRequest request;
    private final MetaConfig metaConfig;
    private final MongoTemplate mongoTemplate;
    private final DiscoveryService discoveryService;
    private final ServerSelector serverSelector;
    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();

    public MetaServiceImpl(FileNameGenerator fileNameGenerator,
                           HttpServletRequest request,
                           MetaConfig metaConfig,
                           MongoTemplate mongoTemplate,
                           DiscoveryService discoveryService,
                           ServerSelector serverSelector) {
        this.fileNameGenerator = fileNameGenerator;
        this.request = request;
        this.metaConfig = metaConfig;
        this.mongoTemplate = mongoTemplate;
        this.discoveryService = discoveryService;
        this.serverSelector = serverSelector;
    }

    @Override
    public MetaFile generate(FileMeta fileMeta) {
        //获取文件大小
        Long fileSize = fileMeta.getFileSize();
        //获取文件后缀名
        String extension = fileMeta.getExtension();
        //获取文件存储桶的名字
        String bucketName = fileMeta.getBucketName();
        //获取客户端IP
        String clientIpAddr = RequestUtil.getIpAddr(request);
        //获取分片大小
        Integer chunkSize = metaConfig.getChunkSize();
        //重新定义文件名
        String fileName = fileNameGenerator.generate(fileMeta, clientIpAddr);
        //用fileName当做MongoDB中文档ID确保唯一性
        MetaFile metaFile = mongoTemplate.findById(fileName, MetaFile.class);

        if (Objects.nonNull(metaFile)){
            return metaFile;
        }

        metaFile = new MetaFile();
        //获取分片个数
        int totalChunk = (int) Math.ceil(fileSize * 1.0 / chunkSize);

        List<FileChunkMeta> chunks = createChunks(fileSize, extension, bucketName, chunkSize, fileName, totalChunk);

        metaFile.setFileName(fileName)
                .setExtension(extension)
                .setFileSize(fileSize)
                .setBucketName(bucketName)
                .setTotalChunk(totalChunk)
                .setCompleted(false)
                .setChunks(chunks);
        mongoTemplate.insert(metaFile);
        return metaFile;
    }

    /**
     * 创建文件分片元数据(创建分片s)
     * @param fileSize 文件大小
     * @param extension 文件后缀名
     * @param bucketName 文件存储桶
     * @param chunkSize 分片大小
     * @param fileName 文件名
     * @param totalChunk 总分片数量
     * @return 分片元数据列表
     */
    private List<FileChunkMeta> createChunks(Long fileSize
            , String extension
            , String bucketName
            , Integer chunkSize
            , String fileName
            , int totalChunk) {

        List<FileChunkMeta> chunks = new ArrayList<>();

        List<ServerInfo> aliveServers = discoveryService.aliveServers();
        if (aliveServers.size()==0){
            throw new BusinessException(EnumMetaException.NOT_ENOUGH_CHUNK_SERVER);
        }
        //初始分片大小
        long start = 0;
        //根据总分片名进行遍历
        for (int i = 0; i < totalChunk; i++) {
            //记住当前分片大小
            long currentChunkSize = chunkSize;
            if (fileSize <(long)(i+1)* chunkSize){
                currentChunkSize = fileSize - (long) i * chunkSize;
            }
            //选择机器
            // TODO: 2023/8/3 可更新选择策略
            List<ServerInfo> selectServers = serverSelector.select(aliveServers, metaConfig.getChunkInstanceCount());
            //每一台服务器都存储相同的文件分片，也就意味着可以为文件做分片 后续在下载的时候需要分组，避免一次下载多个相同的文件
            for (ServerInfo selectServer : selectServers) {
                String address = selectServer.getHost()+":"+selectServer.getPort();

                FileChunkMeta fileChunkMeta = new FileChunkMeta();
                fileChunkMeta.setFileName(fileName)
                        .setChunkNo(i)
                        .setBucketName(bucketName)
                        .setChunkStart(start)
                        .setChunkSize((int) currentChunkSize)
                        .setExtension(extension)
                        .setAddress(address)
                        .setSchema(selectServer.getSchema())
                        .setWeight(metaConfig.getChunkInstanceMaxWeight());

                chunks.add(fileChunkMeta);
            }
            start+=currentChunkSize;
        }
        return chunks;
    }

    /**
     * 分片上传完成
     * @param
     */
    @Override
    public synchronized void completeChunk(CompleteChunkFileDTO completeChunkFileDTO) {
        String fileName = completeChunkFileDTO.getFileName();
        /**
         * 上传成功之后从MongoDB中查找一下
         */
        MetaFile metaFile = mongoTemplate.findById(fileName, MetaFile.class);
//        AtomicReference<MetaFile> metaFileAtomicReference = new AtomicReference<>();
        if(Objects.isNull(metaFile)){
            throw new BusinessException(EnumMetaException.META_FILE_NOT_FOUND);
        }
        AtomicBoolean completed = new AtomicBoolean(true);
        //如果找到了则进行遍历
        metaFile.getChunks().forEach(c->{
            if (c.getChunkNo().equals(completeChunkFileDTO.getChunkNo())
                    &&c.getAddress().equals(completeChunkFileDTO.getAddress())
                    &&c.getSchema().equals(completeChunkFileDTO.getSchema())){
                c.setChunkMd5(completeChunkFileDTO.getMd5());
                c.setIsCompleted(true);
            }
            if (!c.getIsCompleted()){
                completed.set(false);
            }
        });
        mongoTemplate.save(metaFile);
    }

    @Override
    public MetaFile meta(String bucketName, String fileName) {
        //根据文件名进行查找 返回MetaFile类型
        MetaFile metaFile = mongoTemplate.findById(fileName, MetaFile.class);
        if (Objects.isNull(metaFile)){
            throw new BusinessException(EnumMetaException.META_FILE_NOT_FOUND);
        }
        //获取活着的服务器s
        List<ServerInfo> serverInfos = discoveryService.aliveServers();
        //用服务Ip作为匹配条件
        Set<String> addressSet = serverInfos.stream()
                .map(serverInfo -> serverInfo.getHost() + ":" + serverInfo.getPort())
                .collect(Collectors.toSet());

        List<FileChunkMeta> chunks = metaFile.getChunks();
        chunks.forEach(c->{
            String address = c.getAddress();
            if (!addressSet.contains(address)){
                c.setWeight(c.getWeight()>0 ? c.getWeight()-1:0);
            }else {
                c.setWeight(metaConfig.getChunkInstanceMaxWeight());
            }

            if (!c.getIsCompleted()){
                throw new BusinessException(EnumMetaException.CHUNK_FILE_NOT_UPLOADED);
            }
        });
        mongoTemplate.save(metaFile);

        /**
         * 筛选
         */
        Collection<List<FileChunkMeta>> values = chunks.stream()
                //根据分片序号进行分组
                .collect(Collectors.groupingBy(FileChunkMeta::getChunkNo))
                //取值
                .values();
        List<FileChunkMeta> chunkMetas = values
                .stream()
                //并行筛选
                .parallel()
                .map(fileChunkMetas -> fileChunkMetas.stream()
                        .peek(c -> {
                            String address = c.getAddress();
                            if (!addressSet.contains(address)) {
                                c.setWeight(c.getWeight() > 0 ? c.getWeight() - 1 : 0);
                            }
                        })
                        .max(Comparator.comparing(FileChunkMeta::getWeight))
                        .orElse(new FileChunkMeta())
                )
                .filter(e -> e.getWeight() > 0)
                .toList();

        //判断取出的分片数量是否小于总数量
        if (chunkMetas.size()<metaFile.getTotalChunk()){
            //如果存在的话找出缺少的分片s
            Set<Integer> chunkNoSet = chunkMetas.stream().map(FileChunkMeta::getChunkNo).collect(Collectors.toSet());
            //找出缺少的分片
            ArrayList<Integer> lossChunkNo = new ArrayList<>();
            for (int i = 0; i < metaFile.getTotalChunk(); i++) {
                if (!chunkNoSet.contains(i)){
                    lossChunkNo.add(i);
                }
            }
            log.info("文件{}存在分片不可用现象:{}",fileName,lossChunkNo);
            throw new BusinessException(EnumMetaException.NO_CHUNK_META_AVAILABLE);
        }
        metaFile.setChunks(chunkMetas);
        return metaFile;
    }

    @Override
    public List<FileChunkMeta> chunkInfo(String bucketName, String fileName, Integer chunkNo) {
        if (fileName.contains(".")) {
            fileName = fileName.split("\\.")[0];
        }
        MetaFile metaFile = mongoTemplate.findById(fileName, MetaFile.class);
        if (Objects.isNull(metaFile)) {
            throw new BusinessException(EnumMetaException.META_FILE_NOT_FOUND);
        }
        return metaFile.getChunks()
                .stream()
                .filter(chunk -> chunk.getChunkNo().equals(chunkNo))
                .collect(Collectors.toList());
    }

    @Override
    public List<BucketVO> files() {
        List<MetaFile> allFilesMeta = mongoTemplate.findAll(MetaFile.class);

        Map<String, List<MetaFile>> bucketMap = allFilesMeta.stream()
                .collect(Collectors.groupingBy(MetaFile::getBucketName));

        return bucketMap.entrySet().stream().map(entry -> {
            List<FileVO> fileVOList = entry.getValue()
                    .stream()
                    .filter(MetaFile::getCompleted)
                    .map(metaFile -> new FileVO()
                            .setFileName(metaFile.getFileName())
                            .setExtension(metaFile.getExtension())
                            .setBucketName(metaFile.getBucketName())
                            .setFileSize(metaFile.getFileSize())
                    ).toList();
            return new BucketVO().setBucketName(entry.getKey())
                    .setFiles(fileVOList);
        }).toList();
    }

    @Override
    public void delete(String bucketName, String fileName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("fileName").is(fileName));
        mongoTemplate.remove(query, MetaFile.class);
    }
}
