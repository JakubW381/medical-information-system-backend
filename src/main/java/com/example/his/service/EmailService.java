package com.example.his.service;


import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendRegistrationPassword(String to , String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("projektmedyczny12@gmail.com");
            helper.setTo(to);
            helper.setSubject("MEDICAL SYSTEM PASSWORD");
            helper.setText(password);

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Error sending email", e);
        }
    }
}
