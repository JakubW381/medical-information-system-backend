package com.example.his.repository.jpa_specification;

import com.example.his.dto.request.DocumentPageRequest;
import com.example.his.model.Document;
import com.example.his.model.user.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class DocumentSpecification {

    public static Specification<Document> filterDocuments(DocumentPageRequest dto, User patient) {
        return (root, query, cb) -> {
            Predicate p = cb.conjunction();

            if (patient != null) {
                p = cb.and(p, cb.equal(root.get("patient").get("id"), patient.getPatientProfile().getId()));
            }

            if (dto.getSearch() != null && !dto.getSearch().isBlank()) {
                String pattern = "%" + dto.getSearch().toLowerCase() + "%";
                p = cb.and(p, cb.or(
                        cb.like(cb.lower(root.get("filePath")), pattern),
                        cb.like(cb.lower(root.get("tags")), pattern)
                ));
            }

            if (dto.getYear() != 0) {
                p = cb.and(p, cb.equal(cb.function("YEAR", Integer.class, root.get("dateTime")), dto.getYear()));
            }

            return p;
        };
    }
}
