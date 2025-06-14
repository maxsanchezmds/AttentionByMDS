package com.attention.analysis.sentiment_service.repository;

import com.attention.analysis.sentiment_service.model.SvgSentiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SvgSentimentRepository extends JpaRepository<SvgSentiment, Long> {
    
    Optional<SvgSentiment> findByIdConversacion(Long idConversacion);
}