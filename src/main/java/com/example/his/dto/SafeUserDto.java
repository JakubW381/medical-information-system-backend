package com.example.his.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SafeUserDto {

    private Long id;
    private String name;
    private String lastName;

    private String specialization;
    private String department;
    private String position;
}
