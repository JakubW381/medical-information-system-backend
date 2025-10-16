package com.example.his.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DocumentDto {

    private Long id;

    private SafeUserDto patient;
    private SafeUserDto sender;

    private LocalDateTime dateTime;
    private List<String> tags;
    private String thumbnailSignedURL;
}
