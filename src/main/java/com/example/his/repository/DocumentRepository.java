package com.example.his.repository;

import com.example.his.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByPatientId(Long id);
    List<Document> findBySenderId(Long id);
    List<Document> findByPatientIdAndSenderId(Long patientId, Long senderId);
}
