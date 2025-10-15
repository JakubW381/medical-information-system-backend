package com.example.his.controllers;

import com.example.his.config.util.JWTService;
import com.example.his.dto.AuthRequestDto;
import com.example.his.dto.RegisterRequestDto;
import com.example.his.service.user.RegisterResponse;
import com.example.his.service.user.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDto authRequestDto , HttpServletResponse response){
        Authentication authentication = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(authRequestDto.getEmail(),authRequestDto.getPassword()));

        if (authentication.isAuthenticated()){
            response.addCookie(authService.login(authRequestDto));
            return ResponseEntity.ok("Logged In");
        }else{
            return new ResponseEntity<>("Authentication Error", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("HIS_JWT", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok("Logged Out");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto registerRequestDto){
        RegisterResponse response = authService.register(registerRequestDto);
        if (response == RegisterResponse.EMAIL_EXISTS) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("User with this email address already exists");
        }
        if (response == RegisterResponse.PESEL_EXISTS) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("User with this PESEL already exists");
        }
        if (response == RegisterResponse.SUCCESS){
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Signed up successfully");
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected registration Error");
    }
}
