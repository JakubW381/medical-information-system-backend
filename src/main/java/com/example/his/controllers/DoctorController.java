package com.example.his.controllers;

import com.example.his.dto.*;
import com.example.his.dto.request.DocumentPageRequest;
import com.example.his.dto.request.PatientRegisterRequest;
import com.example.his.dto.request.PatientsPageRequest;
import com.example.his.dto.response.PageResponse;
import com.example.his.model.Document;
import com.example.his.model.logs.Log;
import com.example.his.model.logs.LogType;
import com.example.his.model.user.DoctorProfile;
import com.example.his.model.user.User;
import com.example.his.repository.DocumentRepository;
import com.example.his.repository.UserRepository;
import com.example.his.service.DocumentService;
import com.example.his.service.LogService;
import com.example.his.service.user.AuthService;
import com.example.his.service.user.RegisterResponse;
import com.example.his.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;


@RestController
@RequestMapping("/api/doc")
public class DoctorController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private AuthService authService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private LogService logService;

    @GetMapping("/patient/{id}")
    public ResponseEntity<?> getPatientProfile(@PathVariable Long id){
        User patient = userRepository.findByPatientProfileId(id)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));
        return ResponseEntity.ok(patient.getPatientProfile().toDto());
    }

    @PostMapping("/patient/documents/{id}")
    public ResponseEntity<PageResponse<DocumentTNDto>> getPatientDocuments(@PathVariable Long id, @RequestBody DocumentPageRequest pageDto){
        User patient = userRepository.findByPatientProfileId(id)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));

        PageResponse<Document> documents = documentService.documentsByPatient(patient, pageDto);
        PageResponse<DocumentTNDto> dtos = documentService.generateDtos(documents);

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/patient/document/{id}")
    public ResponseEntity<String> getPatientDocument(@PathVariable Long id){
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No document with this Id"));

        try {
            String base64File = documentService.getFileBase64(document);
            return ResponseEntity.ok(base64File);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read file", e);
        }
    }

    @PostMapping("/patients")
    public ResponseEntity<PageResponse<PatientProfileDto>> getPatients(@RequestBody PatientsPageRequest pageDto){
        PageResponse<PatientProfileDto> dtos = userService.getPatients(pageDto);
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

    @PostMapping("/patient/{id}/upload")
    public ResponseEntity<?> uploadFiles(@PathVariable Long id , @RequestParam("files") MultipartFile[] files) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User patient = userRepository.findByPatientProfileId(id)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));
        User doctor = userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("Doctor not found"));

        for (MultipartFile file : files) {
            try {
                documentService.saveFile(file, patient, doctor);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error with file processing: " + file.getOriginalFilename());
            }
        }
        return ResponseEntity.ok("Files processed");
    }


    @PostMapping("/register-patient")
    public ResponseEntity<?> registerPatient(@RequestBody PatientRegisterRequest patientRegisterRequest){

        RegisterResponse response = authService.registerPatient(patientRegisterRequest);

        if (response == RegisterResponse.EMAIL_EXISTS) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("User with this email address already exists");
        }
        if (response == RegisterResponse.PESEL_EXISTS) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("User with this PESEL already exists");
        }

        if (response == RegisterResponse.SUCCESS){

            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Log log = new Log();

            User author = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));

            User target = userRepository.findByEmail(patientRegisterRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("unknown user repo error"));

            log.setAuthor(author);
            log.setTarget(target);
            log.setLogType(LogType.USER_REGISTERED);
            log.setDescription("Patient Registered");
            logService.saveLog(log);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Signed up successfully");
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected registration Error");
    }
}
