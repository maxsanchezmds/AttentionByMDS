package com.attention.analysis.sentiment_service.repository;

import com.attention.analysis.sentiment_service.model.Sentiment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SentimentRepository extends JpaRepository<Sentiment, Long> {
    
    @Query("SELECT s FROM Sentiment s WHERE s.idConversacion = ?1 ORDER BY s.fechaEnvio DESC")
    List<Sentiment> findLastMessagesByConversationId(String idConversacion, Pageable pageable);
}