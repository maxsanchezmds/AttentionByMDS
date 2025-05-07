package com.attention.analysis.auth_service.service;

import com.attention.analysis.auth_service.dto.*;
import com.attention.analysis.auth_service.model.Empresa;
import com.attention.analysis.auth_service.model.Rol;
import com.attention.analysis.auth_service.model.Usuario;
import com.attention.analysis.auth_service.repository.EmpresaRepository;
import com.attention.analysis.auth_service.repository.UsuarioRepository;
import com.attention.analysis.auth_service.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponseDTO registrarEmpresa(RegistroEmpresaDTO request) {
        // Verificar si ya existe un usuario con el mismo correo o documento
        if (usuarioRepository.existsByCorreoElectronico(request.getCorreoElectronico())) {
            throw new RuntimeException("El correo electrónico ya está registrado");
        }
        
        if (usuarioRepository.existsByDocumentoIdentidad(request.getDocumentoIdentidad())) {
            throw new RuntimeException("El documento de identidad ya está registrado");
        }
        
        // Verificar si el NIT ya está registrado
        if (empresaRepository.existsByNitEmpresa(request.getNitEmpresa())) {
            throw new RuntimeException("El NIT de la empresa ya está registrado");
        }
        
        // Crear la empresa con todos los nuevos campos
        Empresa empresa = Empresa.builder()
                .nombreLegal(request.getNombreLegalEmpresa())
                .nitEmpresa(request.getNitEmpresa())
                .telefonoEmpresa(request.getTelefonoEmpresa())
                .correoEmpresa(request.getCorreoEmpresa())
                .direccionEmpresa(request.getDireccionEmpresa())
                .paisEmpresa(request.getPaisEmpresa())
                .telefonoWhatsapp(request.getTelefonoWhatsapp())
                .build();
        
        // Generar el identificador único automáticamente
        empresa.generarIdentificador();
        
        // Guardar la empresa
        empresa = empresaRepository.save(empresa);
        
        // Crear el usuario administrador de la empresa
        Usuario usuario = Usuario.builder()
                .nombreCompleto(request.getNombreCompleto())
                .correoElectronico(request.getCorreoElectronico())
                .telefono(request.getTelefono())
                .documentoIdentidad(request.getDocumentoIdentidad())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(Rol.EMPRESA)
                .empresa(empresa)
                .build();
        
        usuarioRepository.save(usuario);
        
        // Generar el token JWT
        String jwtToken = jwtService.generateToken(usuario);
        
        return AuthResponseDTO.builder()
                .token(jwtToken)
                .mensaje("Empresa y administrador registrados exitosamente")
                .identificadorEmpresa(empresa.getIdentificadorEmpresa())
                .build();
    }

    @Transactional
    public AuthResponseDTO registrarEjecutivo(RegistroEjecutivoDTO request) {
        // Verificar si ya existe un usuario con el mismo correo o documento
        if (usuarioRepository.existsByCorreoElectronico(request.getCorreoElectronico())) {
            throw new RuntimeException("El correo electrónico ya está registrado");
        }
        
        if (usuarioRepository.existsByDocumentoIdentidad(request.getDocumentoIdentidad())) {
            throw new RuntimeException("El documento de identidad ya está registrado");
        }
        
        // Buscar la empresa por su identificador
        Empresa empresa = empresaRepository.findByIdentificadorEmpresa(request.getIdentificadorEmpresa())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con el identificador proporcionado"));
        
        // Crear el usuario ejecutivo
        Usuario usuario = Usuario.builder()
                .nombreCompleto(request.getNombreCompleto())
                .correoElectronico(request.getCorreoElectronico())
                .telefono(request.getTelefono())
                .documentoIdentidad(request.getDocumentoIdentidad())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(Rol.EJECUTIVO)
                .empresa(empresa)
                .build();
        
        usuarioRepository.save(usuario);
        
        // Generar el token JWT
        String jwtToken = jwtService.generateToken(usuario);
        
        return AuthResponseDTO.builder()
                .token(jwtToken)
                .mensaje("Ejecutivo registrado exitosamente")
                .identificadorEmpresa(empresa.getIdentificadorEmpresa())
                .build();
    }

    public AuthResponseDTO login(LoginDTO request, HttpServletResponse response) {
        // Autenticar al usuario
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getCorreoElectronico(),
                        request.getPassword()
                )
        );
        
        // Buscar al usuario
        Usuario usuario = usuarioRepository.findByCorreoElectronico(request.getCorreoElectronico())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        
        // Generar el token JWT
        String jwtToken = jwtService.generateToken(usuario);
        
        // Establecer la cookie segura
        Cookie cookie = new Cookie("jwt_token", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Solo para HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(3600 * 24); // 24 horas
        response.addCookie(cookie);
        
        String identificadorEmpresa = null;
        if (usuario.getEmpresa() != null) {
            identificadorEmpresa = usuario.getEmpresa().getIdentificadorEmpresa();
        }
        
        return AuthResponseDTO.builder()
                .token(jwtToken)
                .mensaje("Inicio de sesión exitoso")
                .identificadorEmpresa(identificadorEmpresa)
                .build();
    }
}