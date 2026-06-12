package com.project.www.accessmanagement.entity;

import com.project.www.entity.Gender;

import com.project.www.entity.Auditable;

import com.project.www.accessmanagement.entity.Role;

import com.project.www.accessmanagement.entity.Permission;

import com.project.www.tenant.entity.OfficeLocation;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "tenantId", "email" })
})
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private java.util.Set<Role> roles = new java.util.HashSet<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private java.util.Set<Permission> permissions = new java.util.HashSet<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_modules", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "module_name")
    private java.util.Set<String> modules = new java.util.HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_location_id")
    private OfficeLocation assignedOffice;

    @Column(name = "employee_id", length = 100)
    private String employeeId;

    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;

    @Column(name = "joining_date")
    private java.time.LocalDate joiningDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_type_id")
    private com.project.www.tenant.entity.EmployeeType employeeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designation_id")
    private com.project.www.tenant.entity.Designation designation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_mode_id")
    private com.project.www.tenant.entity.WorkMode workMode;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_entities", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "entity_id"))
    private java.util.Set<com.project.www.tenant.entity.BusinessEntity> entities = new java.util.HashSet<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_departments", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "department_id"))
    private java.util.Set<com.project.www.tenant.entity.Department> departments = new java.util.HashSet<>();

    public OfficeLocation getAssignedOffice() {
        return this.assignedOffice;
    }



    public String getName() {
        return getFirstName() + " " + getLastName();
    }

    public User getManager() {
        return null;
    }

    public User getSupervisor() {
        return null;
    }
}
