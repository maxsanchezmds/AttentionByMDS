package com.attention.analysis.Classification_Service.repository;

import com.attention.analysis.Classification_Service.model.Clasificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClasificacionRepository extends JpaRepository<Clasificacion, Long> {
    
    /**
     * Encuentra la clasificación más reciente para una conversación
     */
    Optional<Clasificacion> findByIdConversacion(Long idConversacion);
    
    /**
     * Verifica si existe alguna clasificación para una conversación
     */
    boolean existsByIdConversacion(Long idConversacion);
    
    /**
     * Encuentra todas las clasificaciones de una conversación ordenadas por fecha
     */
    @Query("SELECT c FROM Clasificacion c WHERE c.idConversacion = :idConversacion ORDER BY c.fechaClasificacion DESC")
    List<Clasificacion> findAllByIdConversacionOrderByFechaDesc(@Param("idConversacion") Long idConversacion);
    
    /**
     * Encuentra clasificaciones por tipo
     */
    @Query("SELECT c FROM Clasificacion c WHERE c.clasificacion = :tipo ORDER BY c.fechaClasificacion DESC")
    List<Clasificacion> findByTipoClasificacion(@Param("tipo") Clasificacion.TipoClasificacion tipo);
    
    /**
     * Encuentra clasificaciones recientes (útil para estadísticas)
     */
    @Query("SELECT c FROM Clasificacion c WHERE c.fechaClasificacion >= :fechaDesde ORDER BY c.fechaClasificacion DESC")
    List<Clasificacion> findRecentClasificaciones(@Param("fechaDesde") LocalDateTime fechaDesde);
    
    /**
     * Cuenta clasificaciones por tipo en un período
     */
    @Query("SELECT COUNT(c) FROM Clasificacion c WHERE c.clasificacion = :tipo AND c.fechaClasificacion >= :fechaDesde")
    Long countByTipoAndFechaDesde(@Param("tipo") Clasificacion.TipoClasificacion tipo, @Param("fechaDesde") LocalDateTime fechaDesde);
    
    /**
     * Cuenta total de clasificaciones para una conversación
     */
    Long countByIdConversacion(Long idConversacion);
}