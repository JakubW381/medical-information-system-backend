package com.example.his.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {

    private Long patientId;

    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String phoneNumber;

    private String bloodType;
    private String allergies;
    private String chronicDiseases;
    private String medications;
    private String insuranceNumber;
}
