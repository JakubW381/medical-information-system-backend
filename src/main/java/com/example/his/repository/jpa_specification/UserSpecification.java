package com.example.his.repository.jpa_specification;

import com.example.his.dto.request.UserPageRequest;
import com.example.his.model.user.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> filterUsers(UserPageRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getId() != null) {
                predicates.add(cb.equal(root.get("id"), request.getId()));
            }

            if (request.getName() != null && !request.getName().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + request.getName().toLowerCase() + "%"));
            }

            if (request.getLastName() != null && !request.getLastName().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("lastName")), "%" + request.getLastName().toLowerCase() + "%"));
            }

            if (request.getEmail() != null && !request.getEmail().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + request.getEmail().toLowerCase() + "%"));
            }

            if (request.getPesel() != null && !request.getPesel().isBlank()) {
                predicates.add(cb.like(root.get("pesel"), "%" + request.getPesel() + "%"));
            }

            if (request.getRole() != null) {
                predicates.add(cb.equal(root.get("role"), request.getRole()));
            }

            if (request.getCreatedAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getCreatedAfter()));
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
