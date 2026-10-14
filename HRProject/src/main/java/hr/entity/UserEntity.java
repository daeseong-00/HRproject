package hr.entity;



import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    // 사번 (로그인 ID로 활용, 중복 불가)
    @Column(name = "employee_no", nullable = false, unique = true, length = 20)
    private String employeeNo;

    // 암호화된 비밀번호
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "email", length = 100)
    private String email;

    // 직급 (사원, 대리, 과장, 팀장 등)
    @Column(name = "position", nullable = false, length = 30)
    private String position;

    // 권한: ROLE_USER, ROLE_MANAGER, ROLE_ADMIN
    @Column(name = "role", nullable = false, length = 20)
    private String role;

    // 잔여 연차 (반차 0.5일 처리를 위해 Double)
    @Column(name = "remain_leave_days", nullable = false)
    private Double remainLeaveDays;

    // 프로필 이미지 경로 (사원증 카드용)
    @Column(name = "profile_image_url")
    private String profileImageUrl;

    // 부서(Department)와의 N:1 다대일 단방향 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.remainLeaveDays == null) {
            this.remainLeaveDays = 15.0;
        }
        if (this.role == null) {
            this.role = "ROLE_USER";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public UserEntity(String employeeNo, String password, String name, String email,
                String position, String role, Double remainLeaveDays,
                String profileImageUrl, DepartmentEntity department) {
        this.employeeNo = employeeNo;
        this.password = password;
        this.name = name;
        this.email = email;
        this.position = position;
        this.role = role != null ? role : "ROLE_USER";
        this.remainLeaveDays = remainLeaveDays != null ? remainLeaveDays : 15.0;
        this.profileImageUrl = profileImageUrl;
        this.department = department;
    }

    // 연차 차감 편의 비즈니스 메서드 (향후 결재 승인 시 사용)
    public void deductLeave(Double days) {
        if (this.remainLeaveDays < days) {
            throw new IllegalArgumentException("잔여 연차가 부족합니다.");
        }
        this.remainLeaveDays -= days;
    }
}