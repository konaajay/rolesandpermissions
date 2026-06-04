package com.project.www.dto;

import com.project.www.enums.*;

import lombok.Data;

@Data
public class CustomerRegisterDTO {
    private String name;
    private String email;
    private String password;
    private String phone;
    private String location;
}
