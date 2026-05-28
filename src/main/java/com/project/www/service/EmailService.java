package com.project.www.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendCredentialsEmail(String toEmail, String firstName, String loginId, String password, String loginUrl, String tenantCode) {
        SimpleMailMessage message = new SimpleMailMessage();
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
}
