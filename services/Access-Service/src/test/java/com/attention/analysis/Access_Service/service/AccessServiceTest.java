package com.attention.analysis.Access_Service.service;

import com.attention.analysis.Access_Service.dto.AccessRequest;
import com.attention.analysis.Access_Service.dto.Empresa;
import com.attention.analysis.Access_Service.dto.WhatsappMessage;
import com.attention.analysis.Access_Service.model.Acceso;
import com.attention.analysis.Access_Service.repository.AccesoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessServiceTest {

    @Mock
    private AccesoRepository accesoRepository;
    @Mock
    private EmpresaService empresaService;
    @Mock
    private ClassificationServiceClient classificationClient;
    @Mock
    private SentimentServiceClient sentimentClient;

    @InjectMocks
    private AccessService accessService;

    private AccessRequest buildRequest() {
        WhatsappMessage.Metadata metadata = new WhatsappMessage.Metadata();
        metadata.setDisplay_phone_number("12345");
        WhatsappMessage.Value value = new WhatsappMessage.Value();
        value.setMetadata(metadata);
        WhatsappMessage whatsappMessage = new WhatsappMessage();
        whatsappMessage.setValue(value);

        AccessRequest req = new AccessRequest();
        req.setIdConversacion(1L);
        req.setWhatsappMessage(whatsappMessage);
        return req;
    }

    @Test
    void procesarAcceso_creaNuevoAcceso() {
        AccessRequest request = buildRequest();
        Empresa empresa = new Empresa();
        empresa.setId(2L);
        when(empresaService.validarNumeroEmpresa("12345")).thenReturn(Optional.of(empresa));
        when(accesoRepository.findByIdEmpresa(2L)).thenReturn(Optional.empty());
        when(accesoRepository.save(any(Acceso.class))).thenAnswer(inv -> inv.getArgument(0));

        Acceso result = accessService.procesarAcceso(request);

        verify(accesoRepository).save(any(Acceso.class));
        verifyNoInteractions(classificationClient, sentimentClient);
        assertEquals(2L, result.getIdEmpresa());
        assertFalse(result.getClassificationAccess());
        assertFalse(result.getSentimentAccess());
    }

    @Test
    void procesarAcceso_accesosExistentes_disparaServicios() {
        AccessRequest request = buildRequest();
        Empresa empresa = new Empresa();
        empresa.setId(5L);
        Acceso acceso = new Acceso(1L, 5L, true, true, false, false, false);
        when(empresaService.validarNumeroEmpresa("12345")).thenReturn(Optional.of(empresa));
        when(accesoRepository.findByIdEmpresa(5L)).thenReturn(Optional.of(acceso));

        Acceso result = accessService.procesarAcceso(request);

        verify(classificationClient).solicitarClasificacion(1L);
        verify(sentimentClient).solicitarAnalisisSentimiento(1L, request.getWhatsappMessage());
        verify(accesoRepository, never()).save(any());
        assertSame(acceso, result);
    }

    @Test
    void procesarAcceso_numeroNoRegistrado_lanzaExcepcion() {
        AccessRequest request = buildRequest();
        when(empresaService.validarNumeroEmpresa("12345")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> accessService.procesarAcceso(request));
    }

    @Test
    void procesarAcceso_mensajeInvalido_lanzaExcepcion() {
        AccessRequest req = new AccessRequest();
        WhatsappMessage msg = new WhatsappMessage();
        msg.setValue(new WhatsappMessage.Value());
        req.setWhatsappMessage(msg);
        req.setIdConversacion(1L);

        assertThrows(IllegalArgumentException.class, () -> accessService.procesarAcceso(req));
    }

    @Test
    void actualizarAccesos_existente_actualizaYGuarda() {
        Acceso existente = new Acceso(1L, 3L, false, false, false, false, false);
        Acceso nuevos = new Acceso(null, null, true, true, true, true, true);
        when(accesoRepository.findByIdEmpresa(3L)).thenReturn(Optional.of(existente));
        when(accesoRepository.save(any(Acceso.class))).thenAnswer(inv -> inv.getArgument(0));

        Acceso result = accessService.actualizarAccesos(3L, nuevos);

        verify(accesoRepository).save(existente);
        assertTrue(result.getSentimentAccess());
        assertTrue(result.getClassificationAccess());
        assertTrue(result.getAttentionQualityAccess());
        assertTrue(result.getFeedbackAccess());
        assertTrue(result.getLearnAccess());
    }

    @Test
    void actualizarAccesos_noExiste_lanzaExcepcion() {
        Acceso nuevos = new Acceso();
        when(accesoRepository.findByIdEmpresa(9L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> accessService.actualizarAccesos(9L, nuevos));
    }
}