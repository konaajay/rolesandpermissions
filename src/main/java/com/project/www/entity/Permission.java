package com.project.www.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "permissions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tenantId", "permission_key"})
        }
)
public class Permission extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false)
    private String module;

    @Column(nullable = false)
    private String action;

    @Column(name = "permission_key", nullable = false)
    private String permissionKey;

    private String description;

    @Column(nullable = false)
    private Boolean active = true;

    @Builder
    public Permission(Long id, Long tenantId, String module, String action, String description, Boolean active) {
        this.id = id;
        this.tenantId = tenantId;
        this.module = module;
        this.action = action;
        this.description = description;
        this.active = active != null ? active : true;
        generatePermissionKey();
    }

    @PrePersist
    @PreUpdate
    public void generatePermissionKey() {
        if (module != null && action != null) {
            this.permissionKey = (module.trim() + "_" + action.trim()).toUpperCase();
        }
    }

    public void setModule(String module) {
        this.module = module;
        generatePermissionKey();
    }

    public void setAction(String action) {
        this.action = action;
        generatePermissionKey();
    }
}