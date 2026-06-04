package com.project.www.service.email;

import com.project.www.enums.*;

import com.project.www.entity.EmailRecipient;
import com.project.www.dto.EmailProviderResult;
import com.project.www.service.EmailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmtpEmailProvider implements EmailProvider {

    @Autowired
    private EmailSenderService emailSenderService;

    @Override
    public EmailProviderResult send(EmailRecipient recipient) {
        return emailSenderService.sendCampaignEmail(recipient);
    }
}
