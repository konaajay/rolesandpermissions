package com.project.www.service.email;

import com.project.www.marketing.entity.EmailRecipient;

import com.project.www.enums.*;

import com.project.www.marketing.entity.EmailRecipient;
import com.project.www.dto.EmailProviderResult;
import com.project.www.service.ResendEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResendEmailProvider implements EmailProvider {

    @Autowired
    private ResendEmailService resendEmailService;

    @Override
    public EmailProviderResult send(EmailRecipient recipient) {
        return resendEmailService.sendEmailViaResend(recipient);
    }
}
