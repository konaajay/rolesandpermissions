package com.project.www.tenant.dto;

import lombok.Data;

@Data
public class CompanyProfileDTO {
    private Long id;
    private String companyName;
    private String companyCode;
    private String email;
    private String phone;
    private String website;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String pincode;
    private String gstNumber;
    private String panNumber;
    private String registrationNumber;
    private String logoUrl;
    private String faviconUrl;
    private String stampUrl;
    private String signatureUrl;
    private String headerImageUrl;
    private String footerImageUrl;
    private String timezone;
    private String currency;
    private String industryType;
    private String businessType;
}
