package com.project.www.service.impl;

import com.project.www.dto.RequirementRequest;
import com.project.www.dto.RequirementResponseDto;
import com.project.www.entity.Requirement;
import com.project.www.entity.RequirementItem;
import com.project.www.entity.Vendor;
import com.project.www.repository.RequirementRepository;
import com.project.www.repository.VendorRepository;
import com.project.www.service.EmailService;
import com.project.www.service.RequirementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequirementServiceImpl implements RequirementService {

    private final RequirementRepository requirementRepository;
    private final VendorRepository vendorRepository;
    private final EmailService emailService;

    @Override
    public RequirementResponseDto createRequirement(RequirementRequest request) {
        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        Requirement requirement = Requirement.builder()
                .description(request.getDescription())
                .vendor(vendor)
                .requiredDate(request.getRequiredDate())
                .requirementType(request.getRequirementType())
                .status("CREATED")
                .items(new ArrayList<>())
                .build();

        if (request.getItems() != null) {
            for (var itemReq : request.getItems()) {
                RequirementItem item = RequirementItem.builder()
                        .requirement(requirement)
                        .itemName(itemReq.getItemName())
                        .brand(itemReq.getBrand())
                        .quantity(itemReq.getQuantity())
                        .unit(itemReq.getUnit())
                        .build();
                requirement.getItems().add(item);
            }
        }

        Requirement savedRequirement = requirementRepository.save(requirement);

        // Update status to SENT_TO_VENDOR after creating
        savedRequirement.setStatus("SENT_TO_VENDOR");
        savedRequirement = requirementRepository.save(savedRequirement);

        // Send email to vendor
        String vendorName = vendor.getVendorName() != null ? vendor.getVendorName() : vendor.getCompanyName();
        String reqDate = savedRequirement.getRequiredDate() != null ? savedRequirement.getRequiredDate().toString() : "TBD";
        
        StringBuilder itemsList = new StringBuilder();
        if (savedRequirement.getItems() != null && !savedRequirement.getItems().isEmpty()) {
            for (int i = 0; i < savedRequirement.getItems().size(); i++) {
                RequirementItem item = savedRequirement.getItems().get(i);
                itemsList.append(i + 1).append(". ")
                        .append(item.getItemName())
                        .append(" - ").append(item.getBrand() != null ? item.getBrand() : "N/A")
                        .append(" - Qty ").append(item.getQuantity())
                        .append("\n");
            }
        } else {
            itemsList.append("No specific items listed.\n");
        }

        String emailBody = String.format(
                "Dear %s,\n\nA new requirement has been assigned to you.\n\nItems:\n%s\nRequired Date: %s\n\nDescription: %s\n\nPlease review and provide the quotation.\n\nRegards,\nVendorOS",
                vendorName,
                itemsList.toString(),
                reqDate,
                savedRequirement.getDescription() != null ? savedRequirement.getDescription() : ""
        );

        if (vendor.getEmail() != null && !vendor.getEmail().isEmpty()) {
            try {
                emailService.sendEmail(vendor.getEmail(), "New Requirement Assigned", emailBody);
                log.info("Email sent to vendor {} for requirement {}", vendor.getEmail(), savedRequirement.getId());
            } catch (Exception e) {
                log.error("Failed to send email to vendor {}: {}", vendor.getEmail(), e.getMessage());
                // Don't fail the entire transaction just because email failed
            }
        } else {
            log.warn("Vendor {} has no email address, could not send requirement notification.", vendor.getId());
        }

        return mapToDto(savedRequirement);
    }

    @Override
    public RequirementResponseDto updateRequirementStatus(Long id, String status) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requirement not found"));
        requirement.setStatus(status);
        return mapToDto(requirementRepository.save(requirement));
    }

    @Override
    public List<RequirementResponseDto> getAllRequirements() {
        return requirementRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<RequirementResponseDto> getRequirementsByVendor(Long vendorId) {
        return requirementRepository.findByVendorId(vendorId).stream()
                .map(this::mapToDto)
                .toList();
    }
    
    private RequirementResponseDto mapToDto(Requirement req) {
        RequirementResponseDto.VendorDto vendorDto = null;
        if (req.getVendor() != null) {
            vendorDto = RequirementResponseDto.VendorDto.builder()
                    .id(req.getVendor().getId())
                    .vendorName(req.getVendor().getVendorName())
                    .companyName(req.getVendor().getCompanyName())
                    .email(req.getVendor().getEmail())
                    .build();
        }
        
        List<RequirementResponseDto.RequirementItemDto> itemsDto = new ArrayList<>();
        if (req.getItems() != null) {
            itemsDto = req.getItems().stream()
                    .map(item -> RequirementResponseDto.RequirementItemDto.builder()
                            .id(item.getId())
                            .itemName(item.getItemName())
                            .brand(item.getBrand())
                            .quantity(item.getQuantity())
                            .unit(item.getUnit())
                            .build())
                    .toList();
        }

        return RequirementResponseDto.builder()
                .id(req.getId())
                .description(req.getDescription())
                .requirementType(req.getRequirementType())
                .status(req.getStatus())
                .requiredDate(req.getRequiredDate())
                .vendor(vendorDto)
                .items(itemsDto)
                .build();
    }
}
