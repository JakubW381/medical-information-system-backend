package com.example.his.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicalExaminationResponse {
    private Long id;
    private String patientName;
    private String patientLastName;
    private LocalDateTime date;
    private String description;
}
