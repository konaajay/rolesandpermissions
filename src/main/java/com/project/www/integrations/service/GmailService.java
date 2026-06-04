package com.project.www.integrations.service;

import com.project.www.integrations.dto.GmailSendRequest;

public interface GmailService {
    void sendEmail(GmailSendRequest request);
}
