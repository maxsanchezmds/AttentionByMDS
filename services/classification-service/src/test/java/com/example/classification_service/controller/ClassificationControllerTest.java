package com.example.classification_service.controller;

import com.example.classification_service.dto.ClassificationRequest;
import com.example.classification_service.model.Clasificacion;
import com.example.classification_service.repository.ClasificacionRepository;
import com.example.classification_service.service.ClassificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClassificationController.class)
class ClassificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClassificationService classificationService;
    @MockBean
    private ClasificacionRepository clasificacionRepository;

    @Test
    void clasificarConversacion_devuelveOk() throws Exception {
        Clasificacion clas = new Clasificacion();
        clas.setIdConversacion(1L);
        clas.setClasificacion(Clasificacion.TipoClasificacion.URGENTE);
        clas.setFechaClasificacion(LocalDateTime.now());
        when(classificationService.procesarClasificacion(any(ClassificationRequest.class)))
                .thenReturn(clas);
        when(clasificacionRepository.countByIdConversacion(1L)).thenReturn(1L);

        String body = "{\"idConversacion\":1}";
        mockMvc.perform(post("/api/classification/classify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clasificacion.clasificacion").value("URGENTE"))
                .andExpect(jsonPath("$.mensaje").value("Clasificación procesada exitosamente"));
    }

    @Test
    void clasificarConversacion_datosInvalidos_retornaBadRequest() throws Exception {
        when(classificationService.procesarClasificacion(any(ClassificationRequest.class)))
                .thenThrow(new IllegalArgumentException("error"));
        String body = "{\"idConversacion\":2}";
        mockMvc.perform(post("/api/classification/classify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("error"));
    }

    @Test
    void obtenerClasificacion_noExiste_retornaNotFound() throws Exception {
        when(classificationService.obtenerClasificacion(5L))
                .thenThrow(new IllegalArgumentException("no existe"));

        mockMvc.perform(get("/api/classification/conversacion/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("no existe"));
    }

    @Test
    void obtenerHistorial_sinResultados_retornaNotFound() throws Exception {
        when(clasificacionRepository.findAllByIdConversacionOrderByFechaDesc(3L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/classification/conversacion/3/historial"))
                .andExpect(status().isNotFound());
    }
}