package com.example.his.controllers;

import com.example.his.dto.*;
import com.example.his.dto.request.DocumentPageRequest;
import com.example.his.dto.request.PatientRegisterRequest;
import com.example.his.dto.request.PatientsPageRequest;
import com.example.his.dto.request.MedicalExaminationRequest;
import com.example.his.dto.request.MedicalExaminationsPageRequest;
import com.example.his.dto.request.MessageRequest;
import com.example.his.dto.response.MedicalExaminationResponse;
import com.example.his.dto.response.PageResponse;
import com.example.his.model.Document;
import com.example.his.model.MedicalExamination;
import com.example.his.model.logs.Log;
import com.example.his.model.logs.LogType;
import com.example.his.model.user.DoctorProfile;
import com.example.his.model.user.User;
import com.example.his.model.user.PatientProfile;
import com.example.his.repository.DocumentRepository;
import com.example.his.repository.MedicalExaminationRepository;
import com.example.his.repository.UserRepository;
import com.example.his.service.DocumentService;
import com.example.his.service.LogService;
import com.example.his.service.user.AuthService;
import com.example.his.service.user.RegisterResponse;
import com.example.his.service.user.UserService;
import com.example.his.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
    private MedicalExaminationRepository medicalExaminationRepository;

    @Autowired
    private LogService logService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/send-message")
    public ResponseEntity<?> sendMessage(@RequestBody MessageRequest messageRequest) {
        try {
            User user = userRepository.findByEmail(messageRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String name = user.getName() + (user.getLastName() != null ? " " + user.getLastName() : "");

            String content = messageRequest.getContent().replace("\n", "<br/>");
            emailService.sendMail(messageRequest.getEmail(), name, content, messageRequest.getSubject());
            return ResponseEntity.ok("Message sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sending message: " + e.getMessage());
        }
    }

    @GetMapping("/patient/{id}")
    public ResponseEntity<?> getPatientProfile(@PathVariable Long id) {
        User patient = userRepository.findByPatientProfileId(id)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));
        return ResponseEntity.ok(patient.getPatientProfile().toDto());
    }

    @PostMapping("/patient/documents/{id}")
    public ResponseEntity<PageResponse<DocumentTNDto>> getPatientDocuments(@PathVariable Long id,
            @RequestBody DocumentPageRequest pageDto) {
        User patient = userRepository.findByPatientProfileId(id)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));

        PageResponse<Document> documents = documentService.documentsByPatient(patient, pageDto);
        PageResponse<DocumentTNDto> dtos = documentService.generateDtos(documents);

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/patient/document/{id}")
    public ResponseEntity<String> getPatientDocument(@PathVariable Long id) {
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
    public ResponseEntity<PageResponse<PatientProfileDto>> getPatients(@RequestBody PatientsPageRequest pageDto) {
        PageResponse<PatientProfileDto> dtos = userService.getPatients(pageDto);
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/update-patient")
    public ResponseEntity<?> updatePatientProfile(@RequestBody PatientProfileDto dto) {

        User patient = userRepository.findByPatientProfileId(dto.getPatientId())
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));

        userService.updatePatientProfile(patient, dto);

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
    public ResponseEntity<?> uploadFiles(@PathVariable Long id, @RequestParam("files") MultipartFile[] files) {

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
    public ResponseEntity<?> registerPatient(@RequestBody PatientRegisterRequest patientRegisterRequest) {

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

        if (response == RegisterResponse.SUCCESS) {

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

    @GetMapping("/doctor")
    public ResponseEntity<?> getDoctorProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));
        return ResponseEntity.ok(patient.toDoctorProfileDto());
    }

    @PostMapping("/assign-patient/{patientId}")
    public ResponseEntity<?> assignPatient(@PathVariable Long patientId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctorUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Doctor not found"));

        User patientUser = userRepository.findByPatientProfileId(patientId)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));

        DoctorProfile doctorProfile = doctorUser.getDoctorProfile();
        PatientProfile patientProfile = patientUser.getPatientProfile();

        if (!doctorProfile.getAssignedPatients().contains(patientProfile)) {
            doctorProfile.getAssignedPatients().add(patientProfile);
            userRepository.save(doctorUser);
        }

        return ResponseEntity.ok("Patient assigned successfully");
    }

    @GetMapping("/assigned-patients")
    public ResponseEntity<List<PatientProfileDto>> getAssignedPatients() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctorUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Doctor not found"));

        List<PatientProfileDto> patients = doctorUser.getDoctorProfile().getAssignedPatients().stream()
                .map(PatientProfile::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(patients);
    }

    @PostMapping("/patient/{patientId}/examination")
    public ResponseEntity<?> createExamination(@PathVariable Long patientId,
            @RequestBody MedicalExaminationRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctorUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Doctor not found"));

        User patientUser = userRepository.findByPatientProfileId(patientId)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));

        MedicalExamination examination = new MedicalExamination();
        examination.setDoctor(doctorUser.getDoctorProfile());
        examination.setPatient(patientUser.getPatientProfile());
        examination.setDate(request.getDate());
        examination.setDescription(request.getDescription());

        medicalExaminationRepository.save(examination);

        return ResponseEntity.ok("Examination created successfully");
    }

    @PostMapping("/examinations")
    public ResponseEntity<PageResponse<MedicalExaminationResponse>> getExaminations(
            @RequestBody MedicalExaminationsPageRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctorUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Doctor not found"));

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<MedicalExamination> page = medicalExaminationRepository.findByDoctorAndSearch(
                doctorUser.getDoctorProfile(),
                request.getSearch(),
                pageRequest);

        List<MedicalExaminationResponse> items = page.getContent().stream()
                .map(m -> new MedicalExaminationResponse(
                        m.getId(),
                        m.getPatient().getUser().getName(),
                        m.getPatient().getUser().getLastName(),
                        m.getDate(),
                        m.getDescription()))
                .collect(Collectors.toList());

        PageResponse<MedicalExaminationResponse> response = new PageResponse<>();
        response.setItems(items);
        response.setSize(page.getSize());
        response.setCurrent(page.getNumber());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/patient/{patientId}/assignment-status")
    public ResponseEntity<?> getAssignmentStatus(@PathVariable Long patientId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctorUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Doctor not found"));

        User patientUser = userRepository.findByPatientProfileId(patientId)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));

        DoctorProfile doctorProfile = doctorUser.getDoctorProfile();
        PatientProfile patientProfile = patientUser.getPatientProfile();

        boolean isAssigned = doctorProfile.getAssignedPatients().contains(patientProfile);
        long examCount = medicalExaminationRepository.countByDoctorAndPatient(doctorProfile, patientProfile);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("assigned", isAssigned);
        response.put("examCount", examCount);

        return ResponseEntity.ok(response);
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/unassign-patient/{patientId}")
    public ResponseEntity<?> unassignPatient(@PathVariable Long patientId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctorUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Doctor not found"));

        User patientUser = userRepository.findByPatientProfileId(patientId)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));

        DoctorProfile doctorProfile = doctorUser.getDoctorProfile();
        PatientProfile patientProfile = patientUser.getPatientProfile();

        if (doctorProfile.getAssignedPatients().contains(patientProfile)) {
            doctorProfile.getAssignedPatients().remove(patientProfile);
            userRepository.save(doctorUser);
            medicalExaminationRepository.deleteByDoctorAndPatient(doctorProfile, patientProfile);
            return ResponseEntity.ok("Patient unassigned and examinations deleted");
        }

        return ResponseEntity.badRequest().body("Patient was not assigned");
    }
}
