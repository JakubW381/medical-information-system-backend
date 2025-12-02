package com.example.his.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientsPageRequest {
    private int page;
    private String search;

    private String dateOfBirth;
    private String gender;
    private String address;
    private String phoneNumber;

    private String bloodType;
    private String allergies;
    private String chronicDiseases;
    private String medications;
    private String insuranceNumber;
}
