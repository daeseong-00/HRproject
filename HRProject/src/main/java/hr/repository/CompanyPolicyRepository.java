package hr.repository;

import hr.entity.CompanyPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyPolicyRepository extends JpaRepository<CompanyPolicyEntity, Long> {
    // 단일 회사 정책 첫 번째 레코드 가져오기 (정책은 보통 1건만 존재)
    default CompanyPolicyEntity getPolicy() {
        return findById(1L).orElse(null);
    }
}