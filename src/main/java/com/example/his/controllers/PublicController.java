package com.example.his.controllers;

import com.example.his.model.Token;
import com.example.his.repository.TokenRepository;
import com.example.his.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private DocumentService documentService;

    @GetMapping("/document/{tokenString}")
    public ResponseEntity<String> getSharedDocumentPage(@PathVariable String tokenString) {
        Token token = tokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new IllegalArgumentException("Wrong token"));

        if (LocalDateTime.now().isAfter(token.getExpiration())) {
            return new ResponseEntity<>("<h1>Token expired</h1>", HttpStatus.FORBIDDEN);
        }

        String fullPath = token.getDocument().getFilePath();
        String filenameWithTimestamp = Paths.get(fullPath).getFileName().toString();
        int underscoreIndex = filenameWithTimestamp.indexOf("_");
        String filename = (underscoreIndex != -1) ? filenameWithTimestamp.substring(underscoreIndex + 1)
                : filenameWithTimestamp;

        String expiration = token.getExpiration().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Shared Document</title>
                    <style>
                        body { font-family: system-ui, -apple-system, sans-serif; display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100vh; margin: 0; background-color: #f3f4f6; color: #1f2937; }
                        .card { background: white; padding: 2.5rem; border-radius: 1rem; box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1); text-align: center; max-width: 90%%; width: 400px; }
                        h1 { margin-bottom: 1.5rem; font-size: 1.5rem; font-weight: bold; }
                        p { margin: 0.5rem 0; color: #4b5563; }
                        .btn { display: inline-block; padding: 0.75rem 1.5rem; background-color: #10b981; color: white; text-decoration: none; border-radius: 0.5rem; margin-top: 2rem; font-weight: 500; transition: background-color 0.2s; }
                        .btn:hover { background-color: #059669; }
                        .info { background: #f9fafb; padding: 1rem; border-radius: 0.5rem; margin-top: 1rem; border: 1px solid #e5e7eb; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <h1>Shared Medical Document</h1>
                        <div class="info">
                            <p><strong>File:</strong> %s</p>
                            <p><strong>Expires:</strong> %s</p>
                        </div>
                        <a href="/api/public/document/%s/download" class="btn">Download File</a>
                    </div>
                </body>
                </html>
                """
                .formatted(filename, expiration, tokenString);

        return ResponseEntity.ok(html);
    }

    @GetMapping("/document/{tokenString}/download")
    public ResponseEntity<Resource> downloadSharedDocument(@PathVariable String tokenString) {
        Token token = tokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new IllegalArgumentException("Wrong token"));

        if (LocalDateTime.now().isAfter(token.getExpiration())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            byte[] fileBytes = documentService.getFileBytes(token.getDocument());
            ByteArrayResource resource = new ByteArrayResource(fileBytes);

            String fullPath = token.getDocument().getFilePath();
            String filenameWithTimestamp = Paths.get(fullPath).getFileName().toString();
            int underscoreIndex = filenameWithTimestamp.indexOf("_");
            String filename = (underscoreIndex != -1) ? filenameWithTimestamp.substring(underscoreIndex + 1)
                    : filenameWithTimestamp;

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentLength(fileBytes.length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read file", e);
        }
    }

}
