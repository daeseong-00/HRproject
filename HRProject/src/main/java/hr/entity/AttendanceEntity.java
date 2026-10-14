package hr.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "attendances",
    uniqueConstraints = {
        // 당일 중복 출근 원천 방지 (Oracle UK 제약 매핑)
        @UniqueConstraint(name = "uk_user_work_date", columnNames = {"user_id", "work_date"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long id;

    // 사원과의 N:1 연관관계 (지연 로딩)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // 근무 일자 (YYYY-MM-DD)
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    // 출근 일시
    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    // 퇴근 일시
    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    // 근무 형태: OFFICE(본사 출근), REMOTE(재택근무)
    @Column(name = "work_type", nullable = false, length = 20)
    private String workType;

    // 상태: PRESENT(정상), LATE(지각), EARLY_LEAVE(조퇴)
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    // 일일 실근무 시간 (분 단위)
    @Column(name = "total_minutes")
    private Integer totalMinutes;

    // 초과 근무 시간 (분 단위)
    @Column(name = "overtime_minutes")
    private Integer overtimeMinutes;

    // 출근 당시 접속 IP
    @Column(name = "client_ip", length = 50)
    private String clientIp;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.totalMinutes == null) this.totalMinutes = 0;
        if (this.overtimeMinutes == null) this.overtimeMinutes = 0;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public AttendanceEntity(UserEntity user, LocalDate workDate, LocalDateTime checkInTime,
                            String workType, String status, String clientIp) {
        this.user = user;
        this.workDate = workDate;
        this.checkInTime = checkInTime;
        this.workType = workType;
        this.status = status;
        this.clientIp = clientIp;
        this.totalMinutes = 0;
        this.overtimeMinutes = 0;
    }

    // 퇴근 시 시간 정산 편의 메서드 (변경 감지 활용)
    public void recordCheckOut(LocalDateTime checkOutTime, Integer totalMinutes, Integer overtimeMinutes) {
        this.checkOutTime = checkOutTime;
        this.totalMinutes = totalMinutes;
        this.overtimeMinutes = overtimeMinutes;
    }
}