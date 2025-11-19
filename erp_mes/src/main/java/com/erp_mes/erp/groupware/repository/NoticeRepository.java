package com.erp_mes.erp.groupware.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.erp_mes.erp.groupware.entity.Notice;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
	// notType 필드(예: '전체공지', '부서별')를 기준으로 공지사항을 조회
    List<Notice> findByNotType(String notType);

    // 부서 ID와 공지 유형을 기준으로 공지사항을 조회
    @Query("SELECT n FROM Notice n JOIN n.employee e JOIN e.department d WHERE d.comDtId = :empDeptId AND n.notType = :notType")
    List<Notice> findByEmpDeptIdAndNotType(@Param("empDeptId") String empDeptId, @Param("notType") String notType);
}

