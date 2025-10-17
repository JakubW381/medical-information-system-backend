package com.example.his.controllers;

import com.example.his.dto.*;
import com.example.his.dto.request.DocumentPageRequest;
import com.example.his.dto.request.PatientsPageRequest;
import com.example.his.dto.response.PageResponse;
import com.example.his.model.Document;
import com.example.his.model.user.DoctorProfile;
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
@RequestMapping("/api/doc")
public class DoctorController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentService documentService;


    @GetMapping("/patient/{id}")
    public ResponseEntity<?> getPatientProfile(@PathVariable Long id){
        User patient = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));
        return ResponseEntity.ok(patient.getPatientProfile().toDto());
    }

    @PostMapping("/patient/documents/{id}")
    public ResponseEntity<PageResponse<DocumentTNDto>> getPatientDocuments(@PathVariable Long id, @RequestBody DocumentPageRequest pageDto){
        User patient = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));

        PageResponse<Document> documents = documentService.documentsByPatient(patient, pageDto);
        PageResponse<DocumentTNDto> dtos = documentService.generateDtos(documents);

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/patients")
    public ResponseEntity<PageResponse<PatientProfileDto>> getPatients(@RequestBody PatientsPageRequest pageDto){

        PageResponse<User> users = userService.getPatients(pageDto);
        PageResponse<PatientProfileDto> dtos = userService.generateDtos(users);

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/update-patient")
    public ResponseEntity<?> updatePatientProfile(@RequestBody PatientProfileDto dto) {

        User patient = userRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));

        userService.updatePatientProfile(patient,dto);

        return ResponseEntity.ok("Patient profile updated");
    }

    @PostMapping("/update-doctor")
    public ResponseEntity<?> updateDoctorProfile(@RequestBody DoctorProfileDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));

        DoctorProfile profile = doctor.getDoctorProfile();
        profile.setDepartment(dto.getDepartment());
        profile.setPosition(dto.getPosition());
        profile.setSpecialization(dto.getSpecialization());
        profile.setProfessionalLicenseNumber(dto.getProfessionalLicenseNumber());

        userRepository.save(doctor);
        return ResponseEntity.ok("Doctor profile updated");
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
}
