package com.erp_mes.erp.approval.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erp_mes.erp.approval.entity.ApprDetail;

@Repository
public interface ApprDetailRepository extends JpaRepository<ApprDetail, Long> {

	// 연차 사용
	List<ApprDetail> findByApprReqId(Long reqId);

}
