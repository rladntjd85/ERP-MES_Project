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
public class CommuteScheduleDTO {
	private Long scheduleId;
	private LocalDateTime workStartTime;
	private LocalDateTime workEndTime;
	private LocalDateTime applyStartDate;

	@Builder
	public CommuteScheduleDTO(Long scheduleId, LocalDateTime workStartTime, LocalDateTime workEndTime,
			LocalDateTime applyStartDate) {
		this.scheduleId = scheduleId;
		this.workStartTime = workStartTime;
		this.workEndTime = workEndTime;
		this.applyStartDate = applyStartDate;
	}

}
