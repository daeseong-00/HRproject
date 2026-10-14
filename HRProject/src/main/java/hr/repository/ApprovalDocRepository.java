package hr.repository;

import hr.entity.ApprovalDocEntity;
import hr.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ApprovalDocRepository extends JpaRepository<ApprovalDocEntity, Long> {
    // 내가 기안한 결재 목록 조회
    List<ApprovalDocEntity> findByUserOrderByCreatedAtDesc(UserEntity user);

    // [핵심] 오늘 날짜에 최종 승인(APPROVED)된 재택근무 신청서가 존재하는지 검증
    @Query("SELECT d FROM ApprovalDocEntity d " +
           "WHERE d.user = :user " +
           "AND d.docType = 'REMOTE_WORK' " +
           "AND d.docStatus = 'APPROVED' " +
           "AND :today BETWEEN d.startDate AND d.endDate")
    List<ApprovalDocEntity> findApprovedRemoteWork(
            @Param("user") UserEntity user, 
            @Param("today") LocalDate today);
}