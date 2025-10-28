package com.example.his.model.user;


import com.example.his.dto.SafeUserDto;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@Table(name = "users")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String lastName;

    @Column(unique = true)
    private String email;
    private String password;
    private String pesel;

    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastLogin;


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private PatientProfile patientProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private DoctorProfile doctorProfile;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public SafeUserDto toSafeUserDto(){
        SafeUserDto safeUserDto = new SafeUserDto();
        safeUserDto.setId(id);
        safeUserDto.setName(name);
        safeUserDto.setLastName(lastName);
        if (getRole() == Role.ROLE_DOCTOR){
            safeUserDto.setPosition(doctorProfile.getPosition());
            safeUserDto.setDepartment(doctorProfile.getDepartment());
            safeUserDto.setSpecialization(doctorProfile.getSpecialization());
        }
        return safeUserDto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode(); // lub Objects.hash(id)
    }
}
