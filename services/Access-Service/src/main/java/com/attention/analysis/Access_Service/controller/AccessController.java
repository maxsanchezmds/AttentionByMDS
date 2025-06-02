package com.attention.analysis.Access_Service.controller;

import com.attention.analysis.Access_Service.dto.AccessRequest;
import com.attention.analysis.Access_Service.model.Acceso;
import com.attention.analysis.Access_Service.service.AccessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/access")
public class AccessController {
    
    private final AccessService accessService;
    
    public AccessController(AccessService accessService) {
        this.accessService = accessService;
    }
    
    @PostMapping("/check")
    public ResponseEntity<?> checkAccess(@Valid @RequestBody AccessRequest request) {
        try {
            Acceso acceso = accessService.procesarAcceso(request);
            return ResponseEntity.ok(acceso);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la solicitud");
        }
    }
    
    @GetMapping("/empresa/{idEmpresa}")
    public ResponseEntity<?> obtenerAccesosPorEmpresa(@PathVariable Long idEmpresa) {
        return accessService.obtenerAccesosPorEmpresa(idEmpresa)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/empresa/{idEmpresa}")
    public ResponseEntity<?> actualizarAccesos(@PathVariable Long idEmpresa, 
                                              @RequestBody Acceso acceso) {
        try {
            Acceso accesoActualizado = accessService.actualizarAccesos(idEmpresa, acceso);
            return ResponseEntity.ok(accesoActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar los accesos");
        }
    }
}