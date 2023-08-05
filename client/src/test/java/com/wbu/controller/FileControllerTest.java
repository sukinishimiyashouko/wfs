package com.wbu.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.io.FileInputStream;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


/**
 * @auther 11852
 * @create 2023/8/3
 */
@SpringBootTest
@AutoConfigureMockMvc
public class FileControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void testUploadFile() throws Exception {
        File file = new File("C:\\Users\\11852\\Pictures\\Camera Roll\\AF4985215DF77C14A6AC73680234D44D.jpg");
        FileInputStream fileInputStream = new FileInputStream(file);
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file",fileInputStream);
        mvc.perform(
                    MockMvcRequestBuilders.multipart("/upload")
                .file(mockMultipartFile)
                .param("bucketName","test"))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}
