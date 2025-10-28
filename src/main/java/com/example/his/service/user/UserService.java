package com.example.his.service.user;

import com.example.his.dto.*;
import com.example.his.dto.request.PatientsPageRequest;
import com.example.his.dto.response.PageResponse;
import com.example.his.model.user.Gender;
import com.example.his.model.user.PatientProfile;
import com.example.his.model.user.Role;
import com.example.his.model.user.User;
import com.example.his.repository.UserRepository;
import com.example.his.service.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SearchService searchService;

    public void updatePatientProfile(User patient, PatientProfileDto dto){
        PatientProfile profile = patient.getPatientProfile();

        profile.setDateOfBirth(dto.getDateOfBirth());
        profile.setGender(Gender.valueOf(dto.getGender()));
        profile.setAddress(dto.getAddress());
        profile.setPhoneNumber(dto.getPhoneNumber());
        profile.setBloodType(dto.getBloodType());
        profile.setAllergies(dto.getAllergies());
        profile.setChronicDiseases(dto.getChronicDiseases());
        profile.setMedications(dto.getMedications());
        profile.setInsuranceNumber(dto.getInsuranceNumber());

        userRepository.save(patient);
    }


    @Cacheable("patientDtoCache")
    public PageResponse<PatientProfileDto> generateDtos(PageResponse<User> users){
        List<PatientProfileDto> dtos = new ArrayList<>();

        PageResponse<PatientProfileDto> dtoPage = new PageResponse<>();
        dtoPage.setItems(dtos);
        dtoPage.setSize(users.getSize());
        dtoPage.setCurrent(users.getCurrent());
        dtoPage.setTotalElements(users.getTotalElements());
        dtoPage.setTotalPages(users.getTotalPages());

        return dtoPage;
    }

    @Cacheable("patientPageCache")
    public PageResponse<User> getPatients(PatientsPageRequest pageDto){
        return searchService.userPaginationSearch(pageDto);
    }
}
