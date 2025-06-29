package com.attention.analysis.auth_service.controller;

import com.attention.analysis.auth_service.dto.EmpresaWhatsappDTO;
import com.attention.analysis.auth_service.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
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
    public ResponseEntity<CollectionModel<EntityModel<EmpresaWhatsappDTO>>> obtenerEmpresasWhatsapp() {
        List<EntityModel<EmpresaWhatsappDTO>> empresas = empresaRepository.findAllEmpresasWhatsapp()
                .stream()
                .map(e -> EntityModel.of(e,
                        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(IntegracionController.class)
                                .obtenerEmpresasWhatsapp()).withSelfRel()))
                .toList();

        CollectionModel<EntityModel<EmpresaWhatsappDTO>> collection = CollectionModel.of(empresas,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(IntegracionController.class)
                        .obtenerEmpresasWhatsapp()).withSelfRel());

        return ResponseEntity.ok(collection);
    }
}