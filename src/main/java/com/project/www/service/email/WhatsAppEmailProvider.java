package com.project.www.service.email;

import com.project.www.enums.*;

import com.project.www.entity.EmailRecipient;
import com.project.www.enums.EmailStatus;
import com.project.www.dto.EmailProviderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppEmailProvider implements EmailProvider {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppEmailProvider.class);

    @Override
    public EmailProviderResult send(EmailRecipient recipient) {
        log.warn("WhatsApp messaging is currently disabled or not implemented.");
        return EmailProviderResult.builder()
                .success(false)
                .status(EmailStatus.FAILED)
                .error("WhatsApp provider service not available")
                .build();
    }
}
