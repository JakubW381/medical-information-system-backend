package com.example.his.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MedicalExaminationsPageRequest {
    private int page;
    private int size = 10;
    private String sortDirection = "desc"; // asc or desc
    private String sortBy = "date"; // date, patientLastName
    private String search; // search by patient name
}
