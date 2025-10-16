package com.example.his.controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.his.dto.UserProfileDto;
import com.example.his.model.user.PatientProfile;
import com.example.his.model.user.User;
import com.example.his.repository.UserRepository;
import com.example.his.service.DocumentService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentService documentService;

    private final Cloudinary cloudinary;

    public UserController(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @PostMapping("/update-patient")
    public ResponseEntity<?> updatePatientProfile(
            @RequestBody UserProfileDto dto,
            @AuthenticationPrincipal User patient) {

        PatientProfile profile = patient.getPatientProfile();

        profile.setDateOfBirth(dto.getDateOfBirth());
        profile.setGender(dto.getGender());
        profile.setAddress(dto.getAddress());
        profile.setPhoneNumber(dto.getPhoneNumber());
        profile.setBloodType(dto.getBloodType());
        profile.setAllergies(dto.getAllergies());
        profile.setChronicDiseases(dto.getChronicDiseases());
        profile.setMedications(dto.getMedications());
        profile.setInsuranceNumber(dto.getInsuranceNumber());

        userRepository.save(patient);

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

                String publicId = null;
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
        return ResponseEntity.ok("Files processed.");
    }

    @GetMapping("/gallery")
    public ResponseEntity<?> showGallery(@AuthenticationPrincipal User sender){

    }
}
