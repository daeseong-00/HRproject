package hr.controller;

import hr.dto.AttendanceCheckOutResponseDTO;
import hr.dto.AttendanceResponseDTO;
import hr.entity.UserEntity;
import hr.repository.AttendanceRepository;
import hr.repository.UserRepository;
import hr.service.AttendanceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceApiController {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;

    @PostMapping("/check-in")
    public ResponseEntity<AttendanceResponseDTO> checkIn(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        String clientIp = request.getRemoteAddr();
        AttendanceResponseDTO response = attendanceService.checkIn(userDetails.getUsername(), clientIp);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check-out")
    public ResponseEntity<AttendanceCheckOutResponseDTO> checkOut(
            @AuthenticationPrincipal UserDetails userDetails) {

        AttendanceCheckOutResponseDTO response = attendanceService.checkOut(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-today")
    @Transactional
    public ResponseEntity<String> resetToday(@AuthenticationPrincipal UserDetails userDetails) {
        UserEntity user = userRepository.findByEmployeeNo(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사원입니다."));

        attendanceRepository.findByUserAndWorkDate(user, LocalDate.now())
                .ifPresent(attendanceRepository::delete);

        return ResponseEntity.ok("リセット完了 (초기화 완료)");
    }
}