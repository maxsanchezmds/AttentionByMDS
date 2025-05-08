package com.attention.analysis.Message_Receptionist.service;

import com.attention.analysis.Message_Receptionist.dto.Empresa;
import com.attention.analysis.Message_Receptionist.dto.WhatsappMessage;
import com.attention.analysis.Message_Receptionist.model.Conversacion;
import com.attention.analysis.Message_Receptionist.model.Mensaje;
import com.attention.analysis.Message_Receptionist.repository.ConversacionRepository;
import com.attention.analysis.Message_Receptionist.repository.MensajeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class MensajeService {

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
        Optional<Empresa> empresaOpt = empresaService.validarNumeroEmpresa(numeroDestinatario);
        
        if (empresaOpt.isEmpty()) {
            return false; // No es un mensaje destinado a una empresa registrada
        }
        
        Empresa empresa = empresaOpt.get();
        
        // Timestamp del mensaje (convertir de epoch seconds a LocalDateTime)
        LocalDateTime fechaMensaje = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(Long.parseLong(mensajeWA.getTimestamp())), 
            ZoneId.systemDefault()
        );
        
        // Buscar o crear conversación
        Conversacion conversacion = obtenerOCrearConversacion(numeroTelefono, empresa, fechaMensaje);
        
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