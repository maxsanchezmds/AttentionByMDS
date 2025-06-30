package com.attention.analysis.auth_service.controller;

import com.attention.analysis.auth_service.model.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @GetMapping("/perfil")
    public ResponseEntity<EntityModel<Map<String, Object>>> obtenerPerfilUsuario(
            @AuthenticationPrincipal Usuario usuario
    ) {
        Map<String, Object> perfil = new HashMap<>();
        perfil.put("id", usuario.getId());
        perfil.put("nombreCompleto", usuario.getNombreCompleto());
        perfil.put("correoElectronico", usuario.getCorreoElectronico());
        perfil.put("telefono", usuario.getTelefono());
        perfil.put("rol", usuario.getRol());
        
        if (usuario.getEmpresa() != null) {
            Map<String, Object> empresa = new HashMap<>();
            empresa.put("id", usuario.getEmpresa().getId());
            empresa.put("nombreLegal", usuario.getEmpresa().getNombreLegal());
            empresa.put("identificadorEmpresa", usuario.getEmpresa().getIdentificadorEmpresa());
            empresa.put("telefonoEmpresa", usuario.getEmpresa().getTelefonoEmpresa());
            empresa.put("correoEmpresa", usuario.getEmpresa().getCorreoEmpresa());
            empresa.put("nitEmpresa", usuario.getEmpresa().getNitEmpresa());
            empresa.put("direccionEmpresa", usuario.getEmpresa().getDireccionEmpresa());
            empresa.put("paisEmpresa", usuario.getEmpresa().getPaisEmpresa());
            empresa.put("telefonoWhatsapp", usuario.getEmpresa().getTelefonoWhatsapp());
            perfil.put("empresa", empresa);
        }

        EntityModel<Map<String, Object>> resource = EntityModel.of(perfil,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UsuarioController.class)
                        .obtenerPerfilUsuario(null)).withSelfRel(),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UsuarioController.class)
                        .soloParaAdmins()).withRel("admin"),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UsuarioController.class)
                        .soloParaEjecutivos()).withRel("ejecutivo"));

        return ResponseEntity.ok(resource);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('EMPRESA')")
    public ResponseEntity<String> soloParaAdmins() {
        return ResponseEntity.ok("Este endpoint solo es accesible para administradores de empresas");
    }

    @GetMapping("/ejecutivo")
    @PreAuthorize("hasRole('EJECUTIVO')")
    public ResponseEntity<String> soloParaEjecutivos() {
        return ResponseEntity.ok("Este endpoint solo es accesible para ejecutivos");
    }
}