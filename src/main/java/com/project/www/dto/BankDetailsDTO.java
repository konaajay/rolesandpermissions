package com.project.www.dto;

import com.project.www.enums.*;

import lombok.Data;

@Data
public class BankDetailsDTO {
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String accountHolderName;
    private String upiId;
}
