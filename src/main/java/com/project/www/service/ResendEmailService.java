package com.project.www.service;

import com.project.www.marketing.entity.EmailRecipient;

import com.project.www.marketing.entity.EmailCampaign;

import com.project.www.config.MarketingProperties;
import com.project.www.dto.EmailProviderResult;
import com.project.www.marketing.entity.EmailCampaign;
import com.project.www.marketing.entity.EmailRecipient;
import com.project.www.enums.EmailStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
@lombok.extern.slf4j.Slf4j
public class ResendEmailService {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);

    @Autowired
    private MarketingProperties props;

    @Autowired
    private RestTemplate restTemplate;

    public EmailProviderResult sendEmailViaResend(EmailRecipient recipient) {
        if (recipient == null || recipient.getEmail() == null) {
            return EmailProviderResult.builder().status(EmailStatus.FAILED).success(false)
                    .error("Recipient or email missing").build();
        }

        String apiKey = (props.getResend() != null) ? props.getResend().getApiKey() : null;
        if (apiKey == null || apiKey.trim().isBlank() || "null".equals(apiKey)) {
            return EmailProviderResult.builder().status(EmailStatus.FAILED).success(false)
                    .error("Resend API Key not configured").build();
        }

        EmailCampaign campaign = recipient.getCampaign();
        String fromName = (campaign != null && campaign.getFromName() != null) ? campaign.getFromName()
                : "Administrator";
        String fromEmail = (campaign != null && campaign.getFromEmail() != null
                && campaign.getFromEmail().contains("@"))
                        ? campaign.getFromEmail()
                        : props.getResend().getFromEmail();

        String fromHeader = String.format("%s <%s>", fromName, fromEmail);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "from", fromHeader,
                    "to", recipient.getEmail(),
                    "subject", campaign != null ? campaign.getSubject() : "No Subject",
                    "html", campaign != null ? campaign.getContent() : "");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity("https://api.resend.com/emails", request, String.class);

            return EmailProviderResult.builder().status(EmailStatus.SENT).success(true).build();

        } catch (Exception e) {
            log.error("RESEND_API_FAIL | To: {} | Error: {}", recipient.getEmail(), e.getMessage());
            return EmailProviderResult.builder().status(EmailStatus.FAILED).success(false).error(e.getMessage())
                    .build();
        }
    }
}
