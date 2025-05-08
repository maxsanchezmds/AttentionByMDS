package com.attention.analysis.Message_Receptionist.repository;

import com.attention.analysis.Message_Receptionist.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
}