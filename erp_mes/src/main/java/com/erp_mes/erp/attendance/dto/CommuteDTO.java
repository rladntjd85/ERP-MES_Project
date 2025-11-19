package com.erp_mes.erp.attendance.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CommuteDTO {
	private Long commuteId;
	private LocalDateTime checkInTime;
	private LocalDateTime checkOutTime;
	private String empId;
	private String workStatus;
	private String workStatusNm;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String empName;
	private String deptName;
	private String posName;
	private String commuteDate;

	@Builder
	public CommuteDTO(Long commuteId, LocalDateTime checkInTime, LocalDateTime checkOutTime, String empId,
			String workStatus, LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.commuteId = commuteId;
		this.checkInTime = checkInTime;
		this.checkOutTime = checkOutTime;
		this.empId = empId;
		this.workStatus = workStatus;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

}
