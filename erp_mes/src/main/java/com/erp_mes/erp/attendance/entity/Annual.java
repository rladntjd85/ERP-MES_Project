package com.erp_mes.erp.attendance.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "ANNUAL",
		uniqueConstraints = @UniqueConstraint(columnNames = {"empId", "annYear"}))
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public class Annual {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;   // pk 용
	
	@Column(nullable = false, length = 20)
	private String empId; // 사원Id 
	
	@Column(nullable = false, length = 4)
	private String annYear; // 사용연도 
	
	@Column
	private Double annUse = 0.0; // 사용연차

	@Column
	private Double annRemain = 0.0; // 잔여연차
	
	@Column
	private Double annTotal = 0.0; // 총연차 
	
	@CreatedDate
	@Column(updatable = false)
	private LocalDateTime createdAt; // 등록일
	 
	@LastModifiedDate
	private LocalDateTime updatedAt; // 수정일
	
	// 연차 개수 설정을 위한 생성자
	public Annual(String empId, String annYear, double annUse, double annTotal) {
		this.empId = empId;
        this.annYear = annYear;
        this.annUse = annUse;
        this.annTotal = annTotal;
        this.annRemain = annTotal - annUse; // 잔여연차 초기화
	}
}
