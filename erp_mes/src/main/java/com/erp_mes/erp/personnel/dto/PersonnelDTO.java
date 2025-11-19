package com.erp_mes.erp.personnel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.sql.Timestamp;

import com.erp_mes.erp.personnel.entity.Personnel;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PersonnelDTO {

	private String empId;		// 사원id 		ex)2025082101
	private String name;		// 이름
	private String passwd;		// 비밀번호
	private String resident;	// 주민등록번호
	private String addrNum;		// 우편번호
	private String addr1;		// 주소
	private String addr2;		// 상세주소
	private String email;		// 이메일
	private String phone;		// 전화번호
	private String joinDate;	// 입사일
	private String resignDate;	// 퇴사일	
	private Timestamp update;	// 수정일
//	private String status;		// 재직상태 수정
	
	// 부서 직급 정보
	private String posId;			// 직급
	private String posName;		
	private String deptId;		// 부서명은 엔티티에서 직접 가져오지 않고 DTO에서 추가
	private String deptName;

	
	//추가 한 컬럼	
	private String levId;		//보안등급 아이디값
	private String levName;		//보안등급 이름
	private String staId;		//재직현황 아이디값
	private String staName;		//재직현항 이름
	// Entity -> DTO 변환을 위한 정적 팩토리 메서드
    public static PersonnelDTO fromEntity(Personnel personnel) {
		return PersonnelDTO.builder().empId(personnel.getEmpId()).name(personnel.getName()).passwd(personnel.getPasswd())
				.resident(personnel.getResident()).addrNum(personnel.getAddrNum()).addr1(personnel.getAddr1()).addr2(personnel.getAddr2())
				.email(personnel.getEmail()).phone(personnel.getPhone()).joinDate(personnel.getJoinDate()).resignDate(personnel.getResignDate())
				.update(personnel.getUpdate())
				.deptId(personnel.getDepartment() != null ? personnel.getDepartment().getComDtId() : null)
				.deptName(personnel.getDepartment() != null ? personnel.getDepartment().getComDtNm()  : null)
				.posId(personnel.getPosition() != null ? personnel.getPosition().getComDtId() : null)
				.posName(personnel.getPosition() != null ? personnel.getPosition().getComDtNm() : null)
				//추가한 부분 보안등급 (Level)
				.levId(personnel.getLevel() != null ? personnel.getLevel().getComDtId() : null)
				.levName(personnel.getLevel() != null ? personnel.getLevel().getComDtNm() : null)
				.staId(personnel.getStatus() != null ? personnel.getStatus().getComDtId() : null)
				.staName(personnel.getStatus() != null ? personnel.getStatus().getComDtNm() : null)
				.build();
	}

}

