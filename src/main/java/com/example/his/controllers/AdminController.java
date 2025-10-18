package com.example.his.controllers;

import com.example.his.dto.DoctorProfileDto;
import com.example.his.dto.PatientProfileDto;
import com.example.his.dto.request.DoctorRegisterRequest;
import com.example.his.dto.request.PatientRegisterRequest;
import com.example.his.model.user.DoctorProfile;
import com.example.his.model.user.PatientProfile;
import com.example.his.model.user.Role;
import com.example.his.model.user.User;
import com.example.his.repository.UserRepository;
import com.example.his.service.user.AuthService;
import com.example.his.service.user.RegisterResponse;
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
    private AuthService authService;

    @GetMapping("/test")
    public ResponseEntity<String> test(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("error: No Logged Admin");
        }

        return ResponseEntity.ok("Admin logged");
    }

    @PostMapping("/register-doctor")
    public ResponseEntity<?> registerDoctor(@RequestBody DoctorRegisterRequest doctorRegisterRequest){

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

        if (response == RegisterResponse.SUCCESS){
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Signed up successfully");
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

        user.setPatientProfile(profile);

        userRepository.save(user);

        return ResponseEntity.ok("Patient profile saved successfully");
    }

}
