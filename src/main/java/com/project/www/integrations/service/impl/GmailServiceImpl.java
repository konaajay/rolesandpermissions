package com.project.www.integrations.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.project.www.integrations.dto.GmailSendRequest;
import com.project.www.integrations.exception.CredentialMissingException;
import com.project.www.integrations.exception.IntegrationException;
import com.project.www.integrations.service.*;
import com.project.www.integrations.util.JsonUtil;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GmailServiceImpl implements GmailService {

    private static final String GMAIL_SEND_URL = "https://gmail.googleapis.com/gmail/v1/users/me/messages/send";

    private final GoogleOAuthService googleOAuthService;
    private final TenantIntegrationResolver tenantIntegrationResolver;
    private final IntegrationLogService logService;
    private final TenantContextService tenantContextService;

    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public void sendEmail(GmailSendRequest request) {
        String token = googleOAuthService.getValidAccessToken();
        if (token == null || token.isBlank()) {
            throw new CredentialMissingException("Google OAuth token required to send Gmail");
        }

        String raw = "From: me\r\nTo: " + request.getTo() + "\r\nSubject: " + request.getSubject()
                + "\r\n\r\n" + request.getBody();
        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));

        Map<String, String> body = Map.of("raw", encoded);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(GMAIL_SEND_URL, HttpMethod.POST,
                    new HttpEntity<>(body, headers), Map.class);

            var ctx = tenantIntegrationResolver.resolveContext("GOOGLE");
            Long tenantId = tenantContextService.getCurrentTenantId();
            logService.log(tenantId, ctx.getTenantIntegration().getId(), "GOOGLE", "gmail_send", "send",
                    JsonUtil.toJson(request), JsonUtil.toJson(response.getBody()),
                    response.getStatusCode().is2xxSuccessful() ? "SUCCESS" : "FAILED",
                    response.getStatusCode().value(), null, 0);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IntegrationException("Gmail API returned error: " + response.getStatusCode());
            }
        } catch (IntegrationException e) {
            throw e;
        } catch (Exception e) {
            throw new IntegrationException("Failed to send Gmail: " + e.getMessage());
        }
    }
}
