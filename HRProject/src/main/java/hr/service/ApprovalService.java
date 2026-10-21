package hr.service;

import hr.dto.ApprovalRequestDTO;
import hr.entity.ApprovalDocEntity;
import hr.entity.UserEntity;
import hr.repository.ApprovalDocRepository;
import hr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalDocRepository approvalDocRepository;
    private final UserRepository userRepository;

    // 1. 결재 기안서 작성 및 저장
    @Transactional
    public void createDoc(String employeeNo, ApprovalRequestDTO dto) {
        UserEntity user = userRepository.findByEmployeeNo(employeeNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사원입니다."));

        Double deduct = "LEAVE".equals(dto.getDocType()) ? (dto.getDeductDays() != null ? dto.getDeductDays() : 1.0) : 0.0;

        ApprovalDocEntity doc = ApprovalDocEntity.builder()
                .user(user)
                .docType(dto.getDocType())
                .leaveSubType(dto.getLeaveSubType())
                .deductDays(deduct)
                .title(dto.getTitle())
                .reason(dto.getReason())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();

        approvalDocRepository.save(doc);
    }

    // 2. 전체 결재 문서 목록 조회
    @Transactional(readOnly = true)
    public List<ApprovalDocEntity> getAllDocs() {
        return approvalDocRepository.findAll();
    }

    // 3. 결재 간이 승인 처리 (팀장/관리자)
    @Transactional
    public void approveDoc(Long docId) {
        ApprovalDocEntity doc = approvalDocRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문서입니다."));

        // 승인 처리
        doc.updateStatus("APPROVED");

        // 휴가 신청서인 경우 사원의 잔여 연차 자동 차감
        if ("LEAVE".equals(doc.getDocType()) && doc.getDeductDays() > 0) {
            UserEntity applicant = doc.getUser();
            applicant.deductLeave(doc.getDeductDays());
        }
    }
    // 단건 문서 조회
    @Transactional(readOnly = true)
    public ApprovalDocEntity getDocById(Long docId) {
        return approvalDocRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문서입니다. ID: " + docId));
    }
}