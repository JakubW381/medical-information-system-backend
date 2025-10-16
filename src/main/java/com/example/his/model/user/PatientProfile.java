package com.example.his.model.user;

import com.example.his.dto.UserProfileDto;
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
    private String gender;
    private String address;
    private String phoneNumber;

    private String bloodType;
    private String allergies;
    private String chronicDiseases;
    private String medications;
    private String insuranceNumber;

    public UserProfileDto toDto(){
        return new UserProfileDto(
                id,
                dateOfBirth,
                gender,
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

