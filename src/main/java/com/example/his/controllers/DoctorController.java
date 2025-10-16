package com.example.his.controllers;

import com.example.his.dto.DoctorProfileDto;
import com.example.his.dto.UserProfileDto;
import com.example.his.model.user.DoctorProfile;
import com.example.his.model.user.PatientProfile;
import com.example.his.model.user.User;
import com.example.his.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/doc")
public class DoctorController {


    @Autowired
    private UserRepository userRepository;

    @PostMapping("/update-patient")
    public ResponseEntity<?> updatePatientProfile(
            @RequestBody UserProfileDto dto,
            @AuthenticationPrincipal User doctor) {

        User patient = userRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));

        PatientProfile profile = patient.getPatientProfile();

        profile.setDateOfBirth(dto.getDateOfBirth());
        profile.setGender(dto.getGender());
        profile.setAddress(dto.getAddress());
        profile.setPhoneNumber(dto.getPhoneNumber());
        profile.setBloodType(dto.getBloodType());
        profile.setAllergies(dto.getAllergies());
        profile.setChronicDiseases(dto.getChronicDiseases());
        profile.setMedications(dto.getMedications());
        profile.setInsuranceNumber(dto.getInsuranceNumber());

        userRepository.save(patient);

        return ResponseEntity.ok("Patient profile updated");
    }


    @PostMapping("/update-doctor")
    public ResponseEntity<?> updateDoctorProfile(@RequestBody DoctorProfileDto dto,
                                                 @AuthenticationPrincipal User doctor) {
        DoctorProfile profile = doctor.getDoctorProfile();
        profile.setDepartment(dto.getDepartment());
        profile.setPosition(dto.getPosition());
        profile.setSpecialization(dto.getSpecialization());
        profile.setProfessionalLicenseNumber(dto.getProfessionalLicenseNumber());

        userRepository.save(doctor);
        return ResponseEntity.ok("Doctor profile updated");
    }

}
