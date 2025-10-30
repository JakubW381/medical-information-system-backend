package com.example.his.repository.jpa_specification;

import com.example.his.dto.request.DocumentPageRequest;
import com.example.his.model.Document;
import com.example.his.model.user.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class DocumentSpecification {

    public static Specification<Document> filterDocuments(DocumentPageRequest dto, User patient) {
        return (root, query, cb) -> {
            Predicate p = cb.conjunction();

            if (patient != null) {
                p = cb.and(p, cb.equal(root.get("patient").get("id"), patient.getPatientProfile().getId()));
            }

            if (dto.getSearch() != null && !dto.getSearch().isBlank()) {
                List<String> words = Arrays.stream(dto.getSearch().toLowerCase().split(" ")).toList();
                Join<Document, String> tagsJoin = root.join("tags", JoinType.LEFT);

                for (String word : words) {
                    String pattern = "%" + word.toLowerCase() + "%";

                    Predicate filePathPredicate = cb.like(cb.lower(root.get("filePath")), pattern);

                    Subquery<Long> sub = query.subquery(Long.class);
                    Root<Document> subRoot = sub.from(Document.class);
                    Join<Document, String> join = subRoot.join("tags");
                    sub.select(subRoot.get("id"))
                            .where(cb.like(cb.lower(join), pattern),
                                    cb.equal(subRoot.get("id"), root.get("id")));

                    Predicate tagPredicate = cb.exists(sub);

                    p = cb.and(p, cb.or(filePathPredicate, tagPredicate));
                }


                query.distinct(true);
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
