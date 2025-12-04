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
            String[] firstNames = {"Alice", "Bob", "Charlie", "Mark", "Eve", "David", "Fiona", "George", "Hannah", "Ian",
                    "Julia", "Kevin", "Laura", "Mike", "Nina", "Oscar", "Paula", "Quinn", "Rachel", "Steve"};
            String[] lastNames = {"Brown", "Green", "White", "Wazowski", "Black", "Smith", "Johnson", "Taylor", "Lee", "Walker",
                    "Hall", "Allen", "Young", "King", "Wright", "Scott", "Adams", "Baker", "Carter", "Davis"};
            String[] emails = {"patient1@example.com", "patient2@example.com", "patient3@example.com", "patient4@example.com",
                    "patient5@example.com", "patient6@example.com", "patient7@example.com", "patient8@example.com",
                    "patient9@example.com", "patient10@example.com", "patient11@example.com", "patient12@example.com",
                    "patient13@example.com", "patient14@example.com", "patient15@example.com", "patient16@example.com",
                    "patient17@example.com", "patient18@example.com", "patient19@example.com", "patient20@example.com"};
            String[] pesels = {"12345678901","23456789012","34567890123","45678901234","56789012345","67890123456","78901234567",
                    "89012345678","90123456789","01234567890","11234567890","21234567890","31234567890","41234567890",
                    "51234567890","61234567890","71234567890","81234567890","91234567890","02234567890"};

            for (int i = 0; i < 20; i++) {
                if (userRepository.findByEmail(emails[i]).isEmpty()) {
                    User patient = new User();
                    patient.setEmail(emails[i]);
                    patient.setPassword(passwordEncoder.encode("patient123"));
                    patient.setRole(Role.ROLE_USER);
                    patient.setName(firstNames[i]);
                    patient.setLastName(lastNames[i]);
                    patient.setPesel(pesels[i]);

                    PatientProfile pp = new PatientProfile();
                    pp.setUser(patient);
                    pp.setDateOfBirth(LocalDate.of(1970 + i % 30, (i % 12) + 1, (i % 28) + 1)); // przykładowe daty
                    pp.setGender(i % 2 == 0 ? com.example.his.model.user.Gender.FEMALE : com.example.his.model.user.Gender.MALE);
                    pp.setAddress((100 + i) + " Main St");
                    pp.setPhoneNumber("555-" + (100 + i) + "-" + (100 + i));
                    pp.setBloodType(i % 4 == 0 ? "A+" : i % 4 == 1 ? "B-" : i % 4 == 2 ? "O+" : "AB+");
                    pp.setAllergies(i % 3 == 0 ? "Peanuts" : "");
                    pp.setChronicDiseases(i % 2 == 0 ? "Hypertension" : "Diabetes");
                    pp.setMedications(i % 2 == 0 ? "Lisinopril" : "Metformin");
                    pp.setInsuranceNumber("INS" + (100000 + i));

                    patient.setPatientProfile(pp);
                    userRepository.save(patient);
                }
            }
        };
    }
}
