package com.project.www.tenant.entity;

import com.project.www.entity.Auditable;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tenants")
public class Tenant extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "db_name", nullable = false)
    private String dbName;

    @Column(name = "domain", unique = true)
    private String domain;

    @Column(name = "admin_email")
    private String adminEmail;

    @Column(name = "db_user")
    private String dbUser;

    @Column(name = "db_password")
    private String dbPassword;

    @Column(name = "super_admin_name")
    private String superAdminName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "status")
    private String status; // TRIAL, ACTIVE, EXPIRED

    @Column(name = "subscription_type")
    private String subscriptionType;

    @Column(name = "subscription_start_date")
    private java.time.LocalDate subscriptionStartDate;

    @Column(name = "subscription_end_date")
    private java.time.LocalDate subscriptionEndDate;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}
