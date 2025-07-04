package com.attention.analysis.Access_Service.service;

import com.attention.analysis.Access_Service.dto.AccessRequest;
import com.attention.analysis.Access_Service.dto.Empresa;
import com.attention.analysis.Access_Service.dto.TwilioMessage;
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
    private final ClassificationServiceClient classificationServiceClient;
    private final SentimentServiceClient sentimentServiceClient;
    
    public AccessService(AccesoRepository accesoRepository, 
                        EmpresaService empresaService,
                        ClassificationServiceClient classificationServiceClient,
                        SentimentServiceClient sentimentServiceClient) {
        this.accesoRepository = accesoRepository;
        this.empresaService = empresaService;
        this.classificationServiceClient = classificationServiceClient;
        this.sentimentServiceClient = sentimentServiceClient;
    }
    
    @Transactional
    public Acceso procesarAcceso(AccessRequest request) {
        logger.info("Procesando acceso para conversación ID: {}", request.getIdConversacion());
        
        // Validar que el mensaje tenga la estructura correcta
        TwilioMessage whatsappMessage = request.getWhatsappMessage();
        if (whatsappMessage.getTo() == null) {
            logger.error("Estructura de mensaje WhatsApp inválida");
            throw new IllegalArgumentException("Estructura de mensaje WhatsApp inválida");
        }
        
        // Obtener el número de teléfono de la empresa del mensaje
        String displayPhoneNumber = limpiarPrefijo(whatsappMessage.getTo());
        
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
        
        // Verificar si la empresa tiene habilitado el acceso a clasificación
        if (acceso.getClassificationAccess()) {
            logger.info("Empresa {} tiene acceso a clasificación. Solicitando clasificación para conversación {}", 
                       empresa.getId(), request.getIdConversacion());
            classificationServiceClient.solicitarClasificacion(request.getIdConversacion());
        }
        
        // Verificar si la empresa tiene habilitado el acceso a análisis de sentimiento
        if (acceso.getSentimentAccess()) {
            logger.info("Empresa {} tiene acceso a análisis de sentimiento. Solicitando análisis para conversación {}", 
                       empresa.getId(), request.getIdConversacion());
            sentimentServiceClient.solicitarAnalisisSentimiento(request.getIdConversacion(), whatsappMessage);
        }
        
        return acceso;
    }
    
    public Acceso crearAccesos(Acceso acceso) {
        return accesoRepository.save(acceso);
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

    private String limpiarPrefijo(String numero) {
        if (numero == null) return null;
        return numero.replace("whatsapp:", "").replace("+", "");
    }
}