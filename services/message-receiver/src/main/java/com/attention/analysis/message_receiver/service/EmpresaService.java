package com.attention.analysis.message_receiver.service;

import com.attention.analysis.message_receiver.model.Empresa;
import com.attention.analysis.message_receiver.repository.EmpresaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class EmpresaService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmpresaService.class);
    
    @Autowired
    private EmpresaRepository empresaRepository;
    
    /**
     * Busca o crea una empresa con un nombre predeterminado
     */
    @Transactional
    public Empresa findOrCreateEmpresa(String nombreEmpresa) {
        logger.info("Buscando o creando empresa: {}", nombreEmpresa);
        
        // Buscar la empresa por nombre
        Optional<Empresa> existingEmpresa = empresaRepository.findByNombreEmpresa(nombreEmpresa);
        
        if (existingEmpresa.isPresent()) {
            logger.info("Empresa encontrada: {}", nombreEmpresa);
            return existingEmpresa.get();
        } else {
            logger.info("Creando nueva empresa: {}", nombreEmpresa);
            Empresa nuevaEmpresa = new Empresa();
            nuevaEmpresa.setNombreEmpresa(nombreEmpresa);
            
            // Generar un identificador aleatorio para la empresa
            nuevaEmpresa.setIdentificadorEmpresa(java.util.UUID.randomUUID().toString());
            
            return empresaRepository.save(nuevaEmpresa);
        }
    }
    
    /**
     * Busca una empresa por su identificador único
     */
    public Empresa findByIdentificadorEmpresa(String identificadorEmpresa) {
        return empresaRepository.findByIdentificadorEmpresa(identificadorEmpresa)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con el identificador: " + identificadorEmpresa));
    }
    
    /**
     * Busca una empresa por su ID
     */
    public Optional<Empresa> findById(Long id) {
        return empresaRepository.findById(id);
    }
}