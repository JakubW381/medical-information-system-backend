package com.example.his.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class EmailServiceTest {

    @Autowired
    private SpringTemplateEngine templateEngine;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void testRegistrationPasswordTemplateRendering() {
        // Mock the mail sender to capture but not send
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Create the email service
        EmailService emailService = new EmailService(mailSender, templateEngine);

        // This will render the template but not actually send (mailSender is mocked)
        emailService.sendRegistrationPassword("test@example.com", "TestPass123!");

        // Verify that send was called (but it's mocked, so nothing actually sent)
        verify(mailSender, times(1)).send(any(MimeMessage.class));

        System.out.println("✓ Registration password template rendered successfully");
    }

    @Test
    void testGeneralEmailTemplateRendering() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        EmailService emailService = new EmailService(mailSender, templateEngine);

        emailService.sendMail("test@example.com", "This is test content", "Test Subject");

        verify(mailSender, times(1)).send(any(MimeMessage.class));

        System.out.println("✓ General email template rendered successfully");
    }
}
