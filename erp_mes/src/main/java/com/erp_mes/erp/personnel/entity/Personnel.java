package com.erp_mes.erp.personnel.entity;

import java.sql.Timestamp;

import org.hibernate.annotations.UpdateTimestamp;

import com.erp_mes.erp.commonCode.entity.CommonDetailCode;
import com.erp_mes.erp.commonCode.repository.CommonDetailCodeRepository;
import com.erp_mes.erp.personnel.dto.PersonnelDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Entity
@Table(name = "employee")
public class Personnel {

	// 사원번호
	@Id
	@Column(nullable = false, name = "emp_id")
	private String empId;

	// 이름
	@Column(nullable = false, name = "emp_name")
	private String name; 

	// 사원 비밀번호
	@Column(nullable = false, unique = true, name = "emp_passwd")
	private String passwd;

	// 주민번호
	@Column(nullable = false, name = "emp_resid")
	private String resident;

	// 우편번호
	@Column(nullable = false, name = "emp_addr_num")
	private String addrNum;

	// 주소
	@Column(nullable = false, name = "emp_addr1")
	private String addr1;

	// 상세주소
	@Column(nullable = false, name = "emp_addr2")
	private String addr2;

	// 이메일
	@Column(nullable = false, name = "emp_email")
	private String email;

	// 전화번호
	@Column(nullable = false, name = "emp_phone")
	private String phone;

	// 입사일
	@Column(nullable = false, name = "emp_join_date")
	private String joinDate;

	// 퇴사일
	@Column(name = "emp_resign_date")
	private String resignDate;

	// 수정일
	@UpdateTimestamp
	@Column(nullable = false, name = "update_at")
	private Timestamp update;

//	// 재직상태
//	@Column(nullable = false, name = "emp_status")
//	private String status;

	// 부서
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(nullable = false, name = "emp_dept_id", referencedColumnName = "com_dt_id")
	private CommonDetailCode department;

	// 직급
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false, name = "emp_position", referencedColumnName = "com_dt_id")
	private CommonDetailCode position; // 직책

	
	//추가한 컬럼 보안등급---------------------------
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(nullable = false, name = "emp_level_id", referencedColumnName = "com_dt_id")
	private CommonDetailCode level;
	
	
//	재직현황
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false, name = "emp_status", referencedColumnName = "com_dt_id")
	private CommonDetailCode status;
	
	
	public static Personnel fromDTO(PersonnelDTO peronnelDTO , CommonDetailCodeRepository repo) {		//repo 추가로 사용하기 위함
		
		Personnel personnel = new Personnel();
		personnel.setEmpId(peronnelDTO.getEmpId());
		personnel.setName(peronnelDTO.getName());
		personnel.setPasswd(peronnelDTO.getPasswd());
		personnel.setResident(peronnelDTO.getResident());
		personnel.setAddrNum(peronnelDTO.getAddrNum());	
		personnel.setAddr1(peronnelDTO.getAddr1());
		personnel.setAddr2(peronnelDTO.getAddr2());
		personnel.setEmail(peronnelDTO.getEmail());
		personnel.setPhone(peronnelDTO.getPhone());
		personnel.setJoinDate(peronnelDTO.getJoinDate());
		personnel.setResignDate(peronnelDTO.getResignDate());
		personnel.setUpdate(peronnelDTO.getUpdate());
		personnel.setName(peronnelDTO.getName());
		personnel.setName(peronnelDTO.getName());
		
		//추가된 부분 
		  if(peronnelDTO.getDeptId() != null) {
		        CommonDetailCode dept = repo.findById(peronnelDTO.getDeptId())
		            .orElseThrow(() -> new IllegalArgumentException("없는 부서 코드"));
		        personnel.setDepartment(dept);
		    }

		    if(peronnelDTO.getPosId() != null) {
		        CommonDetailCode pos = repo.findById(peronnelDTO.getPosId())
		            .orElseThrow(() -> new IllegalArgumentException("없는 직급 코드"));
		        personnel.setPosition(pos);
		    }

		    if(peronnelDTO.getLevId() != null) {
		        CommonDetailCode lev = repo.findById(peronnelDTO.getLevId())
		            .orElseThrow(() -> new IllegalArgumentException("없는 보안등급 코드"));
		        personnel.setLevel(lev);
		    }

		    if(peronnelDTO.getStaId() != null) {
		        CommonDetailCode status = repo.findById(peronnelDTO.getStaId())
		            .orElseThrow(() -> new IllegalArgumentException("없는 재직상태 코드"));
		        personnel.setStatus(status);
		    }

		
		
		return personnel;
	}
	
	
	//update 할때 사용하는 메서드
	public void fromDTOUpdate( PersonnelDTO dto , CommonDetailCodeRepository repo) {
			
		this.setName(dto.getName());
//	    this.setPasswd(dto.getPasswd());	//조건문을 두기 위해 주석 처리
	    this.setResident(dto.getResident());
	    this.setAddrNum(dto.getAddrNum());
	    this.setAddr1(dto.getAddr1());
	    this.setAddr2(dto.getAddr2());
	    this.setEmail(dto.getEmail());
	    this.setPhone(dto.getPhone());
	    this.setJoinDate(dto.getJoinDate());
	    this.setResignDate(dto.getResignDate());
	    this.setUpdate(dto.getUpdate());
	
	    if(dto.getDeptId() != null) {
	        this.setDepartment(repo.findById(dto.getDeptId())
	            .orElseThrow(() -> new IllegalArgumentException("없는 부서 코드")));
	    }
	
	    if(dto.getPosId() != null) {
	        this.setPosition(repo.findById(dto.getPosId())
	            .orElseThrow(() -> new IllegalArgumentException("없는 직급 코드")));
	    }
	
	    if(dto.getLevId() != null) {
	        this.setLevel(repo.findById(dto.getLevId())
	            .orElseThrow(() -> new IllegalArgumentException("없는 보안등급 코드")));
	    }
	
	    if(dto.getStaId() != null) {
	        this.setStatus(repo.findById(dto.getStaId())
	            .orElseThrow(() -> new IllegalArgumentException("없는 재직상태 코드")));
	    }
			
	}
	
}