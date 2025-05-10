package com.attention.analysis.Message_Receptionist.service;

import com.attention.analysis.Message_Receptionist.dto.Empresa;
import com.attention.analysis.Message_Receptionist.dto.WhatsappMessage;
import com.attention.analysis.Message_Receptionist.model.Conversacion;
import com.attention.analysis.Message_Receptionist.model.Mensaje;
import com.attention.analysis.Message_Receptionist.repository.ConversacionRepository;
import com.attention.analysis.Message_Receptionist.repository.MensajeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class MensajeService {

    private static final Logger logger = LoggerFactory.getLogger(MensajeService.class);

    private final MensajeRepository mensajeRepository;
    private final ConversacionRepository conversacionRepository;
    private final EmpresaService empresaService;

    public MensajeService(MensajeRepository mensajeRepository, 
                         ConversacionRepository conversacionRepository,
                         EmpresaService empresaService) {
        this.mensajeRepository = mensajeRepository;
        this.conversacionRepository = conversacionRepository;
        this.empresaService = empresaService;
    }

    @Transactional
    public boolean procesarMensaje(WhatsappMessage whatsappMessage) {
        try {
            // Obtener el display_phone_number del mensaje
            String displayPhoneNumber = whatsappMessage.getValue().getMetadata().getDisplay_phone_number();
            
            // Validar si el número pertenece a una empresa registrada
            Optional<Empresa> empresaOpt = empresaService.validarNumeroEmpresa(displayPhoneNumber);
            
            if (empresaOpt.isEmpty()) {
                logger.warn("Mensaje recibido de un número no registrado: {}", displayPhoneNumber);
                return false;
            }
            
            Empresa empresa = empresaOpt.get();
            // Continuar con el procesamiento del mensaje...
            
            if (whatsappMessage.getValue() == null || 
                whatsappMessage.getValue().getMessages() == null || 
                whatsappMessage.getValue().getMessages().isEmpty()) {
                return false;
            }

            WhatsappMessage.Message mensajeWA = whatsappMessage.getValue().getMessages().get(0);
            String numeroTelefono = mensajeWA.getFrom();
            String contenidoMensaje = mensajeWA.getText() != null ? mensajeWA.getText().getBody() : "";
            String idMensaje = mensajeWA.getId();
            
            String numeroDestinatario = whatsappMessage.getValue().getMetadata().getPhone_number_id();
            
            // Validar que el mensaje tenga como destino una empresa registrada
            Optional<Empresa> empresaOpt2 = empresaService.validarNumeroEmpresa(numeroDestinatario);
            
            if (empresaOpt2.isEmpty()) {
                logger.warn("Mensaje descartado: no corresponde a una empresa registrada");
                return false; // Importante: retornar false para indicar que el mensaje no se procesó
            }
            
            Empresa empresa2 = empresaOpt2.get();
            
            // Timestamp del mensaje (convertir de epoch seconds a LocalDateTime)
            LocalDateTime fechaMensaje = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(Long.parseLong(mensajeWA.getTimestamp())), 
                ZoneId.systemDefault()
            );
            
            // Buscar o crear conversación
            Conversacion conversacion = obtenerOCrearConversacion(numeroTelefono, empresa2, fechaMensaje);
            
            // Actualizar fecha de actualización de la conversación
            conversacion.setFechaActualizacion(fechaMensaje);
            conversacionRepository.save(conversacion);
            
            // Guardar el mensaje
            Mensaje mensaje = new Mensaje();
            mensaje.setMensaje(contenidoMensaje);
            mensaje.setConversacion(conversacion);
            mensaje.setFecha(fechaMensaje);
            
            mensajeRepository.save(mensaje);
            
            return true;
        } catch (Exception e) {
            logger.error("Error al procesar mensaje: {}", e.getMessage(), e);
            return false;
        }
    }
    
    private Conversacion obtenerOCrearConversacion(String telefonoCliente, Empresa empresa, LocalDateTime fecha) {
        Optional<Conversacion> conversacionOpt = conversacionRepository.findByTelefonoCliente(telefonoCliente);
        
        if (conversacionOpt.isPresent()) {
            return conversacionOpt.get();
        } else {
            Conversacion nuevaConversacion = new Conversacion();
            nuevaConversacion.setTelefonoCliente(telefonoCliente);
            nuevaConversacion.setIdEmpresa(empresa.getId());
            nuevaConversacion.setCorreoEmpresa(empresa.getCorreoEmpresa());
            nuevaConversacion.setFechaCreacion(fecha);
            nuevaConversacion.setFechaActualizacion(fecha);
            
            return conversacionRepository.save(nuevaConversacion);
        }
    }
}