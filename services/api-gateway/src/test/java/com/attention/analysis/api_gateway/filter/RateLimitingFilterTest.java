package com.attention.analysis.api_gateway.filter;
import com.attention.analysis.api_gateway.filter.RateLimitingFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RateLimitingFilterTest {

    private RateLimitingFilter filter;
    private GatewayFilter gatewayFilter;
    private RateLimitingFilter.Config config;

    @BeforeEach
    void setUp() {
        filter = new RateLimitingFilter();
        config = new RateLimitingFilter.Config();
        config.setMaxRequests(1);
        config.setWindowSizeMs(60000);
        gatewayFilter = filter.apply(config);
    }

    @Test
    void secondRequest_isRateLimited() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header("X-Forwarded-For", "1.1.1.1")
                .build();
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        MockServerWebExchange exchange1 = MockServerWebExchange.from(request);
        StepVerifier.create(gatewayFilter.filter(exchange1, chain)).verifyComplete();
        verify(chain).filter(any());

        MockServerWebExchange exchange2 = MockServerWebExchange.from(request);
        StepVerifier.create(gatewayFilter.filter(exchange2, chain)).verifyComplete();
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exchange2.getResponse().getStatusCode());
    }
}