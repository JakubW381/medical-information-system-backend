package com.example.his.service.user;

import com.example.his.config.util.JWTService;
import com.example.his.dto.request.AuthRequestRequest;
import com.example.his.dto.RegisterRequestDto;
import com.example.his.dto.request.DoctorRegisterRequest;
import com.example.his.dto.request.LabRegisterRequest;
import com.example.his.dto.request.PatientRegisterRequest;
import com.example.his.model.user.*;
import com.example.his.repository.UserRepository;
import com.example.his.service.EmailService;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.Cookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private EmailService emailService;


    public Cookie login(AuthRequestRequest dto){

        User user = userRepository.findByEmail(dto.getEmail()).get();
        String jwtToken = jwtService.generateToken(user);

        Cookie cookie = new Cookie("HIS_JWT",jwtToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(36000);
        return cookie;
    }

    // temporary
    public RegisterResponse register(RegisterRequestDto registerRequestDto){
        Optional<User> userOpt = userRepository.findByEmail(registerRequestDto.getEmail());
        if (userOpt.isPresent()){
            return RegisterResponse.EMAIL_EXISTS;
        }
        userOpt = userRepository.findByPesel(registerRequestDto.getPesel());
        if (userOpt.isPresent()){
            return RegisterResponse.PESEL_EXISTS;
        }

        User user = new User();

        user.setEmail(registerRequestDto.getEmail());
        user.setName(registerRequestDto.getName());
        user.setLastName(registerRequestDto.getLastName());
        user.setPesel(registerRequestDto.getPesel());
        user.setPassword(passwordEncoder.encode(registerRequestDto.getPassword()));
        user.setRole(Role.ROLE_USER);

        PatientProfile patientProfile = new PatientProfile();

        patientProfile.setUser(user);
        user.setPatientProfile(patientProfile);

        userRepository.save(user);
        return RegisterResponse.SUCCESS;
    }

    public RegisterResponse registerDoctor(DoctorRegisterRequest doctorRegisterRequest){

        Optional<User> userOpt = userRepository.findByEmail(doctorRegisterRequest.getEmail());
        if (userOpt.isPresent()){
            return RegisterResponse.EMAIL_EXISTS;
        }
        userOpt = userRepository.findByPesel(doctorRegisterRequest.getPesel());
        if (userOpt.isPresent()){
            return RegisterResponse.PESEL_EXISTS;
        }

        User user = new User();

        user.setEmail(doctorRegisterRequest.getEmail());
        user.setName(doctorRegisterRequest.getName());
        user.setLastName(doctorRegisterRequest.getLastName());
        user.setPesel(doctorRegisterRequest.getPesel());

        String generatedPassword = generateTempPassword();
        user.setPassword(passwordEncoder.encode(generatedPassword));
        user.setRole(Role.ROLE_DOCTOR);

        DoctorProfile doctorProfile = new DoctorProfile();
        doctorProfile.setProfessionalLicenseNumber(doctorRegisterRequest.getProfessionalLicenseNumber());
        doctorProfile.setPosition(doctorRegisterRequest.getPosition());
        doctorProfile.setDepartment(doctorRegisterRequest.getDepartment());
        doctorProfile.setSpecialization(doctorRegisterRequest.getSpecialization());
        doctorProfile.setUser(user);
        user.setDoctorProfile(doctorProfile);

        userRepository.save(user);
        emailService.sendRegistrationPassword(doctorRegisterRequest.getEmail(),generatedPassword);
        return RegisterResponse.SUCCESS;
    }

    public RegisterResponse registerPatient(PatientRegisterRequest registerRequestDto){
        Optional<User> userOpt = userRepository.findByEmail(registerRequestDto.getEmail());
        if (userOpt.isPresent()){
            return RegisterResponse.EMAIL_EXISTS;
        }
        userOpt = userRepository.findByPesel(registerRequestDto.getPesel());
        if (userOpt.isPresent()){
            return RegisterResponse.PESEL_EXISTS;
        }

        User user = new User();

        user.setEmail(registerRequestDto.getEmail());
        user.setName(registerRequestDto.getName());
        user.setLastName(registerRequestDto.getLastName());
        user.setPesel(registerRequestDto.getPesel());

        String generatedPassword = generateTempPassword();
        user.setPassword(passwordEncoder.encode(generatedPassword));
        user.setRole(Role.ROLE_USER);

        PatientProfile patientProfile = new PatientProfile();
        patientProfile.setUser(user);

        patientProfile.setInsuranceNumber(registerRequestDto.getInsuranceNumber());
        patientProfile.setMedications(registerRequestDto.getMedications());
        patientProfile.setAllergies(registerRequestDto.getAllergies());
        patientProfile.setAddress(registerRequestDto.getAddress());
        patientProfile.setChronicDiseases(registerRequestDto.getChronicDiseases());
        patientProfile.setBloodType(registerRequestDto.getBloodType());
        patientProfile.setDateOfBirth(registerRequestDto.getDateOfBirth());
        patientProfile.setPhoneNumber(registerRequestDto.getPhoneNumber());
        patientProfile.setGender(Gender.valueOf(registerRequestDto.getGender()));
        user.setPatientProfile(patientProfile);

        userRepository.save(user);
        emailService.sendRegistrationPassword(registerRequestDto.getEmail(), generatedPassword);
        return RegisterResponse.SUCCESS;
    }

    public RegisterResponse registerLab(LabRegisterRequest labRegisterRequest){
        Optional<User> userOpt = userRepository.findByEmail(labRegisterRequest.getEmail());
        if (userOpt.isPresent()){
            return RegisterResponse.EMAIL_EXISTS;
        }

        User user = new User();

        user.setEmail(labRegisterRequest.getEmail());
        user.setName(labRegisterRequest.getName());

        String generatedPassword = generateTempPassword();
        user.setPassword(passwordEncoder.encode(generatedPassword));
        user.setRole(Role.ROLE_LAB);

        PatientProfile patientProfile = new PatientProfile();
        patientProfile.setUser(user);

        userRepository.save(user);
        emailService.sendRegistrationPassword(labRegisterRequest.getEmail(), generatedPassword);
        return RegisterResponse.SUCCESS;
    }

    public String generateTempPassword(){
        PasswordGenerator gen = new PasswordGenerator();

        CharacterRule lower = new CharacterRule(EnglishCharacterData.LowerCase, 2);
        CharacterRule upper = new CharacterRule(EnglishCharacterData.UpperCase, 2);
        CharacterRule digit = new CharacterRule(EnglishCharacterData.Digit, 2);
        CharacterRule special = new CharacterRule(EnglishCharacterData.Special, 2);
        LengthRule lengthRule = new LengthRule(12);

        return gen.generatePassword(lengthRule.getMinimumLength(), Arrays.asList(lower, upper, digit, special));
    }

}
