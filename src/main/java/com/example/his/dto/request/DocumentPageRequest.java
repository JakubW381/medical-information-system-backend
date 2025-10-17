package com.example.his.dto.request;

import lombok.Data;

@Data
public class DocumentPageRequest {
    private int page;
    private int year;
    private String search;
}
