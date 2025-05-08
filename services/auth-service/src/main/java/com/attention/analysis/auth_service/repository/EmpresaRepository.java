package com.attention.analysis.auth_service.repository;

import com.attention.analysis.auth_service.dto.EmpresaWhatsappDTO;
import com.attention.analysis.auth_service.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByIdentificadorEmpresa(String identificadorEmpresa);
    
    boolean existsByNitEmpresa(String nitEmpresa);
    
    // Nuevo método que selecciona solo las columnas necesarias
    @Query("SELECT new com.attention.analysis.auth_service.dto.EmpresaWhatsappDTO(e.id, e.correoEmpresa, e.telefonoWhatsapp) FROM Empresa e")
    List<EmpresaWhatsappDTO> findAllEmpresasWhatsapp();
}