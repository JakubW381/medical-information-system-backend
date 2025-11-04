package com.example.his.config.util;

import com.example.his.model.user.DoctorProfile;
import com.example.his.model.user.PatientProfile;
import com.example.his.model.user.Role;
import com.example.his.model.user.User;
import com.example.his.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {

            // --- ADMIN ---
            if (userRepository.findByEmail("admin@example.com").isEmpty()) {
                User admin = new User();
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ROLE_ADMIN);
                admin.setName("Admin");
                admin.setLastName("System");
                userRepository.save(admin);
                System.out.println("Admin user created!");
            }

            // --- LAB ---
            if (userRepository.findByEmail("lab@example.com").isEmpty()) {
                User lab = new User();
                lab.setEmail("lab@example.com");
                lab.setPassword(passwordEncoder.encode("lab123"));
                lab.setRole(Role.ROLE_LAB);
                lab.setName("Some Lab Example");
                userRepository.save(lab);
                System.out.println("Lab created!");
            }

            // --- DOCTORS ---
            if (userRepository.findByEmail("dr.john@example.com").isEmpty()) {
                User doctor1 = new User();
                doctor1.setEmail("dr.john@example.com");
                doctor1.setPassword(passwordEncoder.encode("doctor123"));
                doctor1.setRole(Role.ROLE_DOCTOR);
                doctor1.setName("John");
                doctor1.setLastName("Doe");

                DoctorProfile dp1 = new DoctorProfile();
                dp1.setUser(doctor1);
                dp1.setSpecialization("Cardiology");
                dp1.setDepartment("Heart Clinic");
                dp1.setPosition("Senior Doctor");
                dp1.setProfessionalLicenseNumber("DOC123456");

                doctor1.setDoctorProfile(dp1);
                userRepository.save(doctor1);
            }

            if (userRepository.findByEmail("dr.jane@example.com").isEmpty()) {
                User doctor2 = new User();
                doctor2.setEmail("dr.jane@example.com");
                doctor2.setPassword(passwordEncoder.encode("doctor123"));
                doctor2.setRole(Role.ROLE_DOCTOR);
                doctor2.setName("Jane");
                doctor2.setLastName("Smith");

                DoctorProfile dp2 = new DoctorProfile();
                dp2.setUser(doctor2);
                dp2.setSpecialization("Neurology");
                dp2.setDepartment("Neuro Clinic");
                dp2.setPosition("Consultant");
                dp2.setProfessionalLicenseNumber("DOC654321");

                doctor2.setDoctorProfile(dp2);
                userRepository.save(doctor2);
            }

            // --- PATIENTS ---
            if (userRepository.findByEmail("patient1@example.com").isEmpty()) {
                User patient1 = new User();
                patient1.setEmail("patient1@example.com");
                patient1.setPassword(passwordEncoder.encode("patient123"));
                patient1.setRole(Role.ROLE_USER);
                patient1.setName("Alice");
                patient1.setLastName("Brown");
                patient1.setPesel("12345678901");

                PatientProfile pp1 = new PatientProfile();
                pp1.setUser(patient1);
                pp1.setDateOfBirth(LocalDate.of(1985, 3, 12));
                pp1.setGender(com.example.his.model.user.Gender.FEMALE);
                pp1.setAddress("123 Main St");
                pp1.setPhoneNumber("555-123-456");
                pp1.setBloodType("A+");
                pp1.setAllergies("Peanuts");
                pp1.setChronicDiseases("Hypertension");
                pp1.setMedications("Lisinopril");
                pp1.setInsuranceNumber("INS123456");

                patient1.setPatientProfile(pp1);
                userRepository.save(patient1);
            }

            if (userRepository.findByEmail("patient2@example.com").isEmpty()) {
                User patient2 = new User();
                patient2.setEmail("patient2@example.com");
                patient2.setPassword(passwordEncoder.encode("patient123"));
                patient2.setRole(Role.ROLE_USER);
                patient2.setName("Bob");
                patient2.setLastName("Green");
                patient2.setPesel("23456789012");

                PatientProfile pp2 = new PatientProfile();
                pp2.setUser(patient2);
                pp2.setDateOfBirth(LocalDate.of(1990, 7, 22));
                pp2.setGender(com.example.his.model.user.Gender.MALE);
                pp2.setAddress("456 Oak St");
                pp2.setPhoneNumber("555-234-567");
                pp2.setBloodType("B-");
                pp2.setAllergies("");
                pp2.setChronicDiseases("Asthma");
                pp2.setMedications("Albuterol");
                pp2.setInsuranceNumber("INS234567");

                patient2.setPatientProfile(pp2);
                userRepository.save(patient2);
            }

            if (userRepository.findByEmail("patient3@example.com").isEmpty()) {
                User patient3 = new User();
                patient3.setEmail("patient3@example.com");
                patient3.setPassword(passwordEncoder.encode("patient123"));
                patient3.setRole(Role.ROLE_USER);
                patient3.setName("Charlie");
                patient3.setLastName("White");
                patient3.setPesel("34567890123");

                PatientProfile pp3 = new PatientProfile();
                pp3.setUser(patient3);
                pp3.setDateOfBirth(LocalDate.of(1975, 11, 5));
                pp3.setGender(com.example.his.model.user.Gender.MALE);
                pp3.setAddress("789 Pine St");
                pp3.setPhoneNumber("555-345-678");
                pp3.setBloodType("O+");
                pp3.setAllergies("Penicillin");
                pp3.setChronicDiseases("Diabetes");
                pp3.setMedications("Metformin");
                pp3.setInsuranceNumber("INS345678");

                patient3.setPatientProfile(pp3);
                userRepository.save(patient3);
            }


            System.out.println("Sample doctors and patients created.");
        };
    }
}
