package com.example.his.service.tags.implementation;

import com.example.his.service.tags.TaggingService;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GenaiTaggingService implements TaggingService {

    private final Client client = new Client();

    @Override
    public List<String> tagPdf(MultipartFile file) {
        try {
            String prompt = """
                    Analyze this medical document and return ONLY tags
                    as a raw array (example):
                    ["radiology", "CT", "brain", "contrast"]
                    Do NOT include any text or explanation outside raw.
                    """;

            Content content = Content.fromParts(
                    Part.fromText(prompt),
                    Part.fromBytes(file.getBytes(), file.getContentType())
            );

            GenerateContentResponse response =
                    client.models.generateContent("gemini-2.5-flash", content, null);

            String raw = response.text().trim();
            log.info("Gemini PDF tag response: {}", raw);

            return extractTags(raw);

        } catch (IOException e) {
            log.error("Failed to read PDF file", e);
            return List.of("error");
        } catch (Exception e) {
            log.error("Unexpected error during PDF tagging", e);
            return List.of("error");
        }
    }

    @Override
    public List<String> tagImage(MultipartFile file) {
        try {
            String prompt = """
                    Analyze this medical image and return ONLY tags
                    as a raw array (example):
                    ["xray", "fracture", "left_leg"]
                    No explanations, only the raw array.
                    """;

            Content content = Content.fromParts(
                    Part.fromText(prompt),
                    Part.fromBytes(file.getBytes(), file.getContentType())
            );

            GenerateContentResponse response =
                    client.models.generateContent("gemini-2.5-flash", content, null);

            String raw = response.text().trim();
            log.info("Gemini image tag response: {}", raw);

            return extractTags(raw);

        } catch (IOException e) {
            log.error("Failed to read image file", e);
            return List.of("error");
        } catch (Exception e) {
            log.error("Unexpected error during image tagging", e);
            return List.of("error");
        }
    }

    @Override
    public List<String> tagDicom(String metaData) {
        try {
            String prompt = """
                    Analyze this DICOM metadata and return ONLY short tags
                    relevant to the study in raw array format:
                    example: ["CT", "head", "contrast", "axial"]
                    """;

            Content content = Content.fromParts(Part.fromText(prompt + "\n\nMetadata:\n" + metaData));
            GenerateContentResponse response =
                    client.models.generateContent("gemini-2.5-flash", content, null);

            String raw = response.text().trim();
            log.info("Gemini DICOM tag response: {}", raw);

            return extractTags(raw);

        } catch (Exception e) {
            log.error("Error tagging DICOM metadata", e);
            return List.of("error");
        }
    }


    private List<String> extractTags(String raw) {
        List<String> tags = new ArrayList<>();

        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        String text = raw
                .replaceAll("[\\[\\]\\{\\}\"]", "")
                .replaceAll("\\s+", "")
                .replaceAll("`", "")
                .replaceAll("json", "")
                .trim();

        if (text.isBlank()) return List.of();

        for (String tag : text.split(",")) {
            if (!tag.isBlank()) {
                tags.add(tag);
            }
        }

        return tags;
    }
}
