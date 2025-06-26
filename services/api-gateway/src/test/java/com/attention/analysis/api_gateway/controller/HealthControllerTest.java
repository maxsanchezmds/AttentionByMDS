package com.attention.analysis.api_gateway.controller;
import com.attention.analysis.api_gateway.controller.HealthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.attention.analysis.api_gateway.service.JwtService;
import org.springframework.context.annotation.Import;

@WebFluxTest(controllers = HealthController.class)
@Import(com.attention.analysis.api_gateway.config.SecurityConfig.class)
class HealthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private JwtService jwtService;

    @Test
    void healthEndpoint_returnsUp() {
        webTestClient.get().uri("/api/gateway/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    void infoEndpoint_containsRoutes() {
        webTestClient.get().uri("/api/gateway/info")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.routes['/api/auth/**']").isEqualTo("Servicio de autenticación");
    }
}