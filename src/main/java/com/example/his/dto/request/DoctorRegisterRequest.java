package com.example.his.dto.request;

import com.example.his.model.user.Role;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DoctorRegisterRequest {

    private String name;
    private String lastName;

    private String email;
    private String pesel;

    private Role role;


    private String specialization;
    private String department;
    private String position;
    private String professionalLicenseNumber;

}
