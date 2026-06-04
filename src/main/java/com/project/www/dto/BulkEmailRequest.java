package com.project.www.dto;

import com.project.www.enums.*;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class BulkEmailRequest {
    @NotEmpty
    private List<String> recipients;
}
