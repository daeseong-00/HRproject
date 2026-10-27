package hr.service;

import hr.dto.AttendanceCheckOutResponseDTO;
import hr.dto.AttendanceResponseDTO;
import hr.entity.AttendanceEntity;
import hr.entity.CompanyPolicyEntity;
import hr.entity.UserEntity;
import hr.repository.ApprovalDocRepository;
import hr.repository.AttendanceRepository;
import hr.repository.CompanyPolicyRepository;
import hr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final CompanyPolicyRepository companyPolicyRepository;
    private final ApprovalDocRepository approvalDocRepository;

    @Transactional
    public AttendanceResponseDTO checkIn(String employeeNo, String clientIp) {
        // 1. 사원 정보 조회
        UserEntity user = userRepository.findByEmployeeNo(employeeNo)
                .orElseThrow(() -> new IllegalArgumentException("該当する社員が存在しません。"));

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 2. 당일 중복 출근 검증
        if (attendanceRepository.findByUserAndWorkDate(user, today).isPresent()) {
            return AttendanceResponseDTO.builder()
                    .success(false)
                    .message("本日の出勤打刻は既に完了しています。")
                    .build();
        }

        // 3. 사내 IP 검증
        CompanyPolicyEntity policy = companyPolicyRepository.getPolicy();
        boolean isOfficeIp = false;

     // 사내 IP 검증 복구 (로컬호스트 접속 허용)
        if (policy != null && policy.getAllowedOfficeIp() != null) {
            String[] allowedIps = policy.getAllowedOfficeIp().split(",");
            isOfficeIp = Arrays.stream(allowedIps)
                    .map(String::trim)
                    .anyMatch(ip -> ip.equals(clientIp) || "127.0.0.1".equals(clientIp) || "0:0:0:0:0:0:0:1".equals(clientIp));
        }

        String workType;

        // [분기 1] 사내 IP 일치 -> 본사 출근
        if (isOfficeIp) {
            workType = "OFFICE";
        } 
        // [분기 2] 사외 IP 감지 -> 사전 재택 승인 검증
        else {
            var approvedRemoteList = approvalDocRepository.findApprovedRemoteWork(user, today);
            
            // 승인 내역 부재 시 차단 (순수 비즈니스 일본어로 출력)
            if (approvedRemoteList.isEmpty()) {
                return AttendanceResponseDTO.builder()
                        .success(false)
                        .message("【打刻エラー】社内ネットワークではありません。(IP: " + clientIp + ")\n事前承認された在宅勤務申請が存在しないため、出勤打刻できません。")
                        .build();
            }
            workType = "REMOTE";
        }

        // 4. 지각 여부 판정 (09:00 기준)
        LocalTime standardStartTime = (policy != null) 
                ? LocalTime.parse(policy.getWorkStartTime()) 
                : LocalTime.of(9, 0, 0);

        String status = now.toLocalTime().isAfter(standardStartTime) ? "LATE" : "PRESENT";

        // 5. 엔티티 저장
        AttendanceEntity attendance = AttendanceEntity.builder()
                .user(user)
                .workDate(today)
                .checkInTime(now)
                .workType(workType)
                .status(status)
                .clientIp(clientIp)
                .build();

        attendanceRepository.save(attendance);

        String typeLabel = "REMOTE".equals(workType) ? "テレワーク (在宅勤務)" : "オフィス出社";
        return AttendanceResponseDTO.builder()
                .success(true)
                .message("出勤打刻が正常に完了しました。\n勤務区分: " + typeLabel)
                .workType(workType)
                .status(status)
                .checkInTime(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .build();
    }

    // 퇴근 메서드
    @Transactional
    public AttendanceCheckOutResponseDTO checkOut(String employeeNo) {
        UserEntity user = userRepository.findByEmployeeNo(employeeNo)
                .orElseThrow(() -> new IllegalArgumentException("該当する社員が存在しません。"));

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        AttendanceEntity attendance = attendanceRepository.findByUserAndWorkDate(user, today)
                .orElseThrow(() -> new IllegalStateException("本日の出勤記録が存在しません。"));

        if (attendance.getCheckOutTime() != null) {
            return AttendanceCheckOutResponseDTO.builder()
                    .success(false)
                    .message("既に退勤処理が完了しています。")
                    .build();
        }

        long diffMinutes = java.time.Duration.between(attendance.getCheckInTime(), now).toMinutes();
        CompanyPolicyEntity policy = companyPolicyRepository.getPolicy();
        int lunchBreak = (policy != null) ? policy.getLunchBreakMinutes() : 60;
        int totalMinutes = (int) Math.max(0, diffMinutes - (diffMinutes >= lunchBreak ? lunchBreak : 0));

        int standardMinutes = 480;
        int overtimeMinutes = Math.max(0, totalMinutes - standardMinutes);

        attendance.recordCheckOut(now, totalMinutes, overtimeMinutes);

        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        String formattedWorkTime = String.format("%d時間 %d分", hours, minutes);

        return AttendanceCheckOutResponseDTO.builder()
                .success(true)
                .message("退勤打刻が正常に完了しました。本日もお疲れ様でした。")
                .checkOutTime(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .totalMinutes(totalMinutes)
                .overtimeMinutes(overtimeMinutes)
                .formattedWorkTime(formattedWorkTime)
                .build();
    }
}