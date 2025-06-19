package com.attention.analysis.sentiment_service.controller;

import com.attention.analysis.sentiment_service.dto.SentimentRequest;
import com.attention.analysis.sentiment_service.dto.WhatsappMessage;
import com.attention.analysis.sentiment_service.model.Sentiment;
import com.attention.analysis.sentiment_service.model.SvgSentiment;
import com.attention.analysis.sentiment_service.repository.SentimentRepository;
import com.attention.analysis.sentiment_service.repository.SvgSentimentRepository;
import com.attention.analysis.sentiment_service.service.SentimentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SentimentController.class)
class SentimentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SentimentService sentimentService;
    @MockBean
    private SentimentRepository sentimentRepository;
    @MockBean
    private SvgSentimentRepository svgSentimentRepository;

    @Test
    void healthEndpoint_returnsUp() throws Exception {
        mockMvc.perform(get("/api/sentiment/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("sentiment-service"));
    }

    @Test
    void procesarEndpoint_devuelveOk() throws Exception {
        doNothing().when(sentimentService).procesarSentimiento(any(SentimentRequest.class));
        String body = "{\"idConversacion\":1,\"whatsappMessage\":{}}";
        mockMvc.perform(post("/api/sentiment/procesar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Análisis de sentimiento procesado exitosamente"))
                .andExpect(jsonPath("$.idConversacion").value(1L));
    }

    @Test
    void obtenerAnalisis_porPromedio() throws Exception {
        SvgSentiment svg = new SvgSentiment(1L, 80.0, LocalDateTime.now(), 2L);
        when(svgSentimentRepository.findByIdConversacion(1L)).thenReturn(Optional.of(svg));

        mockMvc.perform(get("/api/sentiment/analisis/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("promedio"))
                .andExpect(jsonPath("$.data.promedioSentimiento").value(80.0));
    }

    @Test
    void obtenerAnalisis_porIndividual() throws Exception {
        when(svgSentimentRepository.findByIdConversacion(1L)).thenReturn(Optional.empty());
        Sentiment sentiment = new Sentiment(1L, 2L, 1L, "msg", 70, LocalDateTime.now());
        when(sentimentRepository.findLastMessagesByConversationId(1L, PageRequest.of(0, 10)))
                .thenReturn(List.of(sentiment));

        mockMvc.perform(get("/api/sentiment/analisis/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("individual"))
                .andExpect(jsonPath("$.data[0].sentimiento").value(70));
    }
}