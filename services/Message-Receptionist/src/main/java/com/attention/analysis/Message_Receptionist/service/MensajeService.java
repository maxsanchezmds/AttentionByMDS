package com.attention.analysis.Message_Receptionist.service;

import com.attention.analysis.Message_Receptionist.dto.Empresa;
import com.attention.analysis.Message_Receptionist.dto.WhatsappMessage;
import com.attention.analysis.Message_Receptionist.dto.EjecutivoMensajeRequest;
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
import java.util.List;

@Service
public class MensajeService {

    private static final Logger logger = LoggerFactory.getLogger(MensajeService.class);

    private final MensajeRepository mensajeRepository;
    private final ConversacionRepository conversacionRepository;
    private final EmpresaService empresaService;
    private final AccessServiceClient accessServiceClient;

    public MensajeService(MensajeRepository mensajeRepository, 
                         ConversacionRepository conversacionRepository,
                         EmpresaService empresaService,
                         AccessServiceClient accessServiceClient) {
        this.mensajeRepository = mensajeRepository;
        this.conversacionRepository = conversacionRepository;
        this.empresaService = empresaService;
        this.accessServiceClient = accessServiceClient;
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
            mensaje.setEsDeEjecutivo(false); // Mensaje de cliente
            
            mensajeRepository.save(mensaje);
            
            // Notificar al Access Service
            logger.info("Notificando a Access Service sobre la conversación ID: {}", conversacion.getId());
            accessServiceClient.notificarAcceso(conversacion.getId(), whatsappMessage);
            
            return true;
        } catch (Exception e) {
            logger.error("Error al procesar mensaje: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Transactional
    public boolean procesarMensajeEjecutivo(EjecutivoMensajeRequest request) {
        try {
            logger.info("Procesando mensaje de ejecutivo para cliente: {}", request.getNumeroTelefonoCliente());
            
            // Validar que el número de la empresa esté registrado
            Optional<Empresa> empresaOpt = empresaService.validarNumeroEmpresa(request.getNumeroTelefonoEmpresa());
            
            if (empresaOpt.isEmpty()) {
                logger.warn("Número de empresa no registrado: {}", request.getNumeroTelefonoEmpresa());
                return false;
            }
            
            Empresa empresa = empresaOpt.get();
            LocalDateTime fechaActual = LocalDateTime.now();
            
            // Buscar la conversación existente con el cliente
            Optional<Conversacion> conversacionOpt = conversacionRepository.findByTelefonoCliente(request.getNumeroTelefonoCliente());
            
            if (conversacionOpt.isEmpty()) {
                logger.warn("No existe conversación previa con el cliente: {}", request.getNumeroTelefonoCliente());
                // Crear nueva conversación si no existe
                Conversacion nuevaConversacion = new Conversacion();
                nuevaConversacion.setTelefonoCliente(request.getNumeroTelefonoCliente());
                nuevaConversacion.setIdEmpresa(empresa.getId());
                nuevaConversacion.setCorreoEmpresa(empresa.getCorreoEmpresa());
                nuevaConversacion.setFechaCreacion(fechaActual);
                nuevaConversacion.setFechaActualizacion(fechaActual);
                
                conversacionOpt = Optional.of(conversacionRepository.save(nuevaConversacion));
            }
            
            Conversacion conversacion = conversacionOpt.get();
            
            // Actualizar fecha de actualización de la conversación
            conversacion.setFechaActualizacion(fechaActual);
            conversacionRepository.save(conversacion);
            
            // Crear y guardar el mensaje del ejecutivo
            Mensaje mensaje = new Mensaje();
            mensaje.setMensaje(request.getMensaje());
            mensaje.setConversacion(conversacion);
            mensaje.setFecha(fechaActual);
            mensaje.setEsDeEjecutivo(true); // Mensaje de ejecutivo
            mensaje.setNombreEjecutivo(request.getNombreCompletoEjecutivo());
            
            mensajeRepository.save(mensaje);
            
            logger.info("Mensaje del ejecutivo {} guardado exitosamente", request.getNombreCompletoEjecutivo());
            
            return true;
        } catch (Exception e) {
            logger.error("Error al procesar mensaje del ejecutivo: {}", e.getMessage(), e);
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

    public List<Mensaje> obtenerMensajesPorConversacion(Long idConversacion) {
        return mensajeRepository.findByConversacionIdOrderByFechaAsc(idConversacion);
    }
}