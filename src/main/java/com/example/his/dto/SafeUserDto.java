package com.example.his.dto;

import lombok.Data;

@Data
public class SafeUserDto {
    private String name;
    private String lastName;

    private String specialization;
    private String department;
    private String position;
}
