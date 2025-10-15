package com.example.his.dto;

import lombok.Data;

@Data
public class RegisterRequestDto {
    private String name;
    private String lastName;
    private String pesel;
    private String email;
    private String password;
}
