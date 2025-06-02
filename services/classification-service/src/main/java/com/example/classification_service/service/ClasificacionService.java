package com.example.classification_service.service;

import com.example.classification_service.dto.dto_entrante.MensajeDtoEntrante;
import com.example.classification_service.dto.dto_saliente.MensajeDtoSaliente;
import java.util.List;
import java.util.Optional;

public interface ClasificacionService {
    MensajeDtoSaliente clasificarConversacion(MensajeDtoEntrante mensajeDtoEntrante);
    MensajeDtoSaliente buscarConversacion(Long idConversacion);
    List<MensajeDtoSaliente> buscarConversaciones(Long idEmpresa);
}
