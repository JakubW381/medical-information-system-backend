package com.example.his.repository;

import com.example.his.model.MedicalExamination;
import com.example.his.model.user.DoctorProfile;
import com.example.his.model.user.PatientProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalExaminationRepository extends JpaRepository<MedicalExamination, Long> {

        @Query("SELECT m FROM MedicalExamination m WHERE m.doctor = :doctor " +
                        "AND (:search IS NULL OR LOWER(m.patient.user.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(m.patient.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
        Page<MedicalExamination> findByDoctorAndSearch(@Param("doctor") DoctorProfile doctor,
                        @Param("search") String search,
                        Pageable pageable);

        long countByDoctorAndPatient(DoctorProfile doctor, PatientProfile patient);

        void deleteByDoctorAndPatient(DoctorProfile doctor, PatientProfile patient);
}
