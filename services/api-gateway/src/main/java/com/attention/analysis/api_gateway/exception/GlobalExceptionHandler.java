package com.attention.analysis.api_gateway.exception;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

@Component
public class GlobalExceptionHandler extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);
        
        Throwable error = getError(request);
        
        if (error instanceof org.springframework.web.server.ResponseStatusException) {
            org.springframework.web.server.ResponseStatusException ex = 
                (org.springframework.web.server.ResponseStatusException) error;
            errorAttributes.put("status", ex.getStatusCode().value());
            errorAttributes.put("message", ex.getReason());
        } else {
            // Error genérico
            errorAttributes.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorAttributes.put("error", "Internal Server Error");
            errorAttributes.put("message", "Ha ocurrido un error interno en el servidor");
        }
        
        // Agregar información adicional
        errorAttributes.put("service", "api-gateway");
        errorAttributes.put("path", request.path());
        
        return errorAttributes;
    }
}