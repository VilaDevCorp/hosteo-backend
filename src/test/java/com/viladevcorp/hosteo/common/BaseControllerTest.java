package com.viladevcorp.hosteo.common;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.service.AuthService;
import com.viladevcorp.hosteo.service.MinioService;
import com.viladevcorp.hosteo.utils.ImageUrlResolver;

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

  /**
   * DTOs resolve image URLs through a static reference to MinioService. Depending on test/context
   * ordering that reference can point at a different instance than the mock injected here, which
   * makes image URL assertions flaky. Bind it explicitly before every test.
   */
  @BeforeEach
  void bindImageUrlResolverToMock() {
    ReflectionTestUtils.setField(ImageUrlResolver.class, "minioService", minioService);
  }
}
