package hr.repository;

import hr.entity.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long> {
    // 부서명으로 부서 찾기 (예: "플랫폼개발본부")
    Optional<DepartmentEntity> findByDeptName(String deptName);
}