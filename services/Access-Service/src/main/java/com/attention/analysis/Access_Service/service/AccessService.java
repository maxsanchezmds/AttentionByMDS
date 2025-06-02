package com.attention.analysis.Access_Service.service;

import com.attention.analysis.Access_Service.dto.AccessRequest;
import com.attention.analysis.Access_Service.dto.Empresa;
import com.attention.analysis.Access_Service.dto.WhatsappMessage;
import com.attention.analysis.Access_Service.model.Acceso;
import com.attention.analysis.Access_Service.repository.AccesoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AccessService {
    
    private static final Logger logger = LoggerFactory.getLogger(AccessService.class);
    
    private final AccesoRepository accesoRepository;
    private final EmpresaService empresaService;
    
    public AccessService(AccesoRepository accesoRepository, EmpresaService empresaService) {
        this.accesoRepository = accesoRepository;
        this.empresaService = empresaService;
    }
    
    @Transactional
    public Acceso procesarAcceso(AccessRequest request) {
        logger.info("Procesando acceso para conversación ID: {}", request.getIdConversacion());
        
        // Validar que el mensaje tenga la estructura correcta
        WhatsappMessage whatsappMessage = request.getWhatsappMessage();
        if (whatsappMessage.getValue() == null || 
            whatsappMessage.getValue().getMetadata() == null) {
            logger.error("Estructura de mensaje WhatsApp inválida");
            throw new IllegalArgumentException("Estructura de mensaje WhatsApp inválida");
        }
        
        // Obtener el número de teléfono de la empresa del mensaje
        String displayPhoneNumber = whatsappMessage.getValue().getMetadata().getDisplay_phone_number();
        
        // Validar que el número pertenezca a una empresa registrada
        Optional<Empresa> empresaOpt = empresaService.validarNumeroEmpresa(displayPhoneNumber);
        
        if (empresaOpt.isEmpty()) {
            logger.error("Número de empresa no registrado: {}", displayPhoneNumber);
            throw new IllegalArgumentException("Número de empresa no registrado");
        }
        
        Empresa empresa = empresaOpt.get();
        logger.info("Empresa identificada: ID={}, Correo={}", empresa.getId(), empresa.getCorreoEmpresa());
        
        // Buscar o crear el registro de accesos para esta empresa
        Optional<Acceso> accesoExistente = accesoRepository.findByIdEmpresa(empresa.getId());
        
        Acceso acceso;
        if (accesoExistente.isPresent()) {
            acceso = accesoExistente.get();
            logger.info("Accesos existentes encontrados para empresa ID: {}", empresa.getId());
        } else {
            // Crear nuevo registro de accesos con valores por defecto (todos en false)
            acceso = new Acceso();
            acceso.setIdEmpresa(empresa.getId());
            acceso.setSentimentAccess(false);
            acceso.setClassificationAccess(false);
            acceso.setAttentionQualityAccess(false);
            acceso.setFeedbackAccess(false);
            acceso.setLearnAccess(false);
            
            acceso = accesoRepository.save(acceso);
            logger.info("Nuevo registro de accesos creado para empresa ID: {}", empresa.getId());
        }
        
        return acceso;
    }
    
    public Optional<Acceso> obtenerAccesosPorEmpresa(Long idEmpresa) {
        return accesoRepository.findByIdEmpresa(idEmpresa);
    }
    
    @Transactional
    public Acceso actualizarAccesos(Long idEmpresa, Acceso accesosActualizados) {
        Optional<Acceso> accesoOpt = accesoRepository.findByIdEmpresa(idEmpresa);
        
        if (accesoOpt.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron accesos para la empresa ID: " + idEmpresa);
        }
        
        Acceso acceso = accesoOpt.get();
        
        // Actualizar los accesos
        acceso.setSentimentAccess(accesosActualizados.getSentimentAccess());
        acceso.setClassificationAccess(accesosActualizados.getClassificationAccess());
        acceso.setAttentionQualityAccess(accesosActualizados.getAttentionQualityAccess());
        acceso.setFeedbackAccess(accesosActualizados.getFeedbackAccess());
        acceso.setLearnAccess(accesosActualizados.getLearnAccess());
        
        return accesoRepository.save(acceso);
    }
}