package com.example.his.dto.request;

import com.example.his.model.user.Role;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientRegisterRequest {

    private String name;
    private String lastName;

    private String email;
    private String pesel;

    private Role role;

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
