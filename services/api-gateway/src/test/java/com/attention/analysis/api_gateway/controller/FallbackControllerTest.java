package com.attention.analysis.api_gateway.controller;
import com.attention.analysis.api_gateway.controller.FallbackController;
import com.attention.analysis.api_gateway.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.context.annotation.Import;

@WebFluxTest(controllers = FallbackController.class)
@Import(com.attention.analysis.api_gateway.config.SecurityConfig.class)
class FallbackControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private JwtService jwtService;

    @Test
    void authFallback_returnsServiceUnavailable() {
        webTestClient.get().uri("/fallback/auth")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.service").isEqualTo("auth-service");
    }
}