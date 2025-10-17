package com.example.his.model;

import com.example.his.dto.DocumentTNDto;
import com.example.his.model.user.PatientProfile;
import com.example.his.model.user.User;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;


import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private PatientProfile patient;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @CreationTimestamp
    private LocalDateTime dateTime;

    @ElementCollection
    @CollectionTable(name = "document_tags", joinColumns = @JoinColumn(name = "document_id"))
    @Column(name = "tag")
    private List<String> tags;

    private String filePath;
    private String thumbnailPublicId;

    public DocumentTNDto toDto(String thumbnailSignedURL){
        return new DocumentTNDto(
                id,
                patient.getUser().toSafeUserDto(),
                sender.toSafeUserDto(),
                dateTime,
                tags,
                thumbnailSignedURL
        );
    }
}
