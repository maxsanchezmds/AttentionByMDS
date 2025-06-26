package com.example.classification_service.service;

import com.example.classification_service.dto.ClassificationRequest;
import com.example.classification_service.dto.MensajeDTO;
import com.example.classification_service.model.Clasificacion;
import com.example.classification_service.repository.ClasificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassificationServiceTest {

    @Mock
    private ClasificacionRepository clasificacionRepository;
    @Mock
    private MessageReceptionistClient messageReceptionistClient;
    @Mock
    private OpenAIService openAIService;
    @InjectMocks
    private ClassificationService classificationService;

    private MensajeDTO buildMessage(String text, boolean ejecutivo) {
        MensajeDTO msg = new MensajeDTO();
        msg.setMensaje(text);
        msg.setEsDeEjecutivo(ejecutivo);
        msg.setFecha(LocalDateTime.now());
        return msg;
    }

    @Test
    void procesarClasificacion_creaNuevaClasificacion() {
        ClassificationRequest request = new ClassificationRequest();
        request.setIdConversacion(1L);
        when(messageReceptionistClient.obtenerMensajesConversacion(1L))
                .thenReturn(List.of(buildMessage("Hola", false)));
        when(openAIService.clasificarConversacion(any()))
                .thenReturn("URGENTE");
        when(clasificacionRepository.findByIdConversacion(1L))
                .thenReturn(Optional.empty());
        Clasificacion saved = new Clasificacion();
        saved.setId(10L);
        saved.setIdConversacion(1L);
        saved.setClasificacion(Clasificacion.TipoClasificacion.URGENTE);
        when(clasificacionRepository.save(any(Clasificacion.class))).thenReturn(saved);

        Clasificacion result = classificationService.procesarClasificacion(request);

        assertThat(result.getClasificacion()).isEqualTo(Clasificacion.TipoClasificacion.URGENTE);
        ArgumentCaptor<Clasificacion> captor = ArgumentCaptor.forClass(Clasificacion.class);
        verify(clasificacionRepository).save(captor.capture());
        assertThat(captor.getValue().getIdConversacion()).isEqualTo(1L);
    }

    @Test
    void procesarClasificacion_clasificacionInvalidaUsaModerada() {
        ClassificationRequest request = new ClassificationRequest();
        request.setIdConversacion(2L);
        when(messageReceptionistClient.obtenerMensajesConversacion(2L))
                .thenReturn(List.of(buildMessage("Hola", true)));
        when(openAIService.clasificarConversacion(any()))
                .thenReturn("DESCONOCIDA");
        when(clasificacionRepository.findByIdConversacion(2L))
                .thenReturn(Optional.empty());
        when(clasificacionRepository.save(any(Clasificacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Clasificacion result = classificationService.procesarClasificacion(request);

        assertThat(result.getClasificacion()).isEqualTo(Clasificacion.TipoClasificacion.MODERADA);
    }

    @Test
    void procesarClasificacion_sinMensajes_lanzaExcepcion() {
        ClassificationRequest request = new ClassificationRequest();
        request.setIdConversacion(3L);
        when(messageReceptionistClient.obtenerMensajesConversacion(3L))
                .thenReturn(List.of());

        assertThatThrownBy(() -> classificationService.procesarClasificacion(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se encontraron mensajes");
    }

    @Test
    void obtenerClasificacion_noEncontrada_lanzaExcepcion() {
        when(clasificacionRepository.findByIdConversacion(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> classificationService.obtenerClasificacion(5L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se encontró clasificación");
    }
}