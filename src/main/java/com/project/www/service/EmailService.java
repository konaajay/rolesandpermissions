package com.project.www.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmail(String toEmail, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendCredentialsEmail(String toEmail, String firstName, String loginId, String password, String loginUrl, String tenantCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Your new account credentials");
        message.setText("Hello " + firstName + ",\n\n" +
                "An account has been created for you. Here are your login credentials:\n\n" +
                "Company Code (Tenant Code): " + tenantCode + "\n" +
                "Login ID / Email: " + loginId + "\n" +
                "Password: " + password + "\n\n" +
                "You can log in here: " + loginUrl + "\n\n" +
                "Please change your password after logging in for the first time.\n\n" +
                "Best Regards,\nAdmin Team");

        mailSender.send(message);
    }
    
    public void sendTenantWelcomeEmail(String toEmail, String firstName, String companyName, String customDomain, String password, String loginUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Your ClassX360 Workspace Is Ready");
        
        String urlToUse = (customDomain != null && !customDomain.trim().isEmpty()) 
                ? "https://" + customDomain 
                : loginUrl;

        message.setText("Dear User,\n\n" +
                "Your workspace has been created successfully.\n\n" +
                "Company Name: " + companyName + "\n\n" +
                "Login URL:\n" + urlToUse + "\n\n" +
                "Admin Email:\n" + toEmail + "\n\n" +
                "Temporary Password:\n" + password + "\n\n" +
                "Please log in using the above credentials and change your password after first login.\n\n" +
                "Regards,\nClassX360 Team");
        mailSender.send(message);
    }

    public void sendEmailWithAttachment(String toEmail, String subject, String text, String attachmentFilename, byte[] attachmentData) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(text);

        helper.addAttachment(attachmentFilename, new org.springframework.core.io.ByteArrayResource(attachmentData));

        mailSender.send(message);
    }
}
