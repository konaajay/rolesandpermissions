package com.project.www.service;

import com.project.www.enums.*;
import com.project.www.dto.EmailProviderResult;
import com.project.www.entity.EmailRecipient;
import com.project.www.enums.EmailStatus;
import com.project.www.entity.EmailCampaign;
import com.project.www.entity.Campaign;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@campus.com}")
    private String defaultFromEmail;

    public EmailProviderResult sendCampaignEmail(EmailRecipient recipient) {
        String toEmail = recipient.getEmail();
        EmailCampaign campaign = recipient.getCampaign();
        String subject = (campaign != null) ? campaign.getSubject() : "No Subject";
        String content = (campaign != null) ? campaign.getContent() : "";
        String fromName = (campaign != null && campaign.getFromName() != null && !campaign.getFromName().trim().isEmpty()) ? campaign.getFromName() : "Marketing";
        String fromEmail = (campaign != null && campaign.getFromEmail() != null && !campaign.getFromEmail().trim().isEmpty()) ? campaign.getFromEmail() : defaultFromEmail;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(new InternetAddress(fromEmail, fromName));
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content != null ? content : "", true);

            if (campaign != null && campaign.getReplyTo() != null) {
                helper.setReplyTo(campaign.getReplyTo());
            }

            mailSender.send(message);

            return EmailProviderResult.builder().status(EmailStatus.SENT).success(true).build();

        } catch (Exception e) {
            log.error("SMTP_API_FAIL | To: {} | Error: {}", toEmail, e.getMessage());
            return EmailProviderResult.builder().status(EmailStatus.FAILED).success(false).error(e.getMessage()).build();
        }
    }

    public EmailProviderResult sendCampaignEmail(Campaign campaign, String toEmail) {
        String subject = (campaign != null) ? campaign.getSubject() : "No Subject";
        String content = (campaign != null) ? campaign.getContent() : "";
        String fromName = "Marketing"; // Defaults if not present in Campaign
        String fromEmail = defaultFromEmail;
        
        try {
            log.info("Sending email to: {}", toEmail);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(new InternetAddress(fromEmail, fromName));
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content != null ? content : "", true);

            mailSender.send(message);

            log.info("Email sent successfully to: {}", toEmail);
            return EmailProviderResult.builder().status(EmailStatus.SENT).success(true).build();

        } catch (Exception e) {
            log.error("SMTP_API_FAIL | To: {} | Error: {}", toEmail, e.getMessage());
            return EmailProviderResult.builder().status(EmailStatus.FAILED).success(false).error(e.getMessage()).build();
        }
    }
}
