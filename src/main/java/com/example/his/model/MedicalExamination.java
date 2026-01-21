package com.example.his.model;

import com.example.his.model.user.DoctorProfile;
import com.example.his.model.user.PatientProfile;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class MedicalExamination {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private DoctorProfile doctor;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private PatientProfile patient;

    private LocalDateTime date;

    @Column(columnDefinition = "TEXT")
    private String description;
}
