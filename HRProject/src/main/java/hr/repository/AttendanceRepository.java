package hr.repository;

import hr.entity.AttendanceEntity;
import hr.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {
    // 당일 특정 사원의 출근 기록 조회 (오늘 이미 출근했는지 확인할 때 사용)
    Optional<AttendanceEntity> findByUserAndWorkDate(UserEntity user, LocalDate workDate);

    // 특정 사원의 월별/기간별 근태 내역 조회 (출근일 역순 정렬)
    List<AttendanceEntity> findByUserAndWorkDateBetweenOrderByWorkDateDesc(
            UserEntity user, LocalDate startDate, LocalDate endDate);
}