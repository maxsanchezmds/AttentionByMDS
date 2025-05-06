package com.attention.analysis.auth_service.repository;

import com.attention.analysis.auth_service.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByIdentificadorEmpresa(String identificadorEmpresa);
    
    // Eliminar el método problemático
    // boolean existsByNit(String nit);
    
    // Método de conveniencia que siempre devuelve falso
    default boolean existsByNit(String nit) {
        return false;
    }
}