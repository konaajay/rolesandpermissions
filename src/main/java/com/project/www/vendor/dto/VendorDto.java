package com.project.www.vendor.dto;

import com.project.www.vendor.entity.Vendor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorDto {

    private Long id;
    
    @NotBlank(message = "Vendor Code is required")
    private String vendorCode;

    @NotBlank(message = "Vendor Name is required")
    private String vendorName;

    private Long categoryId;
    private String categoryName;

    private String contactPerson;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Mobile Number is required")
    private String mobileNumber;

    private String alternateMobileNumber;
    private String companyName;
    private String gstNumber;
    private String panNumber;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String status;
    private Double rating;
    private Boolean active;
    private String documentUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
