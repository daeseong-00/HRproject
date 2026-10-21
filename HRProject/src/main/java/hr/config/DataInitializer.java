package hr.config;

import hr.entity.CompanyPolicyEntity;
import hr.entity.DepartmentEntity;
import hr.entity.UserEntity;
import hr.repository.CompanyPolicyRepository;
import hr.repository.DepartmentRepository;
import hr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final CompanyPolicyRepository companyPolicyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. 기본 부서 생성
        DepartmentEntity devDept = departmentRepository.findByDeptName("開発1チーム")
                .orElseGet(() -> departmentRepository.save(
                        DepartmentEntity.builder()
                                .deptName("開発1チーム")
                                .build()
                ));

        // 2. 기본 회사 정책 세팅 (로컬 기본 허용)
        if (companyPolicyRepository.count() == 0) {
            companyPolicyRepository.save(
                    CompanyPolicyEntity.builder()
                            .companyName("Smart HR Corp")
                            .allowedOfficeIp("127.0.0.1, 0:0:0:0:0:0:0:1")
                            .workStartTime("09:00:00")
                            .workEndTime("18:00:00")
                            .lunchBreakMinutes(60)
                            .build()
            );
        }

        // 3. 테스트 사원 생성 (일반 사원: ROLE_USER)
        if (!userRepository.existsByEmployeeNo("20261012")) {
            userRepository.save(
                    UserEntity.builder()
                            .employeeNo("20261012")
                            .password(passwordEncoder.encode("1234"))
                            .name("山田 太郎 (사원)")
                            .email("daeseong@company.com")
                            .position("社員")
                            .role("ROLE_USER")
                            .remainLeaveDays(15.0)
                            .department(devDept)
                            .build()
            );
            System.out.println(">>> [사원 계정 생성] 사번: 20261012 / 비밀번호: 1234 (ROLE_USER)");
        }

        // 4. 테스트 팀장 생성 (승인권자: ROLE_MANAGER)
        if (!userRepository.existsByEmployeeNo("20269999")) {
            userRepository.save(
                    UserEntity.builder()
                            .employeeNo("20269999")
                            .password(passwordEncoder.encode("1234"))
                            .name("佐藤 部長 (팀장)")
                            .email("manager@company.com")
                            .position("チーム長")
                            .role("ROLE_MANAGER")
                            .remainLeaveDays(20.0)
                            .department(devDept)
                            .build()
            );
            System.out.println(">>> [팀장 계정 생성] 사번: 20269999 / 비밀번호: 1234 (ROLE_MANAGER)");
        }
    }
}