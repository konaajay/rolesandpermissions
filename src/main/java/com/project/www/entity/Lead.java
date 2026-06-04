package com.project.www.entity;

import jakarta.persistence.*;
import lombok.*;
import com.project.www.enums.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "marketing_leads", uniqueConstraints = @UniqueConstraint(columnNames = { "email", "batchId" }))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseInterest;
    private Long linkId;

    private String name;
    private String email;
    private String mobile;
    private String phone;
    private Long batchId;
    private Long courseId;

    // Lead source tracking
    private String source;
    private String utmSource;
    private String utmMedium;
    private String utmCampaign;
    private String utmContent;
    private String utmTerm;

    private String ipAddress;

    private LocalDateTime createdAt;

    // Manual accessors to resolve Lombok processing issues
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}
