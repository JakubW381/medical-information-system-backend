package com.example.his.repository.jpa_specification;

import com.example.his.dto.request.PatientsPageRequest;
import com.example.his.model.user.Gender;
import com.example.his.model.user.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class PatientSpecification {

    public static Specification<User> filterUser(PatientsPageRequest pageDto) {
        return (root, query, criteriaBuilder) -> {
            Predicate p = criteriaBuilder.conjunction();

            if (pageDto.getSearch() != null && !pageDto.getSearch().isBlank()) {
                String pattern = "%" + pageDto.getSearch().toLowerCase() + "%";
                p = criteriaBuilder.and(p, criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), pattern)
                ));
            }

            if (pageDto.getAllergies() != null && !pageDto.getAllergies().isBlank()) {
                String pattern = "%" + pageDto.getAllergies().toLowerCase() + "%";
                p = criteriaBuilder.and(p,
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("allergies")), pattern));
            }

            if (pageDto.getAddress() != null && !pageDto.getAddress().isBlank()) {
                String pattern = "%" + pageDto.getAddress().toLowerCase() + "%";
                p = criteriaBuilder.and(p,
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), pattern));
            }

            if (pageDto.getGender() != null) {
                try {
                    Gender genderEnum = Gender.valueOf(pageDto.getGender().toString().toUpperCase());
                    p = criteriaBuilder.and(p, criteriaBuilder.equal(root.get("gender"), genderEnum));
                } catch (IllegalArgumentException e) {
                    System.out.println("Incorrect gender");
                }
            }

            if (pageDto.getBloodType() != null && !pageDto.getBloodType().isBlank()) {
                String pattern = "%" + pageDto.getBloodType().toLowerCase() + "%";
                p = criteriaBuilder.and(p,
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("bloodType")), pattern));
            }

            if (pageDto.getChronicDiseases() != null && !pageDto.getChronicDiseases().isBlank()) {
                String pattern = "%" + pageDto.getChronicDiseases().toLowerCase() + "%";
                p = criteriaBuilder.and(p,
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("chronicDiseases")), pattern));
            }

            if (pageDto.getMedications() != null && !pageDto.getMedications().isBlank()) {
                String pattern = "%" + pageDto.getMedications().toLowerCase() + "%";
                p = criteriaBuilder.and(p,
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("medications")), pattern));
            }

            if (pageDto.getInsuranceNumber() != null && !pageDto.getInsuranceNumber().isBlank()) {
                String pattern = "%" + pageDto.getInsuranceNumber().toLowerCase() + "%";
                p = criteriaBuilder.and(p,
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("insuranceNumber")), pattern));
            }

            if (pageDto.getDateOfBirth() != null) {
                p = criteriaBuilder.and(p,
                        criteriaBuilder.equal(root.get("patientProfile").get("dateOfBirth"), pageDto.getDateOfBirth()));
            }

            if (pageDto.getPhoneNumber() != null && !pageDto.getPhoneNumber().isBlank()) {
                String pattern = "%" + pageDto.getPhoneNumber().toLowerCase() + "%";
                p = criteriaBuilder.and(p,
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("phoneNumber")), pattern));
            }

            return p;
        };
    }
}
