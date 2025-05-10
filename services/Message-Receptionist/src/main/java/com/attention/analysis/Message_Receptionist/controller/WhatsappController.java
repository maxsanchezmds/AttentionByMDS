package com.attention.analysis.Message_Receptionist.controller;

import com.attention.analysis.Message_Receptionist.dto.WhatsappMessage;
import com.attention.analysis.Message_Receptionist.service.MensajeService;
import com.attention.analysis.Message_Receptionist.model.Mensaje;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/conversacion/{idConversacion}/mensajes")
    public ResponseEntity<?> obtenerMensajesConversacion(@PathVariable Long idConversacion) {
        List<Mensaje> mensajes = mensajeService.obtenerMensajesPorConversacion(idConversacion);
        if (mensajes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mensajes);
    }
}