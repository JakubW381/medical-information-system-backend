package com.example.his.controllers;


import com.example.his.model.Token;
import com.example.his.repository.TokenRepository;
import com.example.his.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private DocumentService documentService;

    @GetMapping("/document/{tokenString}")
    public ResponseEntity<String> getSharedDocument(@PathVariable String tokenString){
        Token token = tokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new IllegalArgumentException("Wrong token"));

        if (LocalDateTime.now().isAfter(token.getExpiration())){
            return new ResponseEntity<>("Token expired", HttpStatus.FORBIDDEN);
        }

        try {
            String base64File = documentService.getFileBase64(token.getDocument());
            return ResponseEntity.ok(base64File);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read file", e);
        }
    }

}
