package com.erp_mes.erp.attendance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.erp_mes.erp.attendance.dto.AnnualDTO;
import com.erp_mes.erp.attendance.entity.Annual;
import com.erp_mes.erp.commonCode.entity.CommonDetailCode;

@Repository
public interface AnnualRepository extends JpaRepository<Annual, Long> {

	
	// 내 연차 조회
	Optional<Annual> findByEmpIdAndAnnYear(String empId, String annYear);
	

	// 전체 사원 조회 + 무한스크롤
	Page<Annual> findByAnnYearOrderByCreatedAtDesc(String annYear, Pageable pageable);

	// 검색(사원번호, 이름, 부서, 직급)
	@Query("SELECT a, p FROM Annual a JOIN Personnel p ON a.empId = p.empId " +
			"WHERE LOWER(a.empId) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
			"OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " + 
			"OR LOWER(p.department.comDtNm) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
			"OR LOWER(p.position.comDtNm) LIKE LOWER(CONCAT('%', :keyword, '%'))")
	List<Object[]> searchAnn(@Param("keyword") String keyword);


	// 권한별 조회목록
	@Query("SELECT a FROM Annual a JOIN Personnel p ON a.empId = p.empId " +
		   "WHERE a.annYear = :annYear AND p.department.comDtId = :deptCode")
	Page<Annual> findByAnnYearAndDepNm(@Param("annYear") String annYear, @Param("deptCode")String deptCode, Pageable pageable);
	Page<Annual> findByAnnYearAndEmpId(@Param("annYear") String annYear, @Param("loginEmpId") String loginEmpId, Pageable pageable);




	
	

}
