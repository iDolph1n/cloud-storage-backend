package ru.netology.cloudstorage.it;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CloudStorageIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("cloud")
      .withUsername("cloud")
      .withPassword("cloud");

  static Path tempStorageDir;

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) throws Exception {
    if (tempStorageDir == null) {
      tempStorageDir = Files.createTempDirectory("cloud-storage-it-");
    }
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("app.storage.root-dir", () -> tempStorageDir.toString());
    registry.add("app.bootstrap.users[0].login", () -> "user");
    registry.add("app.bootstrap.users[0].password", () -> "password");
  }

  @Autowired MockMvc mvc;

  @Test
  void full_flow_login_upload_list_download_delete() throws Exception {
    // login
    String loginJson = "{\"login\":\"user\",\"password\":\"password\"}";
    String body = mvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.auth-token").exists())
        .andReturn()
        .getResponse().getContentAsString();

    String token = body.replaceAll(".*\"auth-token\"\\s*:\\s*\"([^\"]+)\".*", "$1");

    // upload
    MockMultipartFile filePart = new MockMultipartFile("file", "hello.txt", "text/plain", "hello".getBytes());
    mvc.perform(multipart("/file")
            .file(filePart)
            .param("filename", "hello.txt")
            .header("auth-token", token))
        .andExpect(status().isOk());

    // list
    mvc.perform(get("/list")
            .param("limit", "10")
            .header("auth-token", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].filename").value("hello.txt"));

    // download
    mvc.perform(get("/file")
            .param("filename", "hello.txt")
            .header("auth-token", token))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("hello.txt")))
        .andExpect(content().bytes("hello".getBytes()));

    // rename
    mvc.perform(put("/file")
            .param("filename", "hello.txt")
            .header("auth-token", token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"filename\":\"renamed.txt\"}"))
        .andExpect(status().isOk());

    // delete
    mvc.perform(delete("/file")
            .param("filename", "renamed.txt")
            .header("auth-token", token))
        .andExpect(status().isOk());
  }
}
