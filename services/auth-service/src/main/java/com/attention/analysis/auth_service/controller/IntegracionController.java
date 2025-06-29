package com.attention.analysis.auth_service.controller;

import com.attention.analysis.auth_service.dto.EmpresaWhatsappDTO;
import com.attention.analysis.auth_service.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/integracion")
@RequiredArgsConstructor
public class IntegracionController {

    private final EmpresaRepository empresaRepository;

    @GetMapping("/empresas/whatsapp")
    public ResponseEntity<List<EmpresaWhatsappDTO>> obtenerEmpresasWhatsapp() {
        List<EmpresaWhatsappDTO> empresas = empresaRepository.findAllEmpresasWhatsapp();
        return ResponseEntity.ok(empresas);
    }
}