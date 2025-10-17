package com.example.his.repository.jpa_specification;

import com.example.his.dto.request.DocumentPageRequest;
import com.example.his.model.Document;
import com.example.his.model.user.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class DocumentSpecification {

    public static Specification<Document> filterDocuments(DocumentPageRequest dto, User patient) {
        return (root, query, cb) -> {
            Predicate p = cb.conjunction();

            if (patient != null) {
                p = cb.and(p, cb.equal(root.get("patient").get("id"), patient.getPatientProfile().getId()));
            }

            if (dto.getSearch() != null && !dto.getSearch().isBlank()) {
                String pattern = "%" + dto.getSearch().toLowerCase() + "%";
                Join<Document, String> tagsJoin = root.join("tags", JoinType.LEFT);

                p = cb.and(p, cb.or(
                        cb.like(cb.lower(root.get("filePath")), pattern),
                        cb.like(cb.lower(tagsJoin), pattern)
                ));
            }

            if (dto.getYear() > 0) {
                LocalDateTime start = LocalDateTime.of(dto.getYear(), 1, 1, 0, 0);
                LocalDateTime end = start.plusYears(1);
                p = cb.and(p, cb.between(root.get("dateTime"), start, end));
            }

            return p;
        };
    }
}
