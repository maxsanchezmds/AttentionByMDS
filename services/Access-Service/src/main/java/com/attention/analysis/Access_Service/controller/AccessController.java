package com.attention.analysis.Access_Service.controller;

import com.attention.analysis.Access_Service.dto.AccessRequest;
import com.attention.analysis.Access_Service.model.Acceso;
import com.attention.analysis.Access_Service.service.AccessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

@RestController
@RequestMapping("/api/access")
public class AccessController {
    
    private final AccessService accessService;
    
    public AccessController(AccessService accessService) {
        this.accessService = accessService;
    }
    private EntityModel<Acceso> toModel(Acceso acceso) {
        return EntityModel.of(acceso,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AccessController.class)
                        .obtenerAccesosPorEmpresa(acceso.getIdEmpresa())).withSelfRel(),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AccessController.class)
                        .actualizarAccesos(acceso.getIdEmpresa(), acceso)).withRel("update"));
    }

    @PostMapping("/check")
    public ResponseEntity<?> checkAccess(@Valid @RequestBody AccessRequest request) {
        try {
            Acceso acceso = accessService.procesarAcceso(request);
            return ResponseEntity.ok(toModel(acceso));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la solicitud");
        }
    }
    
    @PostMapping("/empresa/crear-accesos")
    public ResponseEntity<?> crearAccesosEmpresa(@RequestBody Acceso acceso) {
        try {
            // Verificar si ya existen accesos para esta empresa
            if (accessService.obtenerAccesosPorEmpresa(acceso.getIdEmpresa()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Ya existen accesos configurados para esta empresa");
            }
            
            // Guardar los nuevos accesos
            Acceso accesoGuardado = accessService.crearAccesos(acceso);
            return ResponseEntity.ok(toModel(accesoGuardado));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear los accesos: " + e.getMessage());
        }
    }
    
    @GetMapping("/empresa/{idEmpresa}")
    public ResponseEntity<?> obtenerAccesosPorEmpresa(@PathVariable Long idEmpresa) {
        return accessService.obtenerAccesosPorEmpresa(idEmpresa)
                .map(acc -> ResponseEntity.ok(toModel(acc)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/empresa/{idEmpresa}")
    public ResponseEntity<?> actualizarAccesos(@PathVariable Long idEmpresa, 
                                              @RequestBody Acceso acceso) {
        try {
            Acceso accesoActualizado = accessService.actualizarAccesos(idEmpresa, acceso);
            return ResponseEntity.ok(toModel(accesoActualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar los accesos");
        }
    }
}