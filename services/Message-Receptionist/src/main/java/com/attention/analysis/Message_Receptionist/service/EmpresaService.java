package com.attention.analysis.Message_Receptionist.service;

import com.attention.analysis.Message_Receptionist.dto.Empresa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class EmpresaService {

    private static final Logger logger = LoggerFactory.getLogger(EmpresaService.class);
    private final WebClient webClient;
    private final String empresasUrl;

    public EmpresaService(WebClient.Builder webClientBuilder, 
                         @Value("${service.empresas.url}") String empresasUrl) {
        this.webClient = webClientBuilder.build();
        this.empresasUrl = empresasUrl;
        logger.info("URL del servicio de empresas configurado: {}", empresasUrl);
    }

    public List<Empresa> obtenerEmpresas() {
        logger.info("Consultando empresas en: {}", empresasUrl);
        try {
            // Agregamos timeout y reintentos para mejorar la resiliencia
            Empresa[] empresas = webClient.get()
                    .uri(empresasUrl)
                    .retrieve()
                    .bodyToMono(Empresa[].class)
                    .timeout(Duration.ofSeconds(5))
                    .retry(3)
                    .onErrorResume(WebClientResponseException.class, e -> {
                        logger.error("Error HTTP al obtener empresas: {} - {}", e.getRawStatusCode(), e.getStatusText());
                        return Mono.empty();
                    })
                    .onErrorResume(e -> {
                        logger.error("Error al obtener empresas: {}", e.getMessage(), e);
                        return Mono.empty();
                    })
                    .block();
            
            if (empresas != null) {
                logger.info("Se encontraron {} empresas", empresas.length);
                return Arrays.asList(empresas);
            } else {
                logger.warn("No se encontraron empresas o la respuesta fue nula");
                return List.of();
            }
        } catch (Exception e) {
            logger.error("Error general al obtener empresas: {}", e.getMessage(), e);
            return List.of();
        }
    }

    public Optional<Empresa> validarNumeroEmpresa(String displayPhoneNumber) {
        logger.info("Validando número de teléfono de empresa: {}", displayPhoneNumber);
        
        // Obtener lista de empresas del servicio de autenticación
        List<Empresa> empresas = obtenerEmpresas();
        
        if (empresas.isEmpty()) {
            logger.warn("No se pudo obtener la lista de empresas.");
            return Optional.empty();
        }
        
        // Limpiar el número que viene en el mensaje
        String numeroLimpio = limpiarNumeroTelefono(displayPhoneNumber);
        
        // Buscar coincidencia con alguna empresa
        return empresas.stream()
            .filter(empresa -> {
                String telefonoEmpresa = limpiarNumeroTelefono(empresa.getTelefonoWhatsapp());
                boolean coincide = validarCoincidenciaNumeros(telefonoEmpresa, numeroLimpio);
                
                if (coincide) {
                    logger.info("Coincidencia encontrada con empresa ID: {}", empresa.getId());
                }
                
                return coincide;
            })
            .findFirst();
    }
    
    private String limpiarNumeroTelefono(String numero) {
        // Eliminar todos los caracteres no numéricos
        return numero != null ? numero.replaceAll("[^0-9]", "") : "";
    }
    
    private boolean validarCoincidenciaNumeros(String numero1, String numero2) {
        // Verificar si hay al menos 8 dígitos consecutivos que coincidan
        if (numero1 == null || numero2 == null || numero1.length() < 8 || numero2.length() < 8) {
            return false;
        }
        
        for (int i = 0; i <= numero1.length() - 8; i++) {
            String secuencia = numero1.substring(i, i + 8);
            if (numero2.contains(secuencia)) {
                logger.debug("Secuencia coincidente encontrada: {}", secuencia);
                return true;
            }
        }
        
        return false;
    }
}