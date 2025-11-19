package com.erp_mes.erp.groupware.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.erp_mes.erp.groupware.entity.Schedule;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleDTO {
    private Long schId;
    private String empId;
    private String schTitle;
    private String schContent;
    private String schType;
    private LocalDateTime starttimeAt;
    private LocalDateTime endtimeAt;
    private LocalDate createAt;
    private LocalDate updateAt;

    // Entity -> DTO 변환 생성자
    public ScheduleDTO(Schedule schedule) {
        this.schId = schedule.getSchId();
        this.empId = schedule.getEmployee().getEmpId();
        this.schTitle = schedule.getSchTitle();
        this.schContent = schedule.getSchContent();
        this.schType = schedule.getSchType();
        this.starttimeAt = schedule.getStarttimeAt();
        this.endtimeAt = schedule.getEndtimeAt();
        this.createAt = schedule.getCreateAt();
        this.updateAt = schedule.getUpdateAt();
    }
}