package com.project.www.marketing.dto;

import com.project.www.marketing.dto.LeadNoteRequest;

import com.project.www.enums.*;

import lombok.Data;

@Data
public class LeadNoteRequest {
    private String note;
    private String createdBy;
}
