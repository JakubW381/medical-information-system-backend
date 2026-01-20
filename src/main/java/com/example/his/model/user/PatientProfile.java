package com.example.his.model.user;

import com.example.his.dto.PatientProfileDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PatientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String address;
    private String phoneNumber;

    private String bloodType;
    private String allergies;
    private String chronicDiseases;
    private String medications;
    private String insuranceNumber;

    public PatientProfileDto toDto() {
        return new PatientProfileDto(
                id,
                user.getName(),
                user.getLastName(),
                user.getPesel(),
                dateOfBirth,
                gender.name(),
                address,
                phoneNumber,
                bloodType,
                allergies,
                chronicDiseases,
                medications,
                insuranceNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PatientProfile))
            return false;
        PatientProfile that = (PatientProfile) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
