package com.example.classification_service.repository;

import com.example.classification_service.model.Clasificacion; // Importa la clase Clasificacion que será la que crearemos en la base de datos
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository // Indica que la siguiente interfaz es un repositorio
public interface ClasificacionRepository extends JpaRepository<Clasificacion, Long> {
    List <Clasificacion> findByIdEmpresa(Long IdEmpresa);
    List <Clasificacion> findByIdCliente(Long IdCliente);
}
