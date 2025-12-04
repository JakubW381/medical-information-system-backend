package com.example.his.repository.jpa_specification;

import com.example.his.dto.request.PatientsPageRequest;
import com.example.his.model.user.Gender;
import com.example.his.model.user.User;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class PatientSpecification {

    public static Specification<User> filterUser(PatientsPageRequest pageDto) {
        return (root, query, cb) -> {
            root.join("patientProfile", JoinType.LEFT);
            query.distinct(true);

            Predicate p = cb.conjunction();

            p = cb.and(p, cb.isNotNull(root.get("patientProfile")));

            if (pageDto.getSearch() != null && !pageDto.getSearch().isBlank()) {
                String pattern = "%" + pageDto.getSearch().toLowerCase() + "%";
                p = cb.and(p, cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("lastName")), pattern)
                ));
            }

            if (pageDto.getAllergies() != null && !pageDto.getAllergies().isBlank()) {
                String pattern = "%" + pageDto.getAllergies().toLowerCase() + "%";
                p = cb.and(p, cb.like(cb.lower(root.get("patientProfile").get("allergies")), pattern));
            }

            if (pageDto.getAddress() != null && !pageDto.getAddress().isBlank()) {
                String pattern = "%" + pageDto.getAddress().toLowerCase() + "%";
                p = cb.and(p, cb.like(cb.lower(root.get("patientProfile").get("address")), pattern));
            }

            if (pageDto.getGender() != null && !pageDto.getGender().isBlank()) {
                try {
                    Gender genderEnum = Gender.valueOf(pageDto.getGender().toUpperCase());
                    p = cb.and(p, cb.equal(root.get("patientProfile").get("gender"), genderEnum));
                } catch (IllegalArgumentException e) {
                    System.out.println("Incorrect gender");
                }
            }

            if (pageDto.getBloodType() != null && !pageDto.getBloodType().isBlank()) {
                String pattern = "%" + pageDto.getBloodType().toLowerCase() + "%";
                p = cb.and(p, cb.like(cb.lower(root.get("patientProfile").get("bloodType")), pattern));
            }

            if (pageDto.getChronicDiseases() != null && !pageDto.getChronicDiseases().isBlank()) {
                String pattern = "%" + pageDto.getChronicDiseases().toLowerCase() + "%";
                p = cb.and(p, cb.like(cb.lower(root.get("patientProfile").get("chronicDiseases")), pattern));
            }

            if (pageDto.getMedications() != null && !pageDto.getMedications().isBlank()) {
                String pattern = "%" + pageDto.getMedications().toLowerCase() + "%";
                p = cb.and(p, cb.like(cb.lower(root.get("patientProfile").get("medications")), pattern));
            }

            if (pageDto.getInsuranceNumber() != null && !pageDto.getInsuranceNumber().isBlank()) {
                String pattern = "%" + pageDto.getInsuranceNumber().toLowerCase() + "%";
                p = cb.and(p, cb.like(cb.lower(root.get("patientProfile").get("insuranceNumber")), pattern));
            }

            if (pageDto.getDateOfBirth() != null && !pageDto.getDateOfBirth().isBlank()) {
                try {
                    String[] parts = pageDto.getDateOfBirth().split("-");
                    if (parts.length == 3) {
                        int year = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]);
                        int day = Integer.parseInt(parts[2]);
                        LocalDate dob = LocalDate.of(year, month, day);
                        p = cb.and(p, cb.equal(root.get("patientProfile").get("dateOfBirth"), dob));
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid dateOfBirth format");
                }
            }

            if (pageDto.getPhoneNumber() != null && !pageDto.getPhoneNumber().isBlank()) {
                String pattern = "%" + pageDto.getPhoneNumber().toLowerCase() + "%";
                p = cb.and(p, cb.like(cb.lower(root.get("patientProfile").get("phoneNumber")), pattern));
            }

            return p;
        };
    }
}
