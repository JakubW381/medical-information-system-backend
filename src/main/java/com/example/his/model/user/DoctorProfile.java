package com.example.his.model.user;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class DoctorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String specialization;
    private String department;
    private String position;
    private String professionalLicenseNumber;

}
