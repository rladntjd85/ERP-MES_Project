package com.erp_mes.erp.approval.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.modelmapper.ModelMapper;

import com.erp_mes.erp.approval.constant.ApprDecision;
import com.erp_mes.erp.approval.constant.ApprReqType;
import com.erp_mes.erp.approval.constant.ApprStatus;
import com.erp_mes.erp.approval.entity.Appr;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ApprDTO {
	
	private Long reqId;
	
	private String empId;
	
	@NotEmpty(message = "결재자를 한 명 이상 선택해야 합니다.")
    private List<String> empIds;
	
	private String reqType;
	
	@NotBlank(message = "제목은 필수 입력값입니다.") // 공백만 있거나, 길이가 0인 문자열, null 값을 허용하지 않음
	private String title;
	
	private String content;
	
	private LocalDate requestAt;
	
	private LocalDateTime createAt;
	
	private LocalDateTime updateAt;
	
 	private ApprStatus status = ApprStatus.REQUESTED;
 	
 	private Integer totStep;
 	
	private String drafterName;     // 기안자 이름
	private String department;      // 기안자 부서
	private String position;
	private String currentApprover; // 현재 결재자 이름
	private boolean hasRejection = false;
	
	private LocalDateTime decDate; // 결재일자 추가
	private String decision; // 결재 상태 (PENDING, ACCEPT, DENY)
	private Integer stepNo; // 결재순번 추가
    
    
	// 상태를 한글로 변환하는 메서드 추가
	public String getStatusLabel() {
	    if 		("ACCEPT".equals(decision)) return "승인";
	    else if ("DENY".equals(decision)) 	return "반려";
	    else return "대기";
	}
    
	// 내가 기안한 문서의 상태 표시
	public String getMyApprovalStatus() {
		if (this.status == ApprStatus.FINISHED) return "완료";
	    else if (this.status == ApprStatus.CANCELED) {
	        if (this.hasRejection) {
	            return "반려됨";  // 결재자가 반려
	        }
	        return "취소됨";  // 기안자가 직접 취소
	    }
	    else if (this.status == ApprStatus.PROCESSING) return "진행중";
	    else return "대기";
	}
    
 	private List<ApprLineDTO> ApprLineDTOList;
 	private List<ApprDetailDTO> apprDetailDTOList;

	@Builder
	public ApprDTO(Long reqId, String empId, String reqType, String title, String content, LocalDate requestAt, LocalDateTime createAt,
			LocalDateTime updateAt, ApprStatus status, Integer totStep) {
		this.reqId = reqId;
		this.empId = empId;
		this.reqType = reqType;
		this.title = title;
		this.content = content;
		this.requestAt = requestAt;
		this.createAt = createAt;
		this.updateAt = updateAt;
		this.status = status;
		this.totStep = totStep;
	}

	private static ModelMapper modelMapper = new ModelMapper();
	
	public Appr toEntity() { return modelMapper.map(this, Appr.class); }
	
	public static ApprDTO fromEntity(Appr appr) { return modelMapper.map(appr, ApprDTO.class); }

}
