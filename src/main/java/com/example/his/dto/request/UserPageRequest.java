package com.example.his.dto.request;

import com.example.his.model.user.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPageRequest {
    private int page;

    private Long id;
    private String name;
    private String lastName;
    private String email;
    private String pesel;
    private Role role;
    private LocalDateTime createdAfter;

    // Sort fields
    private String sortBy;
    private String sortDirection;
}
