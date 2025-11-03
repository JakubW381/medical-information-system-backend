package com.example.his.controllers;

import com.example.his.dto.DocumentTNDto;
import com.example.his.dto.request.DocumentPageRequest;
import com.example.his.dto.request.PassChangeRequest;
import com.example.his.dto.response.PageResponse;
import com.example.his.dto.PatientProfileDto;
import com.example.his.model.Document;
import com.example.his.model.Token;
import com.example.his.model.logs.Log;
import com.example.his.model.logs.LogType;
import com.example.his.model.user.User;
import com.example.his.repository.DocumentRepository;
import com.example.his.repository.TokenRepository;
import com.example.his.repository.UserRepository;
import com.example.his.service.DocumentService;
import com.example.his.service.EmailService;
import com.example.his.service.LogService;
import com.example.his.service.user.UserService;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private LogService logService;


    @PostMapping("/update-patient")
    public ResponseEntity<?> updatePatientProfile(@RequestBody PatientProfileDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));
        userService.updatePatientProfile(patient,dto);
        return ResponseEntity.ok("Patient profile updated");
    }

    @GetMapping("/patient")
    public ResponseEntity<?> getPatientProfile(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));
        return ResponseEntity.ok(patient.getPatientProfile().toDto());
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile[] files) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));

        for (MultipartFile file : files) {
            try {
                documentService.saveFile(file, patient, patient);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error with file processing: " + file.getOriginalFilename());
            }
        }
        return ResponseEntity.ok("Files processed");
    }


    @PostMapping("/gallery")
    public ResponseEntity<PageResponse<DocumentTNDto>> showGallery(@RequestBody DocumentPageRequest pageDto){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(pageDto);
        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PageResponse<Document> documents = documentService.documentsByPatient(patient, pageDto);
        PageResponse<DocumentTNDto> dtos = documentService.generateDtos(documents);

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/pass-change")
    public ResponseEntity<?> passChange(@RequestBody PassChangeRequest passChangeRequest){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(passChangeRequest.getNewPass()));

        userRepository.save(user);
        return ResponseEntity.ok("Password Changed");
    }


    @GetMapping("/document/{id}")
    public ResponseEntity<String> getDocument(@PathVariable Long id){

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No document with this Id"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!document.getPatient().getId().equals(user.getPatientProfile().getId())){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            String base64File = documentService.getFileBase64(document);
            return ResponseEntity.ok(base64File);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read file", e);
        }
    }

    @GetMapping("/document/{id}/share")
    public ResponseEntity<?> shareDocument(@PathVariable Long id){
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No document with this Id"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!document.getPatient().getId().equals(user.getPatientProfile().getId())){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Token token = new Token();
        token.setUser(user);
        token.setDocument(document);
        token.setExpiration(LocalDateTime.now().plusDays(1));

        PasswordGenerator gen = new PasswordGenerator();

        CharacterRule lower = new CharacterRule(EnglishCharacterData.LowerCase, 4);
        CharacterRule upper = new CharacterRule(EnglishCharacterData.UpperCase, 4);
        CharacterRule digit = new CharacterRule(EnglishCharacterData.Digit, 8);
        LengthRule lengthRule = new LengthRule(16);

        token.setToken(gen.generatePassword(lengthRule.getMinimumLength(), Arrays.asList(lower, upper, digit)));
        tokenRepository.save(token);

        String domain = "localhost:8181";
        String url = domain + "/api/public/document/" + token.getToken();

        Log log = new Log();

        log.setAuthor(user);
        log.setLogType(LogType.USER_REGISTERED);
        log.setDescription("Patient Registered");
        logService.saveLog(log);

        return ResponseEntity.ok(url);
    }

}
