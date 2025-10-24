package com.example.his.service.tags;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TaggingService {
    List<String> tagPdf(MultipartFile file);
    List<String> tagImage(MultipartFile file);
    List<String> tagDicom(MultipartFile file);
}
