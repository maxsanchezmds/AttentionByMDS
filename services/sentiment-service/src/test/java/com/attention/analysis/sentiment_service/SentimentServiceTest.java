package com.attention.analysis.sentiment_service;

import com.attention.analysis.sentiment_service.dto.MensajeDTO;
import com.attention.analysis.sentiment_service.dto.SentimentRequest;
import com.attention.analysis.sentiment_service.dto.WhatsappMessage;
import com.attention.analysis.sentiment_service.model.Sentiment;
import com.attention.analysis.sentiment_service.model.SvgSentiment;
import com.attention.analysis.sentiment_service.repository.SentimentRepository;
import com.attention.analysis.sentiment_service.repository.SvgSentimentRepository;
import com.attention.analysis.sentiment_service.service.MessageReceptionistClient;
import com.attention.analysis.sentiment_service.service.OpenAIService;
import com.attention.analysis.sentiment_service.service.SentimentService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SentimentServiceTest {

    @Mock
    private MessageReceptionistClient messageReceptionistClient;
    @Mock
    private OpenAIService openAIService;
    @Mock
    private SentimentRepository sentimentRepository;
    @Mock
    private SvgSentimentRepository svgSentimentRepository;

    @InjectMocks
    private SentimentService sentimentService;

    @Test
    void procesarSentimiento_guardaNuevoPromedio() {
        Long conversacionId = 1L;
        MensajeDTO.ConversacionDTO conv = new MensajeDTO.ConversacionDTO();
        conv.setId(conversacionId);
        conv.setIdEmpresa(2L);

        MensajeDTO m1 = new MensajeDTO();
        m1.setMensaje("Hola");
        m1.setFecha(LocalDateTime.now().minusMinutes(5));
        m1.setConversacion(conv);

        MensajeDTO m2 = new MensajeDTO();
        m2.setMensaje("Que tal?");
        m2.setFecha(LocalDateTime.now());
        m2.setConversacion(conv);

        when(messageReceptionistClient.obtenerMensajesConversacion(conversacionId))
                .thenReturn(List.of(m1, m2));
        when(openAIService.analizarSentimiento(anyString())).thenReturn(70);
        when(sentimentRepository.findLastMessagesByConversationId(eq(conversacionId), any(Pageable.class)))
                .thenReturn(List.of(new Sentiment(null, 2L, conversacionId, "msg", 70, LocalDateTime.now())));
        when(svgSentimentRepository.findByIdConversacion(conversacionId)).thenReturn(Optional.empty());

        SentimentRequest request = new SentimentRequest();
        request.setIdConversacion(conversacionId);
        request.setWhatsappMessage(new WhatsappMessage());

        sentimentService.procesarSentimiento(request);

        ArgumentCaptor<Sentiment> sentimentCaptor = ArgumentCaptor.forClass(Sentiment.class);
        verify(sentimentRepository).save(sentimentCaptor.capture());
        Sentiment saved = sentimentCaptor.getValue();
        assertEquals("Que tal?", saved.getContenidoMensaje());
        assertEquals(2L, saved.getIdEmpresa());
        assertEquals(70, saved.getSentimiento());

        ArgumentCaptor<SvgSentiment> svgCaptor = ArgumentCaptor.forClass(SvgSentiment.class);
        verify(svgSentimentRepository).save(svgCaptor.capture());
        SvgSentiment svg = svgCaptor.getValue();
        assertEquals(conversacionId, svg.getIdConversacion());
        assertEquals(70.0, svg.getPromedioSentimiento());
    }

    @Test
    void procesarSentimiento_sinMensajes_lanzaExcepcion() {
        when(messageReceptionistClient.obtenerMensajesConversacion(1L))
                .thenReturn(Collections.emptyList());

        SentimentRequest request = new SentimentRequest();
        request.setIdConversacion(1L);
        request.setWhatsappMessage(new WhatsappMessage());

        assertThrows(IllegalArgumentException.class, () -> sentimentService.procesarSentimiento(request));
    }
}