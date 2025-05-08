package com.attention.analysis.Message_Receptionist.controller;

import com.attention.analysis.Message_Receptionist.dto.WhatsappMessage;
import com.attention.analysis.Message_Receptionist.service.MensajeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
public class WhatsappController {

    private final MensajeService mensajeService;

    public WhatsappController(MensajeService mensajeService) {
        this.mensajeService = mensajeService;
    }

    @PostMapping
    public ResponseEntity<String> recibirMensaje(@RequestBody WhatsappMessage whatsappMessage) {
        boolean procesado = mensajeService.procesarMensaje(whatsappMessage);
        
        if (procesado) {
            return ResponseEntity.ok("Mensaje procesado correctamente");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se pudo procesar el mensaje");
        }
    }
}