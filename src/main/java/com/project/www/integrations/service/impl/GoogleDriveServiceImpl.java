package com.project.www.integrations.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.project.www.integrations.dto.GoogleDriveUploadResponse;
import com.project.www.integrations.exception.CredentialMissingException;
import com.project.www.integrations.exception.IntegrationException;
import com.project.www.integrations.service.*;
import com.project.www.integrations.util.JsonUtil;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleDriveServiceImpl implements GoogleDriveService {

    private final GoogleOAuthService googleOAuthService;
    private final TenantIntegrationResolver tenantIntegrationResolver;
    private final IntegrationLogService logService;
    private final TenantContextService tenantContextService;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public GoogleDriveUploadResponse upload(MultipartFile file, String module, Long referenceId) {
        String token = googleOAuthService.getValidAccessToken();
        if (token == null || token.isBlank()) {
            throw new CredentialMissingException("Google OAuth token required for Drive upload");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("metadata", "{\"name\":\"" + file.getOriginalFilename() + "\"}");
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart",
                    HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

            var ctx = tenantIntegrationResolver.resolveContext("GOOGLE");
            Long tenantId = tenantContextService.getCurrentTenantId();
            logService.log(tenantId, ctx.getTenantIntegration().getId(), "GOOGLE", "drive_upload", "upload",
                    file.getOriginalFilename(), JsonUtil.toJson(response.getBody()),
                    response.getStatusCode().is2xxSuccessful() ? "SUCCESS" : "FAILED",
                    response.getStatusCode().value(), null, 0);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IntegrationException("Google Drive upload failed");
            }

            String fileId = String.valueOf(response.getBody().get("id"));
            return GoogleDriveUploadResponse.builder()
                    .fileId(fileId)
                    .fileUrl("https://drive.google.com/file/d/" + fileId + "/view")
                    .fileName(file.getOriginalFilename())
                    .build();
        } catch (IntegrationException e) {
            throw e;
        } catch (Exception e) {
            throw new IntegrationException("Drive upload failed: " + e.getMessage());
        }
    }
}
