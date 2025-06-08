package com.attention.analysis.Classification_Service.repository;

import com.attention.analysis.Classification_Service.model.Clasificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClasificacionRepository extends JpaRepository<Clasificacion, Long> {
    Optional<Clasificacion> findByIdConversacion(Long idConversacion);
    boolean existsByIdConversacion(Long idConversacion);
}