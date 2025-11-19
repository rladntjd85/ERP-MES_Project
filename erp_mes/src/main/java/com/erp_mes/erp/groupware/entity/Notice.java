package com.erp_mes.erp.groupware.entity;

import java.time.LocalDate;

import com.erp_mes.erp.personnel.entity.Personnel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "NOTICE")
@Getter
@Setter
@NoArgsConstructor
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "NOT_ID")
    private Long notId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMP_ID", referencedColumnName = "EMP_ID")
    private Personnel employee;

    @Column(name = "NOT_TITLE", length = 255)
    private String notTitle;

    @Lob
    @Column(name = "NOT_CONTENT")
    private String notContent;

    @Column(name = "NOT_TYPE", length = 100)
    private String notType;

    @Column(name = "CREATE_AT")
    private LocalDate createAt;

    @Column(name = "UPDATE_AT")
    private LocalDate updateAt;

}
