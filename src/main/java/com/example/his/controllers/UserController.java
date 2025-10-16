package com.example.his.controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.his.dto.DocumentDto;
import com.example.his.dto.UserProfileDto;
import com.example.his.model.Document;
import com.example.his.model.user.PatientProfile;
import com.example.his.model.user.User;
import com.example.his.repository.UserRepository;
import com.example.his.service.DocumentService;
import com.example.his.service.user.UserService;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentService documentService;

    private final Cloudinary cloudinary;

    public UserController(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @PostMapping("/update-patient")
    public ResponseEntity<?> updatePatientProfile(@RequestBody UserProfileDto dto, @AuthenticationPrincipal User patient) {
        userService.updatePatientProfile(patient,dto);
        return ResponseEntity.ok("Patient profile updated");
    }


    @GetMapping("/patient")
    public ResponseEntity<?> getPatientProfile(@AuthenticationPrincipal User patient){
        return ResponseEntity.ok(patient.getPatientProfile().toDto());
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile[] files,
                                         @AuthenticationPrincipal User sender) {

        User patient = userRepository.findById(sender.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));

        for (MultipartFile file : files) {
            try {
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename
                        .substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

                String publicId;
                BufferedImage img = null;

                switch (extension) {
                    case "jpg":
                    case "jpeg":
                    case "png":
                        img = Thumbnails.of(file.getInputStream())
                                .size(200, 200)
                                .asBufferedImage();
                        break;

                    case "pdf":
                        PDDocument document = PDDocument.load(file.getInputStream());
                        PDFRenderer renderer = new PDFRenderer(document);
                        img = renderer.renderImageWithDPI(0, 150);
                        document.close();
                        break;

                    default:
                        System.out.println("Wrong file format: " + extension);
                }

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(img, "png", os);

                Map uploadResult = cloudinary.uploader().upload(os.toByteArray(),
                        ObjectUtils.asMap(
                                "public_id", "thumbnails/" + originalFilename,
                                "resource_type", "image",
                                "type", "private"
                        ));
                publicId = (String) uploadResult.get("public_id");

                documentService.saveFile(file, sender, sender,publicId);

            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error with file processing: " + file.getOriginalFilename());
            }
        }
        return ResponseEntity.ok("Files processed");
    }

    @GetMapping("/gallery")
    public ResponseEntity<List<DocumentDto>> showGallery(@AuthenticationPrincipal User patient){
        List<Document> documents = documentService.documentsByPatient(patient);
        List<DocumentDto> dtos = new ArrayList<>();
        for (Document doc : documents){
            String signedUrl = cloudinary.url()
                    .resourceType("image")
                    .type("private")
                    .signed(true)
                    .generate(doc.getThumbnailPublicId());
            DocumentDto dto = doc.toDto(signedUrl);
            dtos.add(dto);
        }
        return ResponseEntity.ok(dtos);
    }
}
