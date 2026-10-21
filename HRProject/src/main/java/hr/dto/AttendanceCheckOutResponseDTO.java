package hr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceCheckOutResponseDTO {
    private boolean success;          // 성공 여부
    private String message;           // 알림 메시지
    private String checkOutTime;      // 퇴근 시각 (HH:mm:ss)
    private Integer totalMinutes;     // 총 실근무 시간 (분)
    private Integer overtimeMinutes;  // 초과근무 시간 (분)
    private String formattedWorkTime; // 포맷팅된 시간 (예: "8時間 30分")
}