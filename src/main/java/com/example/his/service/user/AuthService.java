package com.example.his.service.user;

import com.example.his.config.util.JWTService;
import com.example.his.dto.AuthRequestDto;
import com.example.his.dto.RegisterRequestDto;
import com.example.his.model.user.Role;
import com.example.his.model.user.User;
import com.example.his.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.Cookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTService jwtService;


    public Cookie login(AuthRequestDto dto){

        User user = userRepository.findByEmail(dto.getEmail()).get();
        String jwtToken = jwtService.generateToken(user);

        Cookie cookie = new Cookie("HIS_JWT",jwtToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(36000);
        return cookie;
    }

    public RegisterResponse register(RegisterRequestDto registerRequestDto){
        Optional<User> userOpt = userRepository.findByEmail(registerRequestDto.getEmail());
        if (userOpt.isPresent()){
            return RegisterResponse.EMAIL_EXISTS;
        }
        userOpt = userRepository.findByPesel(registerRequestDto.getPesel());
        if (userOpt.isPresent()){
            return RegisterResponse.PESEL_EXISTS;
        }

        User user = new User();

        user.setEmail(registerRequestDto.getEmail());
        user.setName(registerRequestDto.getName());
        user.setLastName(registerRequestDto.getLastName());
        user.setPesel(registerRequestDto.getPesel());
        user.setPassword(passwordEncoder.encode(registerRequestDto.getPassword()));
        user.setRole(Role.ROLE_USER);

        userRepository.save(user);
        return RegisterResponse.SUCCESS;
    }

}
