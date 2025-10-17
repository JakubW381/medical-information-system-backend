package com.example.his.controllers;

import com.example.his.dto.DocumentTNDto;
import com.example.his.dto.request.DocumentPageRequest;
import com.example.his.dto.response.PageResponse;
import com.example.his.dto.PatientProfileDto;
import com.example.his.model.Document;
import com.example.his.model.user.User;
import com.example.his.repository.UserRepository;
import com.example.his.service.DocumentService;
import com.example.his.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentService documentService;


    @PostMapping("/update-patient")
    public ResponseEntity<?> updatePatientProfile(@RequestBody PatientProfileDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));
        userService.updatePatientProfile(patient,dto);
        return ResponseEntity.ok("Patient profile updated");
    }

    @GetMapping("/patient")
    public ResponseEntity<?> getPatientProfile(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));
        return ResponseEntity.ok(patient.getPatientProfile().toDto());
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile[] files) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));

        for (MultipartFile file : files) {
            try {
                documentService.saveFile(file, patient, patient);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error with file processing: " + file.getOriginalFilename());
            }
        }
        return ResponseEntity.ok("Files processed");
    }

    @GetMapping("/gallery")
    public ResponseEntity<PageResponse<DocumentTNDto>> showGallery(DocumentPageRequest pageDto){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PageResponse<Document> documents = documentService.documentsByPatient(patient, pageDto);
        PageResponse<DocumentTNDto> dtos = documentService.generateDtos(documents);

        return ResponseEntity.ok(dtos);
    }
}
