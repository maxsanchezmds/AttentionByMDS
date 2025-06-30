package com.attention.analysis.Message_Receptionist.controller;

import com.attention.analysis.Message_Receptionist.dto.EjecutivoMensajeRequest;
import com.attention.analysis.Message_Receptionist.dto.WhatsappMessage;
import com.attention.analysis.Message_Receptionist.model.Mensaje;
import com.attention.analysis.Message_Receptionist.service.MensajeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WhatsappController.class)
class WhatsappControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MensajeService mensajeService;

    @Test
    void recibirMensaje_ok() throws Exception {
        when(mensajeService.procesarMensaje(any(WhatsappMessage.class))).thenReturn(true);
        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mensaje procesado correctamente")));
    }

    @Test
    void recibirMensaje_falla() throws Exception {
        when(mensajeService.procesarMensaje(any(WhatsappMessage.class))).thenReturn(false);
        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void enviarMensajeEjecutivo_ok() throws Exception {
        when(mensajeService.procesarMensajeEjecutivo(any(EjecutivoMensajeRequest.class))).thenReturn(true);
        String body = "{\"mensaje\":\"hola\",\"numeroTelefonoEmpresa\":\"1\",\"nombreCompletoEjecutivo\":\"Ejec\",\"numeroTelefonoCliente\":\"2\"}";
        mockMvc.perform(post("/webhook/ejecutivo/mensaje")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void enviarMensajeEjecutivo_falla() throws Exception {
        when(mensajeService.procesarMensajeEjecutivo(any(EjecutivoMensajeRequest.class))).thenReturn(false);
        String body = "{\"mensaje\":\"hola\",\"numeroTelefonoEmpresa\":\"1\",\"nombreCompletoEjecutivo\":\"Ejec\",\"numeroTelefonoCliente\":\"2\"}";
        mockMvc.perform(post("/webhook/ejecutivo/mensaje")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerMensajesConversacion_ok() throws Exception {
        Mensaje msg = new Mensaje(1L, "hola", null, LocalDateTime.now(), false, null);
        when(mensajeService.obtenerMensajesPorConversacion(1L)).thenReturn(List.of(msg));
        mockMvc.perform(get("/webhook/conversacion/1/mensajes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.*[0].mensaje").value("hola"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerMensajesConversacion_noEncontrado() throws Exception {
        when(mensajeService.obtenerMensajesPorConversacion(1L)).thenReturn(List.of());
        mockMvc.perform(get("/webhook/conversacion/1/mensajes"))
                .andExpect(status().isNotFound());
    }
}