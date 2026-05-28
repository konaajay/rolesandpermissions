package com.project.www.entity;

/**
 * Biological/social gender field for the User entity.
 * Kept simple and extensible — add more values as needed by specific modules
 * (e.g. HRMS, Student Management, Hostel allocation).
 */
public enum Gender {
    MALE,
    FEMALE,
    OTHER,
    PREFER_NOT_TO_SAY
}
