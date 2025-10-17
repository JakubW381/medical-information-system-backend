package com.example.his.model.user;

import com.example.his.dto.PatientProfileDto;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class PatientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String address;
    private String phoneNumber;

    private String bloodType;
    private String allergies;
    private String chronicDiseases;
    private String medications;
    private String insuranceNumber;

    public PatientProfileDto toDto(){
        return new PatientProfileDto(
                id,
                user.getName(),
                user.getLastName(),
                user.getPesel(),
                dateOfBirth,
                gender.name(),
                address,
                phoneNumber,
                bloodType,
                allergies,
                chronicDiseases,
                medications,
                insuranceNumber
        );
    }
}

