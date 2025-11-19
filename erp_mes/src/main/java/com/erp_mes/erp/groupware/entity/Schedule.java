package com.erp_mes.erp.groupware.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.erp_mes.erp.personnel.entity.Personnel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "SCHEDULE")
@Getter
@Setter
@NoArgsConstructor
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "SCH_ID")
    private Long schId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMP_ID", referencedColumnName = "EMP_ID")
    private Personnel employee;
    
    @Column(name = "SCH_TITLE")
    private String schTitle;
    
    @Lob
    @Column(name = "SCH_CONTENT")
    private String schContent;
    
    @Column(name = "SCH_TYPE")
    private String schType;
    
    @Column(name = "STARTTIME_AT")
    private LocalDateTime starttimeAt;
    
    @Column(name = "ENDTIME_AT")
    private LocalDateTime endtimeAt;
    
    @CreationTimestamp
    @Column(name = "CREATE_AT")
    private LocalDate createAt;
    
    @UpdateTimestamp
    @Column(name = "UPDATE_AT")
    private LocalDate updateAt;
}
