package com.erp_mes.erp.approval.entity;

import java.time.LocalDate;

import com.erp_mes.erp.approval.constant.ApprHalfType;
import com.erp_mes.erp.approval.constant.ApprVacType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "approval_detail")
@Getter
@Setter
public class ApprDetail {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "approval_detail_seq_generator")
	@SequenceGenerator(
	    name = "approval_detail_seq_generator",
	    sequenceName = "approval_detail_seq",
	    allocationSize = 1  // DB 시퀀스 increment와 맞춤
	)
	@Column(name = "det_id", updatable = false)
	private Long id;
	
	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	private	LocalDate startDate;
	
	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	private	LocalDate endDate;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private	ApprVacType vacType;
	
	@Enumerated(EnumType.STRING)
	private	ApprHalfType halfType;
		
	@ManyToOne
	@JoinColumn(name = "req_id", nullable = false)
	private	Appr appr;
}
