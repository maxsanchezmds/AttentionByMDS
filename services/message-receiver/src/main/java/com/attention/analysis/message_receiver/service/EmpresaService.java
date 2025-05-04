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
    
    @Transactional
    public Empresa findOrCreateEmpresa(String nombreEmpresa) {
        logger.info("Buscando o creando empresa: {}", nombreEmpresa);
        return empresaRepository.findByNombreEmpresa(nombreEmpresa)
                .orElseGet(() -> {
                    Empresa nuevaEmpresa = new Empresa();
                    nuevaEmpresa.setNombreEmpresa(nombreEmpresa);
                    logger.info("Creando nueva empresa: {}", nombreEmpresa);
                    return empresaRepository.save(nuevaEmpresa);
                });
    }
    
    public Optional<Empresa> findById(Long idEmpresa) {
        return empresaRepository.findById(idEmpresa);
    }
}