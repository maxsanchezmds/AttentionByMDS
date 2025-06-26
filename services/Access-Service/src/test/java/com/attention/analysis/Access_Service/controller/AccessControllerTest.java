package com.attention.analysis.Access_Service.controller;

import com.attention.analysis.Access_Service.dto.AccessRequest;
import com.attention.analysis.Access_Service.model.Acceso;
import com.attention.analysis.Access_Service.service.AccessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccessController.class)
class AccessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccessService accessService;

    private String buildRequestJson() {
        return "{\"idConversacion\":1,\"whatsappMessage\":{\"value\":{\"metadata\":{\"display_phone_number\":\"123\"}}}}";
    }

    @Test
    void checkAccess_ok() throws Exception {
        Acceso acceso = new Acceso(1L, 2L, false, false, false, false, false);
        given(accessService.procesarAcceso(any(AccessRequest.class))).willReturn(acceso);

        mockMvc.perform(post("/api/access/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestJson()))
                .andExpect(status().isOk());
    }

    @Test
    void checkAccess_badRequest() throws Exception {
        given(accessService.procesarAcceso(any(AccessRequest.class))).willThrow(new IllegalArgumentException("error"));

        mockMvc.perform(post("/api/access/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestJson()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearAccesosEmpresa_conflict() throws Exception {
        Acceso existing = new Acceso(1L, 2L, false, false, false, false, false);
        given(accessService.obtenerAccesosPorEmpresa(2L)).willReturn(Optional.of(existing));

        String body = "{\"idEmpresa\":2}";
        mockMvc.perform(post("/api/access/empresa/crear-accesos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void crearAccesosEmpresa_creaYRetornaOk() throws Exception {
        given(accessService.obtenerAccesosPorEmpresa(2L)).willReturn(Optional.empty());
        Acceso saved = new Acceso(1L, 2L, true, false, false, false, false);
        given(accessService.crearAccesos(any(Acceso.class))).willReturn(saved);

        String body = "{\"idEmpresa\":2}";
        mockMvc.perform(post("/api/access/empresa/crear-accesos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerAccesosPorEmpresa_found() throws Exception {
        Acceso acceso = new Acceso(1L, 2L, false, false, false, false, false);
        given(accessService.obtenerAccesosPorEmpresa(2L)).willReturn(Optional.of(acceso));

        mockMvc.perform(get("/api/access/empresa/2"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerAccesosPorEmpresa_notFound() throws Exception {
        given(accessService.obtenerAccesosPorEmpresa(2L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/access/empresa/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarAccesos_ok() throws Exception {
        Acceso acceso = new Acceso(1L, 2L, true, true, false, false, false);
        given(accessService.actualizarAccesos(eq(2L), any(Acceso.class))).willReturn(acceso);

        mockMvc.perform(put("/api/access/empresa/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void actualizarAccesos_notFound() throws Exception {
        given(accessService.actualizarAccesos(eq(2L), any(Acceso.class))).willThrow(new IllegalArgumentException("not"));

        mockMvc.perform(put("/api/access/empresa/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }
}