package hr.controller;

import java.time.LocalDate;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import hr.entity.AttendanceEntity;
import hr.entity.UserEntity;
import hr.repository.AttendanceRepository;
import hr.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class IndexController {
	private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            // 1. 로그인한 사번(employeeNo)으로 사원 엔티티 조회
            UserEntity user = userRepository.findByEmployeeNo(userDetails.getUsername())
                    .orElse(null);
            model.addAttribute("loginUser", user);

            // 2. 사원 정보가 존재하면 오늘 날짜 근태 기록 조회 후 모델에 추가
            if (user != null) {
                AttendanceEntity todayAttendance = attendanceRepository
                        .findByUserAndWorkDate(user, LocalDate.now())
                        .orElse(null);
                model.addAttribute("todayAttendance", todayAttendance);
            }
        }
        return "index";
    }
}
