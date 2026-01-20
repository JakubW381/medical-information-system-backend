package com.example.his.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for EmailService
 * - Tests actual EmailService methods with mocked JavaMailSender
 * - Generates HTML previews of email templates to target/test-email-templates/
 * - Run these tests to preview how templates will look before sending real
 * emails
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private SpringTemplateEngine templateEngine;
    private EmailService emailService;

    private static final String EMAIL_TEMPLATES_OUTPUT_DIR = "target/test-email-templates";

    @BeforeEach
    void setUp() throws IOException {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("email-templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        templateResolver.setCacheable(false);

        templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        templateEngine.setEnableSpringELCompiler(true);

        emailService = new EmailService(mailSender, templateEngine);

        ReflectionTestUtils.setField(emailService, "fromEmail", "test@medicalsystem.com");

        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        Path outputPath = Paths.get(EMAIL_TEMPLATES_OUTPUT_DIR);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        System.out.println("\n===========================================");
        System.out.println("Email Template Preview Generation");
        System.out.println("Output directory: " + outputPath.toAbsolutePath());
        System.out.println("===========================================\n");
    }

    @Test
    @DisplayName("Test EmailService.sendRegistrationPassword() - Default")
    void testRegistrationPasswordTemplate_Default() throws IOException {
        String email = "test@example.com";
        String password = "TempPass123!";
        String name = "Test User";

        emailService.sendRegistrationPassword(email, name, password);

        verify(mailSender, times(1)).send(any(MimeMessage.class));

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("recipientName", name);
        templateModel.put("password", password);
        String htmlContent = processTemplate("registration-password", templateModel);

        assertNotNull(htmlContent);
        assertFalse(htmlContent.isEmpty());
        assertTrue(htmlContent.contains(password));
        assertTrue(htmlContent.contains(name));

        String filename = "registration-password-default.html";
        saveHtmlToFile(filename, htmlContent);

        System.out.println("✓ Generated: " + filename);
        System.out.println("  Email: " + email);
        System.out.println("  Password: " + password);
    }

    @Test
    @DisplayName("Test EmailService.sendRegistrationPassword() - Custom")
    void testRegistrationPasswordTemplate_Custom() throws IOException {
        String email = "patient@hospital.com";
        String password = "SecurePass456";
        String name = "Patient Name";

        emailService.sendRegistrationPassword(email, name, password);

        verify(mailSender, times(1)).send(any(MimeMessage.class));

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("recipientName", name);
        templateModel.put("password", password);
        String htmlContent = processTemplate("registration-password", templateModel);

        assertNotNull(htmlContent);
        assertTrue(htmlContent.contains(password));
        assertTrue(htmlContent.contains(name));

        String filename = "registration-password-custom.html";
        saveHtmlToFile(filename, htmlContent);

        System.out.println("✓ Generated: " + filename);
        System.out.println("  Email: " + email);
        System.out.println("  Password: " + password);
    }

    @Test
    @DisplayName("Test EmailService.sendMail() - Default")
    void testGeneralEmailTemplate_Default() throws IOException {
        String email = "test@example.com";
        String subject = "Test Email";
        String content = "This is a test email message from the Medical Information System. This template can be used for various notifications and communications.";
        String name = "Test User";

        emailService.sendMail(email, name, content, subject);

        verify(mailSender, times(1)).send(any(MimeMessage.class));

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("recipientName", name);
        templateModel.put("content", content);
        templateModel.put("subject", subject);
        templateModel.put("senderName", "Medical Information System Team");
        String htmlContent = processTemplate("general-email", templateModel);

        assertNotNull(htmlContent);
        assertFalse(htmlContent.isEmpty());
        assertTrue(htmlContent.contains(subject));
        assertTrue(htmlContent.contains("This is a test email message"));

        String filename = "general-email-default.html";
        saveHtmlToFile(filename, htmlContent);

        System.out.println("✓ Generated: " + filename);
        System.out.println("  Email: " + email);
        System.out.println("  Subject: " + subject);
    }

    @Test
    @DisplayName("Test EmailService.sendMail() - Custom")
    void testGeneralEmailTemplate_Custom() throws IOException {
        String email = "doctor@hospital.com";
        String subject = "Appointment Reminder";
        String content = "Your appointment is scheduled for tomorrow at 10:00 AM. Please arrive 15 minutes early.";
        String name = "Doctor Name";

        emailService.sendMail(email, name, content, subject);

        verify(mailSender, times(1)).send(any(MimeMessage.class));

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("recipientName", name);
        templateModel.put("content", content);
        templateModel.put("subject", subject);
        templateModel.put("senderName", "Medical Information System Team");
        String htmlContent = processTemplate("general-email", templateModel);

        assertNotNull(htmlContent);
        assertTrue(htmlContent.contains(subject));
        assertTrue(htmlContent.contains("appointment is scheduled"));
        assertTrue(htmlContent.contains(name));

        String filename = "general-email-custom.html";
        saveHtmlToFile(filename, htmlContent);

        System.out.println("✓ Generated: " + filename);
        System.out.println("  Email: " + email);
        System.out.println("  Subject: " + subject);
    }

    @Test
    @DisplayName("Generate All Template Variations")
    void testAllTemplateVariations() throws IOException {
        System.out.println("\n--- Generating All Template Variations ---\n");

        generateAndTestRegistration("jan.kowalski@hospital.com", "Jan Kowalski", "Welcome2024!");
        generateAndTestRegistration("mary.smith@clinic.org", "Mary Smith", "SecureP@ss789");

        generateAndTestGeneralEmail("patient@hospital.com", "Patient Name", "Lab Results Available",
                "Your recent lab test results are now available in the patient portal. Please log in to view them.");
        generateAndTestGeneralEmail("doctor@hospital.com", "Doctor Name", "New Patient Assigned",
                "You have been assigned a new patient: Jan kowalski. Please review their medical history before the first appointment.");

        System.out.println("\n===========================================");
        System.out.println("All templates generated successfully!");
        System.out.println("Open files in browser: file://" + Paths.get(EMAIL_TEMPLATES_OUTPUT_DIR).toAbsolutePath());
        System.out.println("===========================================");
    }

    private void generateAndTestRegistration(String email, String name, String password) throws IOException {
        emailService.sendRegistrationPassword(email, name, password);
        verify(mailSender, atLeastOnce()).send(any(MimeMessage.class));

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("recipientName", name);
        templateModel.put("password", password);

        String htmlContent = processTemplate("registration-password", templateModel);
        String filename = "registration-" + email.replace("@", "-at-").replace(".", "-") + ".html";
        saveHtmlToFile(filename, htmlContent);

        System.out.println("✓ Generated: " + filename);
    }

    private void generateAndTestGeneralEmail(String email, String name, String subject, String content)
            throws IOException {
        emailService.sendMail(email, name, content, subject);
        verify(mailSender, atLeastOnce()).send(any(MimeMessage.class));

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("recipientName", name);
        templateModel.put("content", content);
        templateModel.put("subject", subject);
        templateModel.put("senderName", "Medical Information System Team");

        String htmlContent = processTemplate("general-email", templateModel);
        String filename = "general-" + subject.toLowerCase().replace(" ", "-") + ".html";
        saveHtmlToFile(filename, htmlContent);

        System.out.println("✓ Generated: " + filename);
    }

    private String processTemplate(String templateName, Map<String, Object> templateModel) {
        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);
        return templateEngine.process(templateName, thymeleafContext);
    }

    private void saveHtmlToFile(String filename, String htmlContent) throws IOException {
        Path filePath = Paths.get(EMAIL_TEMPLATES_OUTPUT_DIR, filename);
        try (FileWriter writer = new FileWriter(filePath.toFile(), StandardCharsets.UTF_8)) {
            writer.write(htmlContent);
        }
    }

    private String extractNameFromEmail(String email) {
        if (email != null && email.contains("@")) {
            String localPart = email.substring(0, email.indexOf("@"));
            String name = localPart.replace(".", " ").replace("_", " ");
            String[] parts = name.split(" ");
            StringBuilder result = new StringBuilder();
            for (String part : parts) {
                if (!part.isEmpty()) {
                    result.append(Character.toUpperCase(part.charAt(0)))
                            .append(part.substring(1))
                            .append(" ");
                }
            }
            return result.toString().trim();
        }
        return "User";
    }
}