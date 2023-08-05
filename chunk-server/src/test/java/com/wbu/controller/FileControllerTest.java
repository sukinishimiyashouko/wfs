package com.wbu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wbu.DTO.FileChunkDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@AutoConfigureMockMvc
@SpringBootTest
public class FileControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testWrite() throws Exception {
        FileChunkDTO fileChunkDTO = new FileChunkDTO();
        fileChunkDTO.setBucketName("test");
        fileChunkDTO.setFileName("dasfafagd");
        fileChunkDTO.setChunkNo(0);
        fileChunkDTO.setExtension(".jpg");
        fileChunkDTO.setChunkSize(50);
        fileChunkDTO.setBytes("dadbhkajhkah".getBytes());
        String body = objectMapper.writeValueAsString(fileChunkDTO);
        mvc.perform(MockMvcRequestBuilders.post("/file/write")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testRead() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/file/read")
                .param("fileName","dasfafagd")
                .param("extension",".jpg")
                .param("chunkNo","0")
                .param("bucketName","test")).andDo(print()).andExpect(MockMvcResultMatchers.status().isOk());
    }

}
