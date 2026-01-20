package com.example.his.controllers;

import com.example.his.dto.DoctorProfileDto;
import com.example.his.dto.request.*;
import com.example.his.dto.response.LogRecordDto;
import com.example.his.dto.response.PageResponse;
import com.example.his.model.logs.Log;
import com.example.his.model.logs.LogType;
import com.example.his.model.user.*;
import com.example.his.repository.UserRepository;
import com.example.his.service.LogService;
import com.example.his.service.user.AuthService;
import com.example.his.service.user.RegisterResponse;
import com.example.his.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private LogService logService;

    @PostMapping("/register-doctor")
    public ResponseEntity<?> registerDoctor(@RequestBody DoctorRegisterRequest doctorRegisterRequest) {

        RegisterResponse response = authService.registerDoctor(doctorRegisterRequest);

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

            User target = userRepository.findByEmail(doctorRegisterRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("unknown user repo error"));

            log.setAuthor(author);
            log.setTarget(target);
            log.setDescription("Patient Registered");
            log.setLogType(LogType.USER_REGISTERED);

            logService.saveLog(log);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Signed up successfully");
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected registration Error");
    }

    @PostMapping("/register-lab")
    public ResponseEntity<?> registerLab(@RequestBody LabRegisterRequest labRegisterRequest) {

        RegisterResponse response = authService.registerLab(labRegisterRequest);

        if (response == RegisterResponse.EMAIL_EXISTS) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("User with this email address already exists");
        }

        if (response == RegisterResponse.SUCCESS) {

            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Log log = new Log();

            User author = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));

            User target = userRepository.findByEmail(labRegisterRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("unknown user repo error"));

            log.setAuthor(author);
            log.setTarget(target);
            log.setDescription("Laboratory Registered");
            log.setLogType(LogType.USER_REGISTERED);

            logService.saveLog(log);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Lab added successfully");
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected registration Error");
    }

    @PostMapping("/user-doctor")
    public ResponseEntity<?> setDoctorProfile(@RequestBody DoctorProfileDto dto) {

        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        DoctorProfile profile = user.getDoctorProfile();
        if (profile == null) {
            profile = new DoctorProfile();
            profile.setUser(user);
        }

        profile.setSpecialization(dto.getSpecialization());
        profile.setPosition(dto.getPosition());
        profile.setProfessionalLicenseNumber(dto.getProfessionalLicenseNumber());
        profile.setDepartment(dto.getDepartment());

        user.setDoctorProfile(profile);
        user.setRole(Role.ROLE_DOCTOR);
        userRepository.save(user);

        return ResponseEntity.ok("Doctor profile saved successfully");
    }

    @PostMapping("/user-patient")
    public ResponseEntity<?> setPatientProfile(@RequestBody PatientRegisterRequest dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        PatientProfile profile = user.getPatientProfile();
        if (profile == null) {
            profile = new PatientProfile();
            profile.setUser(user);
        }

        profile.setDateOfBirth(dto.getDateOfBirth());
        profile.setAddress(dto.getAddress());
        profile.setPhoneNumber(dto.getPhoneNumber());
        profile.setBloodType(dto.getBloodType());
        profile.setAllergies(dto.getAllergies());
        profile.setChronicDiseases(dto.getChronicDiseases());
        profile.setMedications(dto.getMedications());
        profile.setInsuranceNumber(dto.getInsuranceNumber());
        profile.setGender(Gender.valueOf(dto.getGender()));
        user.setPesel(dto.getPesel());

        user.setPatientProfile(profile);

        userRepository.save(user);

        return ResponseEntity.ok("Patient profile saved successfully");
    }

    @PostMapping("/logs")
    public ResponseEntity<PageResponse<LogRecordDto>> getLogs(@RequestBody LogPageRequest logPageRequest) {
        PageResponse<LogRecordDto> response = logService.getLogPage(logPageRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users")
    public ResponseEntity<PageResponse<User>> getUsers(@RequestBody UserPageRequest pageRequest) {
        PageResponse<User> response = userService.getUsers(pageRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }
}
