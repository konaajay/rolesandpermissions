package com.project.www.integrations.service;

import org.springframework.web.multipart.MultipartFile;

import com.project.www.integrations.dto.GoogleDriveUploadResponse;

public interface GoogleDriveService {
    GoogleDriveUploadResponse upload(MultipartFile file, String module, Long referenceId);
}
