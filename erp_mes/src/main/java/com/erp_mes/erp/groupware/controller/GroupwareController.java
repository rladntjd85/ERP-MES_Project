package com.erp_mes.erp.groupware.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.erp_mes.erp.approval.constant.ApprReqType;
import com.erp_mes.erp.commonCode.service.CommonCodeService;
import com.erp_mes.erp.config.util.TableMetadataManager;
import com.erp_mes.erp.groupware.dto.DocumentDTO;
import com.erp_mes.erp.groupware.entity.Document;
import com.erp_mes.erp.groupware.repository.DocumentRepository;
import com.erp_mes.erp.groupware.service.DocumentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@Controller
@RequestMapping("/groupware")
@Log4j2
@RequiredArgsConstructor
public class GroupwareController {
	
	private final CommonCodeService comService;
	
	@Autowired
	private DocumentRepository documentRepository;
	
	private final DocumentService documentService;
	
	//공통문서목록
	@GetMapping("/document")
	public String document(Model model) {

		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		List<DocumentDTO> documents = documentService.getAllDocuments();
		model.addAttribute("documents", documents);
		
		return "gw/document";
	}
	
	//공통 문서 작성 & 수정 화면
	@GetMapping("/docWrite")
	public String docWrite(@RequestParam(name = "docId", required = false) Long docId, Model model) {
		
		log.info(">>>>>>>>>>>>>>>>>>>>>>"+docId);
		
		model.addAttribute("dtCodes", comService.findByComId("DOC"));
		model.addAttribute("reqTypes", ApprReqType.values());
		
		if (docId == null) {
			
			model.addAttribute("documentDTO", new DocumentDTO());
		} else {
			DocumentDTO documentDTO = documentService.getDocument(docId);
			model.addAttribute("documentDTO", documentDTO);
		}
		
		return "gw/docWrite";
	}

	//공통 문서 저장
	@PostMapping("/save")
	public String saveDoc(DocumentDTO documentDTO) {
		// 1. 현재 로그인한 사용자 정보 가져오기
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		String loginEmpId = authentication.getName();

		Document doc = new Document();
		
		doc.setDocTitle(documentDTO.getDocTitle());
		doc.setDocContent(documentDTO.getDocContent());
		doc.setEmpId(loginEmpId);
		doc.setDocType(documentDTO.getDocType());
		doc.setReqType(documentDTO.getReqType());

		documentRepository.save(doc);
		
		return "redirect:/groupware/document";
	}
	
	//공통문서 상세페이지
	@GetMapping("/docView/{docId}")
	public String docView(@PathVariable("docId") Long docId, Model model) {
		
		log.info(">>>>>>>>>>>>>>>>>>>innnnnnnnn");
		
		DocumentDTO documentDTO = documentService.getDocument(docId);
		
		model.addAttribute("dtCodes", comService.findByComId("DOC"));
		model.addAttribute("documentDTO", documentDTO);
		
		return "gw/docView";
	}
	
	//공통 문서 수정
	@PutMapping("/modify/{docId}")
	public ResponseEntity<?> updateDocument (@PathVariable("docId") Long docId, @RequestBody DocumentDTO documentDTO) {
		
		// id와 dto.docId 일치 여부 검증(optional)
        if (!docId.equals(documentDTO.getDocId())) {
            return ResponseEntity.badRequest().body("ID mismatch");
        }

        documentService.updateDocument(documentDTO);
		
		return ResponseEntity.ok().build();
		
	}
	
//	 번호로 삭제
	@DeleteMapping("/removeById/{docId}")
	public ResponseEntity<?> memberRemoveById(@PathVariable("docId") Long docId) {
		// MemberService - removeMemberByName() 메서드 호출
		// => 파라미터 : 번호   리턴타입 : void
		documentService.removeMemberById(docId);
		
		return ResponseEntity.ok().build();
	}
}
