package com.adnakiwoch.platform.streaming_api.user;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adnakiwoch.platform.streaming_api.dto.internal.UserForUserDetails;
import com.adnakiwoch.platform.streaming_api.dto.req.TestAuthRequest;
import com.adnakiwoch.platform.streaming_api.dto.req.TestRegsiterNewUserReq;
import com.jayway.jsonpath.JsonPath;
import java.util.Map;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class BasicUserTest {
  @Autowired private MockMvc mockMvc;
  @Autowired CacheManager cacheManager;
  @Autowired PasswordEncoder passwordEncoder;

  private ObjectMapper objectMapper = new ObjectMapper();

  @Container @ServiceConnection
  public static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:15-alpine");

  @Test
  public void fullUserRegisterToLogInCycleTest() throws Exception {

    String userName = "Tesfaye Assefa";
    String email = "TesfayeAssefa@Test.com";
    String password = "Tesfaye@Test12#";
    mockMvc
        .perform(
            post("/api/v1/user/new_user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new TestRegsiterNewUserReq(userName, email, password))))
        .andExpect(status().isCreated());

    // tyring to sign in

    org.springframework.test.web.servlet.MvcResult mvcResult =
        mockMvc
            .perform(
                post("/api/v1/user/sign_in")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new TestAuthRequest(email, password))))
            .andExpect(status().isAccepted())
            .andReturn();

    String testing = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.token");

    // tyring to log in

    mockMvc
        .perform(
            get("/api/v1/health/auth_check")
                .header(
                    AUTHORIZATION,
                    "Bearer " // space dude, without it you will get JWT AUTH confused. and also has
                        // to be capital
                        + JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.token")))
        .andExpect(status().isOk());

    // test catching
    // ----- extract catch
    Cache spingCatch = cacheManager.getCache("privilege");
    // ----- extract caffeine
    com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCatch =
        (com.github.benmanes.caffeine.cache.Cache<Object, Object>) spingCatch.getNativeCache();
    Map<Object, Object> nativeCatch = caffeineCatch.asMap();

    // lets do our check
    // remember salting here
    Assert.assertTrue(
        passwordEncoder.matches(
            password, ((UserForUserDetails) nativeCatch.get(email)).password()));
  }
}
