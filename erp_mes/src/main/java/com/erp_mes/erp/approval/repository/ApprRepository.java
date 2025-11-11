package com.erp_mes.erp.approval.repository;

import org.springframework.transaction.annotation.Transactional;

import com.erp_mes.erp.approval.entity.Appr;
import com.erp_mes.erp.attendance.entity.Annual;
import com.erp_mes.erp.personnel.entity.Personnel;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprRepository extends JpaRepository<Appr,Long> {
	
	// 내가 기안한 문서 조회
	@Query(value = """
		SELECT
		    1 as step_no,
		    a.title,
		    e.emp_name,
		    dept.com_dt_nm,
		    pos.com_dt_nm,
		    a.request_at,
		    a.create_at,
		    a.status,
		    a.req_id,
		    a.req_type,
		    a.emp_id,
		    a.status,
		    CASE 
		        WHEN EXISTS (SELECT 1 FROM approval_line al 
		                    WHERE al.req_id = a.req_id 
		                    AND al.decision = 'DENY') 
		        THEN 'DENY' 
		        ELSE NULL 
		    END as has_rejection  -- 12번째: 반려 여부
		FROM approval a
		JOIN employee e ON a.emp_id = e.emp_id
		LEFT JOIN common_dt_code dept ON e.emp_dept_id = dept.com_dt_id
		LEFT JOIN common_dt_code pos ON e.emp_position = pos.com_dt_id
		WHERE a.emp_id = :loginId
		ORDER BY a.create_at DESC
		""", nativeQuery = true)
	List<Object[]> findMyDraftedApprovalList(@Param("loginId") String loginId);
	
	// 인사 결재 목록 세부조회
	@Query(value = """
	    SELECT
	        al.step_no,           -- 0
	        a.title,              -- 1
	        e.emp_name,           -- 2
	        dept.com_dt_nm,       
	        pos.com_dt_nm,        
	        a.request_at,         
	        al.dec_date,          
	        al.decision,          
	        a.req_id,             
	        a.req_type,           
	        a.emp_id              -- 10
	    FROM approval_line al
	    JOIN approval a ON al.req_id = a.req_id
	    JOIN employee e ON a.emp_id = e.emp_id
	    LEFT JOIN common_dt_code dept ON e.emp_dept_id = dept.com_dt_id
	    LEFT JOIN common_dt_code pos ON e.emp_position = pos.com_dt_id
	    WHERE a.req_id = :reqId
	    ORDER BY al.step_no ASC
	    """, nativeQuery = true)
	List<Object[]> findApprovalByReqId(@Param("reqId") Long reqId);
	
	// 인사 결재 목록 조회
	@Query(value = """
	    SELECT
	        al.step_no,           -- 0
	        a.title,              -- 1
	        e.emp_name,           -- 2
	        dept.com_dt_nm,       -- 3
	        pos.com_dt_nm,        -- 4
	        a.request_at,         -- 5
	        al.dec_date,          -- 6
	        al.decision,          -- 7
	        a.req_id,             -- 8
	        a.req_type,           -- 9
	        a.emp_id              -- 10
	    FROM approval_line al
	    JOIN approval a ON al.req_id = a.req_id
	    JOIN employee e ON a.emp_id = e.emp_id
	    LEFT JOIN common_dt_code dept ON e.emp_dept_id = dept.com_dt_id
	    LEFT JOIN common_dt_code pos ON e.emp_position = pos.com_dt_id
	    WHERE al.appr_id = :loginId  -- 결재자
	    ORDER BY a.request_at DESC, al.step_no ASC
	    """, nativeQuery = true)
	List<Object[]> findApprovalListWithJoin(@Param("loginId") String loginId);

    // 결재대기 알람
	@Query(value = """
	    SELECT COUNT(*) 
	    FROM approval 
	    WHERE emp_id = :loginId 
	    AND status = 'REQUESTED'
	    """, nativeQuery = true)
	int countMyPendingApprovals(@Param("loginId") String loginId);
	
	// 결재대기 알람
	@Query(value = """
		    SELECT COUNT(*) 
		    FROM approval 
		    WHERE emp_id = :loginId 
		    AND status = :status
		    """, nativeQuery = true)
		int countMyApprovalsByStatus(@Param("loginId") String loginId, @Param("status") String status);
		
	// 승인버튼 누를시 승인처리되게 하기 (결재목록에서 대기 -> 승인으로 바뀜, 데이터도 반영)
	@Transactional
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = """
	    UPDATE approval_line 
	    SET decision = :decision, 
	        dec_date = SYSDATE,
	        comments = :comments
	    WHERE req_id = :reqId 
	    AND decision IS NULL
	    """, nativeQuery = true)
	int updateApprovalLineDecision(@Param("reqId") Long reqId, 
	                              @Param("decision") String decision,
	                              @Param("comments") String comments);
    
	// 결재 문서 상태 업데이트 메서드 추가
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE approval 
        SET status = :status,
            update_at = SYSDATE
        WHERE req_id = :reqId
        """, nativeQuery = true)
    int updateApprovalStatus(@Param("reqId") Long reqId, 
                             @Param("status") String status);
    
    // 해당 문서의 모든 결재가 완료되었는지 확인
    @Query(value = """
        SELECT COUNT(*) 
        FROM approval_line 
        WHERE req_id = :reqId 
        AND decision = 'PENDING'
        """, nativeQuery = true)
    int countPendingApprovals(@Param("reqId") Long reqId);
    
    // 최소 STEP_NO 조회
    @Query(value = """
        SELECT MIN(step_no) 
        FROM approval_line 
        WHERE req_id = :reqId 
        AND (decision IS NULL OR decision = 'PENDING')
        """, nativeQuery = true)
    Integer findMinPendingStepNo(@Param("reqId") Long reqId);

    // 특정 STEP_NO 업데이트
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE approval_line 
        SET decision = :decision, 
            dec_date = SYSDATE,
            comments = :comments
        WHERE req_id = :reqId 
        AND step_no = :stepNo
        AND (decision IS NULL OR decision = 'PENDING')
        """, nativeQuery = true)
    int updateSpecificApprovalLine(@Param("reqId") Long reqId, 
                                   @Param("stepNo") Integer stepNo,
                                   @Param("decision") String decision,
                                   @Param("comments") String comments);

    //사원 정보 가져오기
    @Query(value = "" +
    	    "SELECT * FROM " +
    	    "employee e " +
    	    "JOIN " +
    	    "common_dt_code d ON e.emp_dept_id = d.com_dt_id " +
    	    "JOIN " +
    	    "common_dt_code p ON e.emp_position = p.com_dt_id " +
    	    "WHERE e.emp_name LIKE '%' || :keyword || '%' and e.emp_id <> :currentEmpId",  // 파라미터로 변경
    	    nativeQuery = true)
	List<Personnel> findByNameContainingIgnoreCase(@Param("keyword") String keyword, 
	                                               @Param("currentEmpId") String currentEmpId);

    //연차신청자 연차 정보 가져오기
    @Query
    ("SELECT a FROM Annual a where a.empId = :empId and a.annYear = :year")
	Optional<Annual> findByAnnual(@Param("empId") String empId, @Param("year") int year);
}
