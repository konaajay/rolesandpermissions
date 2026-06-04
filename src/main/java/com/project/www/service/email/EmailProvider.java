package com.project.www.service.email;

import com.project.www.enums.*;

import com.project.www.entity.EmailRecipient;
import com.project.www.dto.EmailProviderResult;

public interface EmailProvider {
    /**
     * Sends an email to a recipient and returns a typed result.
     * No more silent failures or "Status Magic". ✨🛡️✍️
     */
    EmailProviderResult send(EmailRecipient recipient);
}
