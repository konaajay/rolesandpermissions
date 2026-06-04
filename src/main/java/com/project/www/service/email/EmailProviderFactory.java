package com.project.www.service.email;

import com.project.www.enums.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailProviderFactory {

    @Autowired
    private ResendEmailProvider resendProvider;

    @Autowired
    private SmtpEmailProvider smtpProvider;

    @Autowired
    private WhatsAppEmailProvider whatsappProvider;

    public EmailProvider getProvider(String channel) {
        if ("EMAIL_RESEND".equals(channel)) {
            return resendProvider;
        }
        if ("WHATSAPP".equals(channel)) {
            return whatsappProvider;
        }
        return smtpProvider;
    }
}
