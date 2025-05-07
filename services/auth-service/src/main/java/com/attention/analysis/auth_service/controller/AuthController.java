package com.attention.analysis.auth_service.controller;

import com.attention.analysis.auth_service.dto.AuthResponseDTO;
import com.attention.analysis.auth_service.dto.LoginDTO;
import com.attention.analysis.auth_service.dto.RegistroEjecutivoDTO;
import com.attention.analysis.auth_service.dto.RegistroEmpresaDTO;
import com.attention.analysis.auth_service.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController

@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro/empresa")
    public ResponseEntity<AuthResponseDTO> registrarEmpresa(
            @RequestBody @Valid RegistroEmpresaDTO request
    ) {
        return ResponseEntity.ok(authService.registrarEmpresa(request));
    }

    @PostMapping("/registro/ejecutivo")
    public ResponseEntity<AuthResponseDTO> registrarEjecutivo(
            @RequestBody @Valid RegistroEjecutivoDTO request
    ) {
        return ResponseEntity.ok(authService.registrarEjecutivo(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @RequestBody @Valid LoginDTO request,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(authService.login(request, response));
    }
}