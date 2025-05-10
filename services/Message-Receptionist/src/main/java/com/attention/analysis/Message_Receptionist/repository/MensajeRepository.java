package com.attention.analysis.Message_Receptionist.repository;

import com.attention.analysis.Message_Receptionist.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    List<Mensaje> findByConversacionIdOrderByFechaAsc(Long idConversacion);
}