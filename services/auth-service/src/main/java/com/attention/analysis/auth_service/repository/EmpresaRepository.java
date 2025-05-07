package com.attention.analysis.auth_service.repository;

import com.attention.analysis.auth_service.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByIdentificadorEmpresa(String identificadorEmpresa);
    
    // Añadimos método para verificar NIT
    boolean existsByNitEmpresa(String nitEmpresa);
}