package com.erp_mes.erp.groupware.entity;

import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.erp_mes.erp.approval.constant.ApprReqType;
import com.erp_mes.erp.groupware.dto.DocumentDTO;
import com.erp_mes.erp.personnel.entity.Personnel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "SHARED_DOCUMENT")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "doc_seq_gen")
    @SequenceGenerator(
        name = "doc_seq_gen",
        sequenceName = "SHARED_DOCUMENT_SEQ", // DB 시퀀스명과 동일하게!
        allocationSize = 1                   // 오라클은 1 권장
    )
    
    @Column(name = "DOC_ID")
    private Long docId;

    @Column(nullable = false, length = 20)
	private String empId;

    @Column(nullable = false, name = "DOC_TITLE", length = 100)
    private String docTitle;

    @Lob
    @Column(name = "DOC_CONTENT")
    private String docContent;

    @Column(nullable = false, name = "DOC_TYPE", length = 100)
    private String docType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "REQ_TYPE", length = 30)
    private ApprReqType reqType;

    @CreatedDate
    @Column(nullable = false, name = "CREATE_AT", updatable = false)
    private LocalDateTime createAt;

    @LastModifiedBy
    @Column(name = "UPDATE_AT")
    private LocalDateTime updateAt;

    public void updateFromDto(DocumentDTO dto) {
        this.docTitle = dto.getDocTitle();
        this.docContent = dto.getDocContent();
        this.docType = dto.getDocType();
        this.reqType = dto.getReqType();
    }

}
