package com.attention.analysis.Access_Service.repository;

import com.attention.analysis.Access_Service.model.Acceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccesoRepository extends JpaRepository<Acceso, Long> {
    Optional<Acceso> findByIdEmpresa(Long idEmpresa);
}