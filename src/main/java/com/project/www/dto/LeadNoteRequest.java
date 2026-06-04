package com.project.www.dto;

import com.project.www.enums.*;

import lombok.Data;

@Data
public class LeadNoteRequest {
    private String note;
    private String createdBy;
}
