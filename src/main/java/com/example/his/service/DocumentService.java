package com.example.his.service;

import com.example.his.model.Document;
import com.example.his.model.user.User;
import com.example.his.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    public void saveFile(MultipartFile file, User patient, User sender, String publicId) throws IOException {
        String uploadDir = "uploads/documents/"+patient.getPesel();
        File dir = new File(uploadDir);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;

        Path filePath = Paths.get(uploadDir + uniqueFilename);

        //TODO implement file encryption

        Files.write(filePath, file.getBytes());

        Document document = new Document();
        document.setPatient(patient.getPatientProfile());
        document.setSender(sender);
        document.setFilePath(filePath.toString());
        document.setDateTime(LocalDateTime.now());
        document.setThumbnailPublicId(publicId);

        documentRepository.save(document);
    }





}
