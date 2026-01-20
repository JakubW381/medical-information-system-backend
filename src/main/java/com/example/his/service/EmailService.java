package com.example.his.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username:projektmedyczny12@gmail.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendRegistrationPassword(String to, String name, String password) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("recipientName", name);
            templateModel.put("password", password);

            String htmlBody = processTemplate("registration-password", templateModel);
            sendHtmlMessage(to, "Medical System - Registration Password", htmlBody);
        } catch (Exception e) {
            throw new RuntimeException("Error sending registration password email", e);
        }
    }

    public void sendMail(String to, String name, String content, String subject) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("recipientName", name);
            templateModel.put("content", content);
            templateModel.put("subject", subject);
            templateModel.put("senderName", "Medical Information System Team");

            String htmlBody = processTemplate("general-email", templateModel);
            sendHtmlMessage(to, subject, htmlBody);
        } catch (Exception e) {
            throw new RuntimeException("Error sending email", e);
        }
    }

    private String processTemplate(String templateName, Map<String, Object> templateModel) {
        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);
        return templateEngine.process(templateName, thymeleafContext);
    }

    private void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }

    private String extractNameFromEmail(String email) {
        if (email != null && email.contains("@")) {
            String localPart = email.substring(0, email.indexOf("@"));
            return localPart.substring(0, 1).toUpperCase() + localPart.substring(1); // Capitalize first letter
        }
        return "User";
    }
}
