package com.attention.analysis.Message_Receptionist.service;

import com.attention.analysis.Message_Receptionist.dto.Empresa;
import com.attention.analysis.Message_Receptionist.dto.EjecutivoMensajeRequest;
import com.attention.analysis.Message_Receptionist.dto.TwilioMessage;
import com.attention.analysis.Message_Receptionist.dto.WhatsappMessage;
import com.attention.analysis.Message_Receptionist.model.Conversacion;
import com.attention.analysis.Message_Receptionist.model.Mensaje;
import com.attention.analysis.Message_Receptionist.repository.ConversacionRepository;
import com.attention.analysis.Message_Receptionist.repository.MensajeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MensajeServiceTest {

    @Mock
    private MensajeRepository mensajeRepository;
    @Mock
    private ConversacionRepository conversacionRepository;
    @Mock
    private EmpresaService empresaService;
    @Mock
    private AccessServiceClient accessServiceClient;

    @InjectMocks
    private MensajeService mensajeService;

    private TwilioMessage crearMensaje() {
        TwilioMessage whatsappMessage = new TwilioMessage();
        whatsappMessage.setTo("whatsapp:+87654321");
        whatsappMessage.setFrom("whatsapp:+11111");
        whatsappMessage.setBody("Hola");
        whatsappMessage.setMessageSid("id");
        whatsappMessage.setWaId("11111");
        return whatsappMessage;
    }

    @Test
    void procesarMensaje_numeroEmpresaNoRegistrado() {
        when(empresaService.validarNumeroEmpresa(anyString())).thenReturn(Optional.empty());

        boolean result = mensajeService.procesarMensaje(crearMensaje());

        assertFalse(result);
        verifyNoInteractions(mensajeRepository, conversacionRepository, accessServiceClient);
    }

    @Test
    void procesarMensaje_procesaCorrectamente() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        empresa.setCorreoEmpresa("mail@test.com");

        when(empresaService.validarNumeroEmpresa(anyString())).thenReturn(Optional.of(empresa));
        when(conversacionRepository.findByTelefonoClienteAndIdEmpresa("11111", 1L)).thenReturn(Optional.empty());
        when(conversacionRepository.save(any(Conversacion.class))).thenAnswer(inv -> {
            Conversacion c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        TwilioMessage msg = crearMensaje();

        boolean result = mensajeService.procesarMensaje(msg);

        assertTrue(result);
        verify(conversacionRepository, times(1)).save(any(Conversacion.class));
        verify(mensajeRepository).save(any(Mensaje.class));
        verify(accessServiceClient).notificarAcceso(eq(1L), eq(msg));
    }

    @Test
    void procesarMensajeEjecutivo_sinEmpresa() {
        when(empresaService.validarNumeroEmpresa(anyString())).thenReturn(Optional.empty());
        EjecutivoMensajeRequest req = new EjecutivoMensajeRequest();
        req.setMensaje("hola");
        req.setNumeroTelefonoEmpresa("123");
        req.setNumeroTelefonoCliente("456");
        req.setNombreCompletoEjecutivo("Test User");

        boolean result = mensajeService.procesarMensajeEjecutivo(req);

        assertFalse(result);
        verifyNoInteractions(mensajeRepository);
    }

    @Test
    void procesarMensajeEjecutivo_creaConversacionYGuardaMensaje() {
        Empresa empresa = new Empresa();
        empresa.setId(2L);
        empresa.setCorreoEmpresa("mail@corp.com");

        when(empresaService.validarNumeroEmpresa(anyString())).thenReturn(Optional.of(empresa));
        when(conversacionRepository.findByTelefonoClienteAndIdEmpresa("456", 2L)).thenReturn(Optional.empty());
        when(conversacionRepository.save(any(Conversacion.class))).thenAnswer(inv -> {
            Conversacion c = inv.getArgument(0);
            c.setId(5L);
            return c;
        });

        EjecutivoMensajeRequest req = new EjecutivoMensajeRequest();
        req.setMensaje("Resp");
        req.setNumeroTelefonoEmpresa("123");
        req.setNumeroTelefonoCliente("456");
        req.setNombreCompletoEjecutivo("Ejec");

        boolean result = mensajeService.procesarMensajeEjecutivo(req);

        assertTrue(result);
        verify(conversacionRepository, times(1)).save(any(Conversacion.class));
        verify(mensajeRepository).save(argThat(m -> m.isEsDeEjecutivo()));
    }
}