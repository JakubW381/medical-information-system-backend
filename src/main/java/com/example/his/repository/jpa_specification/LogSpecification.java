package com.example.his.repository.jpa_specification;

import com.example.his.dto.request.LogPageRequest;
import com.example.his.model.logs.Log;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class LogSpecification {

    public static Specification<Log> filterLogs(LogPageRequest dto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (dto.getId() != null) {
                predicates.add(cb.equal(root.get("id"), dto.getId()));
            }

            if (dto.getLogType() != null && !dto.getLogType().isBlank()) {
                predicates.add(cb.equal(root.get("logType"), dto.getLogType()));
            }

            if (dto.getAuthorId() != null) {
                predicates.add(cb.equal(root.get("author").get("id"), dto.getAuthorId()));
            }

            if (dto.getTargetId() != null) {
                predicates.add(cb.equal(root.get("target").get("id"), dto.getTargetId()));
            }

            if (dto.getFrom() != null && dto.getTo() != null) {
                predicates.add(cb.between(root.get("timestamp"), dto.getFrom(), dto.getTo()));
            } else if (dto.getFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), dto.getFrom()));
            } else if (dto.getTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), dto.getTo()));
            }

            if (dto.getSearch() != null && !dto.getSearch().isBlank()) {
                String pattern = "%" + dto.getSearch().toLowerCase() + "%";

                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);

                Predicate authorMatch = cb.like(cb.lower(root.get("author").get("email")), pattern);
                Predicate targetMatch = cb.like(cb.lower(root.get("target").get("email")), pattern);

                predicates.add(cb.or(descMatch, authorMatch, targetMatch));
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
