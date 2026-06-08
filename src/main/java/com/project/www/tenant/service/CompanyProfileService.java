package com.project.www.tenant.service;

import com.project.www.tenant.dto.CompanyProfileDTO;
import com.project.www.tenant.entity.CompanyProfile;
import com.project.www.tenant.repository.CompanyProfileRepository;
import com.project.www.tenant.repository.TenantRepository;
import com.project.www.tenant.entity.Tenant;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyProfileService {

    private final CompanyProfileRepository repository;
    private final TenantRepository tenantRepository;

    @org.springframework.beans.factory.annotation.Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;

    private String getCurrentUsername() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        }
        return "system";
    }

    public CompanyProfileDTO getCompanyProfile() {
        Long tenantId = TenantContext.getCurrentTenant();
        CompanyProfile profile = repository.findByTenantId(tenantId).orElse(null);
        
        if (profile == null) {
            String currentCode = TenantContext.getCurrentTenantCode();
            Tenant tenant = null;
            try {
                TenantContext.clear();
                tenant = tenantRepository.findById(tenantId).orElse(null);
            } finally {
                TenantContext.setCurrentTenant(tenantId);
                TenantContext.setCurrentTenantCode(currentCode);
            }
            
            CompanyProfileDTO dto = new CompanyProfileDTO();
            if (tenant != null) {
                dto.setCompanyName(tenant.getName());
                dto.setCompanyCode(tenant.getCode());
                dto.setEmail(tenant.getAdminEmail());
            }
            return dto;
        }
        
        return mapToDto(profile);
    }

    @Transactional
    public CompanyProfileDTO updateCompanyProfile(CompanyProfileDTO req) {
        Long tenantId = TenantContext.getCurrentTenant();
        String username = getCurrentUsername();

        if (req.getCompanyName() == null || req.getCompanyName().trim().isEmpty()) {
            throw new RuntimeException("Company Name is required");
        }

        CompanyProfile profile = repository.findByTenantId(tenantId).orElse(new CompanyProfile());

        if (profile.getId() == null) {
            profile.setTenantId(tenantId);
            profile.setCreatedBy(username);
            
            if (req.getCompanyCode() == null || req.getCompanyCode().trim().isEmpty()) {
                throw new RuntimeException("Company Code is required for initial setup");
            }
            profile.setCompanyCode(req.getCompanyCode().trim().toUpperCase());
        } else {
            profile.setUpdatedBy(username);
        }

        profile.setCompanyName(req.getCompanyName());
        profile.setEmail(req.getEmail());
        profile.setPhone(req.getPhone());
        profile.setWebsite(req.getWebsite());
        profile.setAddressLine1(req.getAddressLine1());
        profile.setAddressLine2(req.getAddressLine2());
        profile.setCity(req.getCity());
        profile.setState(req.getState());
        profile.setCountry(req.getCountry());
        profile.setPincode(req.getPincode());
        
        if (req.getGstNumber() != null && !req.getGstNumber().isBlank()) {
            boolean gstExists = profile.getId() != null 
                ? repository.existsByTenantIdAndGstNumberAndIdNot(tenantId, req.getGstNumber(), profile.getId())
                : repository.existsByTenantIdAndGstNumber(tenantId, req.getGstNumber());
            
            if (gstExists) {
                throw new RuntimeException("GST Number must be unique per tenant");
            }
        }
        profile.setGstNumber(req.getGstNumber());
        profile.setPanNumber(req.getPanNumber());
        profile.setRegistrationNumber(req.getRegistrationNumber());
        profile.setTimezone(req.getTimezone());
        profile.setCurrency(req.getCurrency());

        CompanyProfile saved = repository.save(profile);
        return mapToDto(saved);
    }

    @Transactional
    public CompanyProfileDTO uploadLogo(MultipartFile file) {
        Long tenantId = TenantContext.getCurrentTenant();
        CompanyProfile profile = repository.findByTenantId(tenantId).orElseGet(() -> {
            CompanyProfile newProfile = new CompanyProfile();
            newProfile.setTenantId(tenantId);
            newProfile.setCreatedBy(getCurrentUsername());
            newProfile.setCompanyName("New Company");
            newProfile.setCompanyCode("TEMP_" + System.currentTimeMillis());
            return newProfile;
        });
        
        String fileUrl = saveFileLocally(file, "logos");
        profile.setLogoUrl(fileUrl);
        profile.setUpdatedBy(getCurrentUsername());
        
        return mapToDto(repository.save(profile));
    }

    @Transactional
    public CompanyProfileDTO uploadFavicon(MultipartFile file) {
        Long tenantId = TenantContext.getCurrentTenant();
        CompanyProfile profile = repository.findByTenantId(tenantId).orElseGet(() -> {
            CompanyProfile newProfile = new CompanyProfile();
            newProfile.setTenantId(tenantId);
            newProfile.setCreatedBy(getCurrentUsername());
            newProfile.setCompanyName("New Company");
            newProfile.setCompanyCode("TEMP_" + System.currentTimeMillis());
            return newProfile;
        });
        
        String fileUrl = saveFileLocally(file, "favicons");
        profile.setFaviconUrl(fileUrl);
        profile.setUpdatedBy(getCurrentUsername());
        
        return mapToDto(repository.save(profile));
    }

    @Transactional
    public CompanyProfileDTO uploadStamp(MultipartFile file) {
        Long tenantId = TenantContext.getCurrentTenant();
        CompanyProfile profile = repository.findByTenantId(tenantId).orElseGet(() -> {
            CompanyProfile newProfile = new CompanyProfile();
            newProfile.setTenantId(tenantId);
            newProfile.setCreatedBy(getCurrentUsername());
            newProfile.setCompanyName("New Company");
            newProfile.setCompanyCode("TEMP_" + System.currentTimeMillis());
            return newProfile;
        });
        
        String fileUrl = saveFileLocally(file, "stamps");
        profile.setStampUrl(fileUrl);
        profile.setUpdatedBy(getCurrentUsername());
        
        return mapToDto(repository.save(profile));
    }

    @Transactional
    public CompanyProfileDTO uploadSignature(MultipartFile file) {
        Long tenantId = TenantContext.getCurrentTenant();
        CompanyProfile profile = repository.findByTenantId(tenantId).orElseGet(() -> {
            CompanyProfile newProfile = new CompanyProfile();
            newProfile.setTenantId(tenantId);
            newProfile.setCreatedBy(getCurrentUsername());
            newProfile.setCompanyName("New Company");
            newProfile.setCompanyCode("TEMP_" + System.currentTimeMillis());
            return newProfile;
        });
        
        String fileUrl = saveFileLocally(file, "signatures");
        profile.setSignatureUrl(fileUrl);
        profile.setUpdatedBy(getCurrentUsername());
        
        return mapToDto(repository.save(profile));
    }

    private CompanyProfileDTO mapToDto(CompanyProfile entity) {
        CompanyProfileDTO dto = new CompanyProfileDTO();
        dto.setId(entity.getId());
        dto.setCompanyName(entity.getCompanyName());
        dto.setCompanyCode(entity.getCompanyCode());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setWebsite(entity.getWebsite());
        dto.setAddressLine1(entity.getAddressLine1());
        dto.setAddressLine2(entity.getAddressLine2());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setCountry(entity.getCountry());
        dto.setPincode(entity.getPincode());
        dto.setGstNumber(entity.getGstNumber());
        dto.setPanNumber(entity.getPanNumber());
        dto.setRegistrationNumber(entity.getRegistrationNumber());
        dto.setLogoUrl(entity.getLogoUrl());
        dto.setFaviconUrl(entity.getFaviconUrl());
        dto.setStampUrl(entity.getStampUrl());
        dto.setSignatureUrl(entity.getSignatureUrl());
        dto.setHeaderImageUrl(entity.getHeaderImageUrl());
        dto.setFooterImageUrl(entity.getFooterImageUrl());
        dto.setTimezone(entity.getTimezone());
        dto.setCurrency(entity.getCurrency());
        return dto;
    }

    private String saveFileLocally(MultipartFile file, String folder) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;
            
            Path uploadPath = Paths.get("uploads", folder);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            return backendUrl + "/uploads/" + folder + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
}
