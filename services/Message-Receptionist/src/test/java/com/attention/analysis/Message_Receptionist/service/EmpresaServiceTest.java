package com.attention.analysis.Message_Receptionist.service;

import com.attention.analysis.Message_Receptionist.dto.Empresa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    private EmpresaService empresaService;

    @BeforeEach
    void setUp() {
        empresaService = Mockito.spy(new EmpresaService(WebClient.builder(), "http://localhost"));
    }

    @Test
    void validarNumeroEmpresa_coincidenciaExacta() {
        Empresa emp = new Empresa();
        emp.setId(1L);
        emp.setCorreoEmpresa("corp@example.com");
        emp.setTelefonoWhatsapp("56 9 1234 5678");

        doReturn(List.of(emp)).when(empresaService).obtenerEmpresas();

        Optional<Empresa> result = empresaService.validarNumeroEmpresa("56 9 1234 5678");
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void validarNumeroEmpresa_numeroTruncadoNoCoincide() {
        Empresa emp = new Empresa();
        emp.setId(2L);
        emp.setCorreoEmpresa("corp@example.com");
        emp.setTelefonoWhatsapp("56 9 1234 5678");

        doReturn(List.of(emp)).when(empresaService).obtenerEmpresas();

        Optional<Empresa> result = empresaService.validarNumeroEmpresa("56 9 1234 56");
        assertTrue(result.isEmpty());
    }
}