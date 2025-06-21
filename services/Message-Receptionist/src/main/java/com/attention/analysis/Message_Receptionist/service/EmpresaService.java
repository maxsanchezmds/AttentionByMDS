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
    /**
     * Verifica si dos números telefónicos coinciden exactamente después de
     * eliminar los caracteres no numéricos.
     *
     * @param numero1 número telefónico de la empresa ya limpiado
     * @param numero2 número telefónico recibido en el mensaje ya limpiado
     * @return {@code true} si ambos números son no nulos y son idénticos
     */
    private boolean validarCoincidenciaNumeros(String numero1, String numero2) {
        if (numero1 == null || numero2 == null) {
            return false;
        }
        
        boolean coincide = numero1.equals(numero2);
        if (coincide) {
            logger.debug("Número coincidente encontrado: {}", numero1);
        }
        
        return coincide;
    }
}