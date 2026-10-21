package hr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponseDTO {
    private boolean success;       // 성공 여부
    private String message;        // 알림 메시지 (한국어 or 일본어)
    private String workType;       // OFFICE, REMOTE
    private String status;         // PRESENT, LATE
    private String checkInTime;    // 출근 시각 (HH:mm:ss)
}