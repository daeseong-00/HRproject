package hr.repository;

import hr.entity.ApprovalDocEntity;
import hr.entity.ApprovalLineEntity;
import hr.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalLineRepository extends JpaRepository<ApprovalLineEntity, Long> {
    // 특정 문서의 결재 라인 순서대로 조회 (1차 검토 -> 2차 승인 도장 그리기용)
    List<ApprovalLineEntity> findByApprovalDocOrderByStepAsc(ApprovalDocEntity approvalDoc);

    // 특정 결재권자(팀장/관리자)에게 온 '대기(WAITING)' 상태의 결재선 목록 조회
    List<ApprovalLineEntity> findByApproverAndStatusOrderByCreatedAtDesc(UserEntity approver, String status);

    // 특정 문서의 특정 순서(step) 결재 라인 단건 조회
    Optional<ApprovalLineEntity> findByApprovalDocAndStep(ApprovalDocEntity approvalDoc, Integer step);
}