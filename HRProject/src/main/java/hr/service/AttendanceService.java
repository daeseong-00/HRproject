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
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사원입니다."));

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 2. 당일 중복 출근 검증
        if (attendanceRepository.findByUserAndWorkDate(user, today).isPresent()) {
            return AttendanceResponseDTO.builder()
                    .success(false)
                    .message("既に本日の出勤打刻が完了しています。(이미 오늘 출근 처리되었습니다.)")
                    .build();
        }

        // 3. 사내 IP 검증 (다중 IP 쉼표 구분 지원 + 로컬호스트 기본 지원)
        CompanyPolicyEntity policy = companyPolicyRepository.getPolicy();
        boolean isOfficeIp = false;

        if (policy != null && policy.getAllowedOfficeIp() != null) {
            String[] allowedIps = policy.getAllowedOfficeIp().split(",");
            isOfficeIp = Arrays.stream(allowedIps)
                    .map(String::trim)
                    .anyMatch(ip -> ip.equals(clientIp) || "127.0.0.1".equals(clientIp) || "0:0:0:0:0:0:0:1".equals(clientIp));
        }

        String workType;

        // [분기 1] 사내망(학교/집 Wi-Fi 또는 등록 IP) 접속 시 -> 본사 출근
        if (isOfficeIp) {
            workType = "OFFICE";
        } 
        // [분기 2] 사외망(핫스팟, 카페 등 외부 IP) 접속 시 -> 사전 재택 승인 검증
        else {
            var approvedRemoteList = approvalDocRepository.findApprovedRemoteWork(user, today);
            
            // 승인된 재택근무 신청서가 없으면 출근 차단
            if (approvedRemoteList.isEmpty()) {
                return AttendanceResponseDTO.builder()
                        .success(false)
                        .message("社内ネットワークではありません。(접속 IP: " + clientIp + ")\n事前承認された在宅勤務申請が存在しないため、出勤できません。\n(사내망이 아닙니다. 사전 승인된 재택근무 내역이 없어 출근이 차단되었습니다.)")
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

        String typeLabel = "REMOTE".equals(workType) ? "テレワーク (재택)" : "オフィス出社 (본사)";
        return AttendanceResponseDTO.builder()
                .success(true)
                .message("出勤打刻が完了しました。\n勤務形態: " + typeLabel)
                .workType(workType)
                .status(status)
                .checkInTime(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .build();
    }

    // 퇴근 메서드 (유지)
    @Transactional
    public AttendanceCheckOutResponseDTO checkOut(String employeeNo) {
        UserEntity user = userRepository.findByEmployeeNo(employeeNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사원입니다."));

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        AttendanceEntity attendance = attendanceRepository.findByUserAndWorkDate(user, today)
                .orElseThrow(() -> new IllegalStateException("本日の出勤記録がありません。"));

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
                .message("退勤打刻が完了しました。")
                .checkOutTime(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .totalMinutes(totalMinutes)
                .overtimeMinutes(overtimeMinutes)
                .formattedWorkTime(formattedWorkTime)
                .build();
    }
}