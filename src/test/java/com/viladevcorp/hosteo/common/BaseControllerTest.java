package com.viladevcorp.hosteo.common;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.service.AuthService;
import com.viladevcorp.hosteo.service.MinioService;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseControllerTest {

  @Autowired AuthService authService;

  @Autowired UserRepository userRepository;

  @Autowired protected TestSetupHelper testSetupHelper;

  @MockBean protected MinioService minioService;

  @BeforeAll
  public void resetAll() throws Exception {
    testSetupHelper.resetTestBase();
  }
}
