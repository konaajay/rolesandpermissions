package com.project.www.dto;

import com.project.www.enums.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailSendRequest {
    @NotBlank
    @Email
    private String to;
}
