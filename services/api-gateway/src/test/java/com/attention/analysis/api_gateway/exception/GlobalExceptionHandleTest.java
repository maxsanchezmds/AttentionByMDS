package com.attention.analysis.api_gateway.exception;
import com.attention.analysis.api_gateway.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    @Test
    void responseStatusException_populatesAttributes() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockServerHttpRequest httpRequest = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(httpRequest);
        ServerRequest request = ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
        exchange.getAttributes().put("org.springframework.boot.web.reactive.error.DefaultErrorAttributes.ERROR", new ResponseStatusException(HttpStatus.NOT_FOUND, "missing"));

        Map<String, Object> attrs = handler.getErrorAttributes(request, ErrorAttributeOptions.defaults());

        assertEquals(404, attrs.get("status"));
        assertEquals("missing", attrs.get("message"));
        assertEquals("api-gateway", attrs.get("service"));
        assertEquals("/test", attrs.get("path"));
    }
}