package com.example.classification_service.service;

import com.example.classification_service.dto.dto_saliente.MensajeDtoSaliente;
import com.example.classification_service.dto.dto_entrante.MensajeDtoEntrante;
import com.example.classification_service.repository.ClasificacionRepository;
import com.example.classification_service.model.Clasificacion;
import com.example.classification_service.service.ClasificacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service // Indica que la siguiente clase es un servicio
@Transactional // Si algo falla, se cancela todo lo que hizo la clase, sirbe para asegurarnos de que todo funcione.

public class ClasificacionServiceImpl implements ClasificacionService {
    private final ClasificacionRepository clasificacionRepository;

    public ClasificacionServiceImpl(ClasificacionRepository clasificacionRepository) {
        this.clasificacionRepository = clasificacionRepository;
    }

    @Override //El @override no es obligatorio, pero sí muy recomendable, ya que ayuda a escribir código más robusto y fácil de mantener.
    // A continuacion se deben crear los metodos de la interfaz y la logica de cada uno
    public MensajeDtoSaliente clasificarConversacion(MensajeDtoEntrante mensajeDtoEntrante) {
        return null;
    }
}