package com.example.his.service.user;

import com.example.his.dto.UserProfileDto;
import com.example.his.model.user.PatientProfile;
import com.example.his.model.user.User;
import com.example.his.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void updatePatientProfile(User patient, UserProfileDto dto){
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
    }
}
