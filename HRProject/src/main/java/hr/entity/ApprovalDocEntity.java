package hr.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "approval_docs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalDocEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "doc_type", nullable = false, length = 30)
    private String docType;

    @Column(name = "leave_sub_type", length = 20)
    private String leaveSubType;

    @Column(name = "deduct_days")
    private Double deductDays;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Lob
    @Column(name = "reason")
    private String reason;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "doc_status", nullable = false, length = 20)
    private String docStatus;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.docStatus == null) this.docStatus = "PENDING";
        if (this.deductDays == null) this.deductDays = 0.0;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public ApprovalDocEntity(UserEntity user, String docType, String leaveSubType,
                             Double deductDays, String title, String reason,
                             LocalDate startDate, LocalDate endDate) {
        this.user = user;
        this.docType = docType;
        this.leaveSubType = leaveSubType;
        this.deductDays = deductDays != null ? deductDays : 0.0;
        this.title = title;
        this.reason = reason;
        this.startDate = startDate;
        this.endDate = endDate;
        this.docStatus = "PENDING";
    }

    public void updateStatus(String docStatus) {
        this.docStatus = docStatus;
    }
}