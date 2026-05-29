package com.project.www.service;

import com.project.www.dto.CompanyProfileDTO;
import com.project.www.entity.CompanyProfile;
import com.project.www.repository.CompanyProfileRepository;
import com.project.www.repository.TenantRepository;
import com.project.www.entity.Tenant;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyProfileService {

    private final CompanyProfileRepository repository;
    private final TenantRepository tenantRepository;

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
        CompanyProfile profile = repository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Company profile not found. Please save profile first."));
        
        // Mocking file upload: in real scenario, upload to S3/Blob Storage
        String fileUrl = "https://example-storage.com/logos/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        profile.setLogoUrl(fileUrl);
        profile.setUpdatedBy(getCurrentUsername());
        
        return mapToDto(repository.save(profile));
    }

    @Transactional
    public CompanyProfileDTO uploadFavicon(MultipartFile file) {
        Long tenantId = TenantContext.getCurrentTenant();
        CompanyProfile profile = repository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Company profile not found. Please save profile first."));
        
        // Mocking file upload
        String fileUrl = "https://example-storage.com/favicons/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        profile.setFaviconUrl(fileUrl);
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
        dto.setTimezone(entity.getTimezone());
        dto.setCurrency(entity.getCurrency());
        return dto;
    }
}
