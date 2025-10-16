package com.example.his.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {

    private Long id;

    private SafeUserDto patient;
    private SafeUserDto sender;

    private LocalDateTime dateTime;
    private List<String> tags;
    private String thumbnailSignedURL;
}
