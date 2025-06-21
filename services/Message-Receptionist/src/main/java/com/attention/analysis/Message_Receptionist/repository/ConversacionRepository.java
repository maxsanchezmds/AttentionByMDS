package com.attention.analysis.Message_Receptionist.repository;

import com.attention.analysis.Message_Receptionist.model.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {
    Optional<Conversacion> findByTelefonoCliente(String telefonoCliente);
    Optional<Conversacion> findByTelefonoClienteAndIdEmpresa(String telefonoCliente, Long idEmpresa);
}