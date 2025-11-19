package com.erp_mes.erp.personnel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.erp_mes.erp.personnel.entity.Personnel;

public interface PersonnelRepository extends JpaRepository<Personnel, String> {
	// 부서 ID로 직원 조회
	List<Personnel> findByDepartment_ComDtId(String deptId);
	
	// 로그인할때 department와 level 필드를 함께 조회
	@Query("""
			select p from Personnel p
			left join fetch p.department
			left join fetch p.level
			where p.empId = :empId
			""")
			Optional<Personnel> findByEmpId(@Param("empId") String empId);
	   
}
