package com.erp_mes.erp.groupware.dto;

import java.time.LocalDate;

import com.erp_mes.erp.groupware.entity.Notice;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NoticeDTO {
	
    private Long notId;
    private String empId;
    private String notTitle;
    private String notContent;
    private String notType;
    private LocalDate createAt;
    private LocalDate updateAt;

    // Entity -> DTO 변환 생성자
    public NoticeDTO(Notice notice) {
        this.notId = notice.getNotId();
        this.empId = notice.getEmployee().getEmpId();
        this.notTitle = notice.getNotTitle();
        this.notContent = notice.getNotContent();
        this.notType = notice.getNotType();
        this.createAt = notice.getCreateAt();
        this.updateAt = notice.getUpdateAt();
    }
}
