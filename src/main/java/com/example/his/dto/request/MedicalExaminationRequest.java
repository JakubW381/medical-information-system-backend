package com.example.his.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MedicalExaminationRequest {
    private LocalDateTime date;
    private String description;
}
