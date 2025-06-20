package com.attention.analysis.sentiment_service.repository;

import com.attention.analysis.sentiment_service.model.AvgSentiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AvgSentimentRepository extends JpaRepository<AvgSentiment, Long> {
    
    Optional<AvgSentiment> findByIdConversacion(Long idConversacion);
}