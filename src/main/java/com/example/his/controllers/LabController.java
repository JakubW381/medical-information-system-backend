package com.example.his.controllers;

import com.example.his.model.user.User;
import com.example.his.repository.UserRepository;
import com.example.his.service.DocumentService;
import com.example.his.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/lab")
public class LabController {

    @Autowired
    private LogService logService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("pesel") String pesel , @RequestParam("files") MultipartFile[] files) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User patient = userRepository.findByPesel(pesel)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));
        User lab = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Lab not found"));

        for (MultipartFile file : files) {
            try {
                documentService.saveFile(file, patient, lab);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error with file processing: " + file.getOriginalFilename());
            }
        }
        return ResponseEntity.ok("Files processed");
    }
}
