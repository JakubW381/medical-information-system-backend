package com.example.his.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.his.dto.DocumentTNDto;
import com.example.his.dto.request.DocumentPageRequest;
import com.example.his.dto.response.PageResponse;
import com.example.his.model.Document;
import com.example.his.model.user.User;
import com.example.his.repository.DocumentRepository;
import com.example.his.service.search.SearchService;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private SearchService searchService;

    /*
     *       Cloudinary for now
     *       then we can switch to Google Cloud Storage
     *       (free plan for 90 days, allows private signed urls with expiration time)
     *       for private URLs for thumbnails of documents
     * */

    private final Cloudinary cloudinary;

    public DocumentService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public void saveFile(MultipartFile file, User patient, User sender) throws IOException {

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("File must have a name");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
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
                try (PDDocument document = PDDocument.load(file.getInputStream())) {
                    PDFRenderer renderer = new PDFRenderer(document);
                    img = renderer.renderImageWithDPI(0, 150);
                }
                break;

            default:
                System.out.println("Unsupported file format: " + extension);
                break;
        }

        String baseName = originalFilename.replaceAll("\\.[^.]*$", "");
        String publicId = null;

        if (img != null) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(img, "png", os);

            Map uploadResult = cloudinary.uploader().upload(os.toByteArray(),
                    ObjectUtils.asMap(
                            "public_id", "thumbnails/" + baseName + "_" + java.util.UUID.randomUUID(),
                            "resource_type", "image",
                            "type", "private"
                    ));
            publicId = (String) uploadResult.get("public_id");
        }

        String uploadDir = "uploads/documents/" + patient.getPesel() + "/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;
        Path filePath = Paths.get(uploadDir, uniqueFilename);

        // TODO: implement file encryption
        Files.write(filePath, file.getBytes());

        Document documentEntity = new Document();
        documentEntity.setPatient(patient.getPatientProfile());
        documentEntity.setSender(sender);
        documentEntity.setFilePath(filePath.toString());
        documentEntity.setDateTime(LocalDateTime.now());
        documentEntity.setThumbnailPublicId(publicId);

        documentRepository.save(documentEntity);
    }

    public PageResponse<DocumentTNDto> generateDtos(PageResponse<Document> documents){
        List<DocumentTNDto> dtos = new ArrayList<>();
        for (Document doc : documents.getItems()){
            String signedUrl = cloudinary.url()
                    .resourceType("image")
                    .type("private")
                    .signed(true)
                    .generate(doc.getThumbnailPublicId());
            DocumentTNDto dto = doc.toDto(signedUrl);
            dtos.add(dto);
        }

        PageResponse<DocumentTNDto> dtoPage = new PageResponse<>();
        dtoPage.setItems(dtos);
        dtoPage.setSize(documents.getSize());
        dtoPage.setCurrent(documents.getCurrent());
        dtoPage.setTotalElements(documents.getTotalElements());
        dtoPage.setTotalPages(documents.getTotalPages());

        return dtoPage;
    }

    public PageResponse<Document> documentsByPatient(User patient, DocumentPageRequest pageDto){
        return searchService.documentPaginationSearch(pageDto,patient);
    }
}
