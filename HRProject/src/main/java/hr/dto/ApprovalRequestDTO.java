package hr.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class ApprovalRequestDTO {
    private String docType;       // REMOTE_WORK(재택), LEAVE(휴가)
    private String leaveSubType;  // ANNUAL(연차), HALF_AM(오전반차), HALF_PM(오후반차)
    private Double deductDays;    // 차감 일수
    private String title;         // 신청 건명
    private String reason;        // 사유 / 업무 계획
    private LocalDate startDate;  // 시작일
    private LocalDate endDate;    // 종료일
}