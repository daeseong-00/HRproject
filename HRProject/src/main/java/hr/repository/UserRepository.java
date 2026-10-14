package hr.repository;

import hr.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // 사번(employeeNo)으로 사원 조회 (로그인 인증 시 필수)
    Optional<UserEntity> findByEmployeeNo(String employeeNo);

    // 사번 중복 여부 확인
    boolean existsByEmployeeNo(String employeeNo);
}