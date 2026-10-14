package hr.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyPolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long id;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(name = "allowed_office_ip", nullable = false, length = 50)
    private String allowedOfficeIp;

    @Column(name = "work_start_time", nullable = false, length = 8)
    private String workStartTime;

    @Column(name = "work_end_time", nullable = false, length = 8)
    private String workEndTime;

    @Column(name = "lunch_break_minutes", nullable = false)
    private Integer lunchBreakMinutes;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    @PrePersist
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public CompanyPolicyEntity(String companyName, String allowedOfficeIp, String workStartTime, String workEndTime, Integer lunchBreakMinutes) {
        this.companyName = companyName;
        this.allowedOfficeIp = allowedOfficeIp;
        this.workStartTime = workStartTime;
        this.workEndTime = workEndTime;
        this.lunchBreakMinutes = lunchBreakMinutes;
    }
}