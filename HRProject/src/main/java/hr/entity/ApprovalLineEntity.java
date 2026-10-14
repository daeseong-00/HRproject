package hr.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "approval_lines",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_doc_step", columnNames = {"doc_id", "step"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "line_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", nullable = false)
    private ApprovalDocEntity approvalDoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)
    private UserEntity approver;

    @Column(name = "step", nullable = false)
    private Integer step;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "comment_text", length = 500)
    private String commentText;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "WAITING";
    }

    @Builder
    public ApprovalLineEntity(ApprovalDocEntity approvalDoc, UserEntity approver, Integer step) {
        this.approvalDoc = approvalDoc;
        this.approver = approver;
        this.step = step;
        this.status = "WAITING";
    }

    public void processApproval(String status, String commentText) {
        this.status = status;
        this.commentText = commentText;
        this.processedAt = LocalDateTime.now();
    }
}