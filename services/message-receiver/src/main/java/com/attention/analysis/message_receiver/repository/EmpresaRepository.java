package com.attention.analysis.message_receiver.repository;

import com.attention.analysis.message_receiver.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    
    Optional<Empresa> findByNombreEmpresa(String nombreEmpresa);
    
    Optional<Empresa> findByIdentificadorEmpresa(String identificadorEmpresa);
    
    boolean existsByNombreEmpresa(String nombreEmpresa);
}