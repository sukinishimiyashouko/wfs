package com.wbu.util;

import org.springframework.web.multipart.MultipartFile;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;


/**
 * @auther 11852
 * @create 2023/8/2
 */
public class Md5Util {
    /**
     * md5  校验
     * @param content
     * @return
     */
    public static String getMd5(byte[] content){
        //创建MessageDigest对象
        MessageDigest digest = null;
        try {
            //指定使用算法的名称，返回指定算法的对象
            digest = MessageDigest.getInstance("MD5");
            //将数据传递给MessageDigest对象
            digest.update(content);
            //生成消息摘要
            byte[] bytes = digest.digest();
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                //保持二进制补码的一致性,byte要转化为int的时候，高的24位必然会补1,&0xff可以将高的24位置为0，低8位保持原样
                builder.append(Integer.toString((b&0xff)+0x100,16).substring(1));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 计算失败");
        }

    }

    public static String getMd5(MultipartFile file){
        //获取文件大小
        int size = (int) file.getSize();
        try {
            //输入流做读操作
            InputStream inputStream = file.getInputStream();
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int byteCount;
            //读取文件信息
            while ((byteCount=inputStream.read(buffer))!=-1){
                //每次读取都将信息传递给MessageDigest对象
                digest.update(buffer,0,byteCount);
            }
            //生成信息摘要
            byte[] bytes = digest.digest();
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(Integer.toString((b&0xff)+0x100,16).substring(1));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException("md5 计算失败");
        }
    }

}
