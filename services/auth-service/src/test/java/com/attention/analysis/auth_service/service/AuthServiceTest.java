package com.attention.analysis.auth_service.service;

import com.attention.analysis.auth_service.dto.*;
import com.attention.analysis.auth_service.model.Empresa;
import com.attention.analysis.auth_service.model.Rol;
import com.attention.analysis.auth_service.model.Usuario;
import com.attention.analysis.auth_service.repository.EmpresaRepository;
import com.attention.analysis.auth_service.repository.UsuarioRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AccessServiceClient accessServiceClient;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registrarEmpresa_whenDataValid_createsEmpresaAndUsuario() {
        RegistroEmpresaDTO dto = RegistroEmpresaDTO.builder()
                .nombreLegalEmpresa("Empresa X")
                .nitEmpresa("123")
                .telefonoEmpresa("111")
                .correoEmpresa("e@x.com")
                .direccionEmpresa("dir")
                .paisEmpresa("CO")
                .telefonoWhatsapp("222")
                .nombreCompleto("Admin")
                .correoElectronico("admin@x.com")
                .telefono("333")
                .documentoIdentidad("444")
                .password("pwd")
                .build();

        Empresa empresaGuardada = Empresa.builder().id(1L).build();
        given(empresaRepository.save(any(Empresa.class))).willReturn(empresaGuardada);
        given(jwtService.generateToken(any(Usuario.class))).willReturn("token");
        given(passwordEncoder.encode(anyString())).willReturn("hashed");

        AuthResponseDTO response = authService.registrarEmpresa(dto);

        assertThat(response.getToken()).isEqualTo("token");
        verify(usuarioRepository).save(any(Usuario.class));
        verify(accessServiceClient).crearAccesosEmpresa(1L);
    }

    @Test
    void registrarEmpresa_whenCorreoExistente_throwsException() {
        RegistroEmpresaDTO dto = RegistroEmpresaDTO.builder()
                .correoElectronico("existing@x.com")
                .documentoIdentidad("doc")
                .nitEmpresa("nit")
                .build();
        given(usuarioRepository.existsByCorreoElectronico("existing@x.com")).willReturn(true);

        assertThatThrownBy(() -> authService.registrarEmpresa(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("correo electrónico ya está registrado");
    }

    @Test
    void registrarEmpresa_whenDocumentoExistente_throwsException() {
        RegistroEmpresaDTO dto = RegistroEmpresaDTO.builder()
                .correoElectronico("new@x.com")
                .documentoIdentidad("doc")
                .nitEmpresa("nit")
                .build();
        given(usuarioRepository.existsByDocumentoIdentidad("doc")).willReturn(true);

        assertThatThrownBy(() -> authService.registrarEmpresa(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("documento de identidad ya está registrado");
    }

    @Test
    void registrarEmpresa_whenNitExistente_throwsException() {
        RegistroEmpresaDTO dto = RegistroEmpresaDTO.builder()
                .correoElectronico("new@x.com")
                .documentoIdentidad("doc")
                .nitEmpresa("nit")
                .build();
        given(empresaRepository.existsByNitEmpresa("nit")).willReturn(true);

        assertThatThrownBy(() -> authService.registrarEmpresa(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("NIT de la empresa ya está registrado");
    }

    @Test
    void registrarEjecutivo_whenEmpresaExiste_creaUsuario() {
        RegistroEjecutivoDTO dto = RegistroEjecutivoDTO.builder()
                .nombreCompleto("Ejecutivo")
                .correoElectronico("eje@x.com")
                .telefono("111")
                .documentoIdentidad("doc")
                .password("pwd")
                .identificadorEmpresa("ident")
                .build();

        Empresa empresa = Empresa.builder().identificadorEmpresa("ident").build();
        given(empresaRepository.findByIdentificadorEmpresa("ident")).willReturn(java.util.Optional.of(empresa));
        given(jwtService.generateToken(any(Usuario.class))).willReturn("tok");
        given(passwordEncoder.encode(anyString())).willReturn("hash");

        AuthResponseDTO response = authService.registrarEjecutivo(dto);

        assertThat(response.getToken()).isEqualTo("tok");
        assertThat(response.getIdentificadorEmpresa()).isEqualTo("ident");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void registrarEjecutivo_whenEmpresaNoExiste_throwsException() {
        RegistroEjecutivoDTO dto = RegistroEjecutivoDTO.builder()
                .identificadorEmpresa("ident")
                .correoElectronico("a@b.com")
                .documentoIdentidad("doc")
                .build();
        given(empresaRepository.findByIdentificadorEmpresa("ident")).willReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> authService.registrarEjecutivo(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Empresa no encontrada");
    }

    @Test
    void login_whenUsuarioValido_retornaTokenYCookie() {
        LoginDTO dto = LoginDTO.builder()
                .correoElectronico("user@x.com")
                .password("pwd")
                .build();

        Usuario usuario = Usuario.builder()
                .correoElectronico("user@x.com")
                .password("hashed")
                .rol(Rol.ADMIN)
                .build();

        given(usuarioRepository.findByCorreoElectronico("user@x.com")).willReturn(java.util.Optional.of(usuario));
        given(jwtService.generateToken(usuario)).willReturn("jwt");

        HttpServletResponse response = mock(HttpServletResponse.class);

        AuthResponseDTO res = authService.login(dto, response);

        assertThat(res.getToken()).isEqualTo("jwt");
        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    void login_whenUsuarioNoExiste_lanzaExcepcion() {
        LoginDTO dto = LoginDTO.builder()
                .correoElectronico("user@x.com")
                .password("pwd")
                .build();
        given(usuarioRepository.findByCorreoElectronico("user@x.com")).willReturn(java.util.Optional.empty());

        HttpServletResponse response = mock(HttpServletResponse.class);

        assertThatThrownBy(() -> authService.login(dto, response))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }
}