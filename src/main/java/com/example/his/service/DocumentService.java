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
import com.example.his.service.tags.TaggingService;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReader;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReaderSpi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private SearchService searchService;

    @Autowired
    private TaggingService taggingService;

    @Value("${SECRET_SHA}")
    private String secret;

    private final Cloudinary cloudinary;

    public DocumentService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public void saveFile(MultipartFile file, User patient, User sender) throws Exception {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) throw new IllegalArgumentException("File must have a name");

        List<String> tags = new ArrayList<>();

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        BufferedImage img = null;

        try {
            switch (extension) {
                case "jpg", "jpeg", "png" -> {
                    img = Thumbnails.of(file.getInputStream()).size(200, 200).asBufferedImage();
                    tags = taggingService.tagImage(file);
                }
                case "pdf" -> {
                    try (PDDocument document = PDDocument.load(file.getInputStream())) {
                        PDFRenderer renderer = new PDFRenderer(document);
                        img = renderer.renderImageWithDPI(0, 150);

                        tags = taggingService.tagPdf(file);
                    }
                }
                case "dcm" -> {
                    DicomImageReader reader = new DicomImageReader(new DicomImageReaderSpi());
                    try (ImageInputStream iis = ImageIO.createImageInputStream(file.getInputStream())) {
                        reader.setInput(iis);
                        BufferedImage dcmImage = reader.read(0);
                        if (dcmImage != null) img = Thumbnails.of(dcmImage).size(200, 200).asBufferedImage();

                        String metadata = reader.getStreamMetadata().getAttributes().toString();

                        tags = taggingService.tagDicom(metadata);

                    }finally {
                        reader.dispose();
                    }
                }
                default -> System.out.println("Unsupported file format: " + extension);
            }
        } catch (Exception e) {
            throw new IOException("Error processing file", e);
        }

        String publicId = null;
        if (img != null) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(img, "png", os);
            Map uploadResult = cloudinary.uploader().upload(os.toByteArray(),
                    ObjectUtils.asMap("public_id", "thumbnails/" + originalFilename + "_" + java.util.UUID.randomUUID(),
                            "resource_type", "image",
                            "type", "private"));
            publicId = (String) uploadResult.get("public_id");
        }

        Path uploadDir = Paths.get("uploads/documents", patient.getPesel());
        if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);

        Path filePath = uploadDir.resolve(System.currentTimeMillis() + "_" + originalFilename);
        Files.write(filePath, encryptBytes(file.getBytes()));

        Document documentEntity = new Document();
        documentEntity.setPatient(patient.getPatientProfile());
        documentEntity.setSender(sender);
        documentEntity.setFilePath(filePath.toString());
        documentEntity.setDateTime(LocalDateTime.now());
        documentEntity.setThumbnailPublicId(publicId);
        documentEntity.setTags(tags);
        documentRepository.save(documentEntity);
    }

    public String getFileBase64(Document document) throws IOException {
        try {
            byte[] encrypted = Files.readAllBytes(Paths.get(document.getFilePath()));
            return Base64.getEncoder().encodeToString(decryptBytes(encrypted));
        } catch (Exception e) {
            throw new IOException("Failed to decrypt file", e);
        }
    }

    public PageResponse<DocumentTNDto> generateDtos(PageResponse<Document> documents) {
        List<DocumentTNDto> dtos = new ArrayList<>();
        for (Document doc : documents.getItems()) {
            String signedUrl = cloudinary.url()
                    .resourceType("image")
                    .type("private")
                    .signed(true)
                    .generate(doc.getThumbnailPublicId());
            dtos.add(doc.toDto(signedUrl));
        } PageResponse<DocumentTNDto> dtoPage = new PageResponse<>();
        dtoPage.setItems(dtos);
        dtoPage.setSize(documents.getSize());
        dtoPage.setCurrent(documents.getCurrent());
        dtoPage.setTotalElements(documents.getTotalElements());
        dtoPage.setTotalPages(documents.getTotalPages());
        return dtoPage;
    }

    @Cacheable("documentPageCache")
    public PageResponse<Document> documentsByPatient(User patient, DocumentPageRequest pageDto) {
        return searchService.documentPaginationSearch(pageDto, patient);
    }

    private byte[] encryptBytes(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(getSecretKeyBytes(), "AES"));
        return cipher.doFinal(data);
    }

    private byte[] decryptBytes(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(getSecretKeyBytes(), "AES"));
        return cipher.doFinal(data);
    }

    private byte[] getSecretKeyBytes() {
        byte[] key = new byte[16];
        byte[] secretBytes = secret.getBytes();
        System.arraycopy(secretBytes, 0, key, 0, Math.min(secretBytes.length, 16));
        return key;
    }


}
