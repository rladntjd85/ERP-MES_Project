package com.erp_mes.erp.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import org.modelmapper.ModelMapper;

import com.erp_mes.erp.approval.constant.ApprVacType;
import com.erp_mes.erp.attendance.entity.Annual;
import com.erp_mes.erp.personnel.entity.Personnel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AnnualDTO {

	private Long id;   // pk 용
	private String empId; // 사원Id 
	private String annYear; // 사용연도 
	private Double annUse = 0.0; // 사용연차
	private Double annRemain = 0.0; // 잔여연차
	private Double annTotal = 0.0; // 총연차
	private LocalDateTime createdAt; // 등록일
	private LocalDateTime updatedAt; // 수정일
	
	private String empName; // 사원이름
	private String depName; // 부서(공통코드)
	private String empPos; // 직책 (공통코드)
	private String joinDate; // 입사일
	private String annPeriod; // 연차 산정 기간 
	private String annExpire; // 휴가 소멸일
	private String annType; // 연차 & 반차
	
	
	
	// 변환
	public static ModelMapper modelMapper = new ModelMapper();
	
	public Annual toEntity() {
		return modelMapper.map(this, Annual.class);
	}
	
	public static AnnualDTO fromEntity(Annual annual) {
		return modelMapper.map(annual, AnnualDTO.class);
		
	}

	public AnnualDTO(Annual zeroAnn, Personnel personnel) {
        this.empId = zeroAnn.getEmpId();
        this.annYear = zeroAnn.getAnnYear();
        this.annUse = zeroAnn.getAnnUse();
        this.annRemain = zeroAnn.getAnnTotal() - zeroAnn.getAnnUse();
        this.annTotal = zeroAnn.getAnnTotal();
        this.createdAt = zeroAnn.getCreatedAt();
        this.updatedAt = zeroAnn.getUpdatedAt();
        this.empName = personnel.getName();
        this.depName = personnel.getDepartment().getComDtNm();
        this.empPos = personnel.getPosition().getComDtNm();
        this.joinDate = personnel.getJoinDate();
        
        // 연차 산정기간
        LocalDate join = LocalDate.parse(personnel.getJoinDate()); // 입사일
        LocalDate today = LocalDate.now();
        
        int yearsSinceJoin = join.until(today).getYears(); // 근속연수
        LocalDate start = join.plusYears(yearsSinceJoin); // 올해 산정기간 시작일
        LocalDate end = start.plusYears(1).minusDays(1); // 산정기간 종료일 (휴가 소멸일)
        
        this.annPeriod = start + " ~ " + end;
        this.annExpire = end.toString(); // 휴가 소멸일
        
        if (zeroAnn.getAnnUse() % 1 == 0) {
        	this.annType = "연차";       // 연차
        } else {
        	this.annType = "반차";  // 반차
        }
        
        
    }
	
	
}
