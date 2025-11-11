package com.erp_mes.erp.approval.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.erp_mes.erp.approval.constant.ApprDecision;
import com.erp_mes.erp.approval.constant.ApprReqType;
import com.erp_mes.erp.approval.dto.ApprDTO;
import com.erp_mes.erp.approval.dto.ApprFullDTO;
import com.erp_mes.erp.approval.service.ApprService;
import com.erp_mes.erp.attendance.entity.Annual;
import com.erp_mes.erp.groupware.dto.DocumentDTO;
import com.erp_mes.erp.groupware.service.DocumentService;
import com.erp_mes.erp.personnel.dto.PersonnelDTO;
import com.erp_mes.erp.personnel.dto.PersonnelLoginDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/approval")
@RequiredArgsConstructor
@Log4j2
public class ApprController {
	
	private final ApprService apprService;
	
	private final DocumentService documentService;
	
	@GetMapping("/doc_list")
	public String getDocList(Model model){
		
		List<DocumentDTO> documents = documentService.getAllDocuments();
		model.addAttribute("documents", documents);
		
		
	    return "/approval/appr_doc_list";
	}

    @GetMapping("/new/{docId}")
    public String draftingForm(@PathVariable("docId") Long docId, Model model, Authentication authentication){
    	    	
    	// apprDTO가 이미 있으면 (리다이렉트로 전달된 경우) 그대로 사용
        // 없으면 (최초 진입 시) 새 객체 생성
        ApprDTO apprDTO = (ApprDTO) model.getAttribute("apprDTO");
        if (apprDTO == null) {
            apprDTO = new ApprDTO();
            model.addAttribute("apprDTO", apprDTO);
        }
    	
    	//사원정보
    	PersonnelLoginDTO principal = (PersonnelLoginDTO) authentication.getPrincipal();
    	String loginEmpId = principal.getEmpId();
    	//연차 정보
    	Annual annual = apprService.getAnnualInfo(loginEmpId);
    	double remain = annual.getAnnTotal() - annual.getAnnUse();
    	//문서정보
    	DocumentDTO documentDTO = documentService.getDocument(docId);
    	
    	String docTitle = documentDTO.getDocTitle();
    	String docContent = documentDTO.getDocContent();
    	
    	log.info(">>>>>>>>>>>>>>>>>>>>>"+ docContent);
    	    	
    	model.addAttribute("selectedRole", documentDTO.getReqType()); // 기본 선택값
    	model.addAttribute("loginEmpId", loginEmpId);
    	model.addAttribute("principal", principal);
    	model.addAttribute("remain", remain);
    	model.addAttribute("docTitle", docTitle);
    	model.addAttribute("docContent", docContent);
    	model.addAttribute("reqTypes", ApprReqType.values());
    	model.addAttribute("docId", docId);
    	        
        return "approval/drafting_form";
    }
    
    @GetMapping("/showDraftingForm") 
    public String showMyForm() {
        log.info("단순 폼 보여주기 요청");
        return "approval/drafting_form"; 
    }
    
    
	// 결재 목록 조회 (페이징, 상태별 필터링 지원)
    @GetMapping("/approval_list")
    public String approvalList(
        @RequestParam(value = "status", required = false, defaultValue = "all") String status,
        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
        Model model, Authentication authentication) {
        
        // null 체크 추가
        if (authentication == null) {
            log.warn("Authentication is null - redirecting to login");
            return "redirect:/login";
        }
        
        log.info("결재 목록 조회 - 상태: {}, 페이지: {}", status, page);
        
        Pageable pageable = PageRequest.of(page, 5, Sort.by(Sort.Direction.DESC, "createAt"));
        String currentUserId = authentication.getName();
        
        Page<ApprDTO> approvalPage = apprService.getApprovalList(pageable, status, currentUserId);
        addPaginationAttributes(model, approvalPage, status);
        
        return "approval/approval_List";    
    }
    
    //결재 상세 정보 조회 API
	@GetMapping("/api/detail/{reqId}")
	@ResponseBody
	public ResponseEntity<ApprFullDTO> getApprovalDetail(@PathVariable("reqId") Long reqId) {
	    log.info("결재 상세 조회 API 호출 - reqId: {}", reqId);
	    
	    try {
	        ApprFullDTO detailDTO = apprService.getApprovalDetail(reqId);
	        log.info("결재 상세 조회 성공 - reqId: {}", reqId);
	        return ResponseEntity.ok(detailDTO); 
	    } catch (Exception e) {
	        log.error("결재 상세 조회 실패 - reqId: {}, 오류: {}", reqId, e.getMessage(), e);
	        return ResponseEntity.status(500).body(null);
	    }
	}
 	
    // 0827 승인 처리 API
	@PostMapping("/api/approve/{reqId}")
	@ResponseBody
	public ResponseEntity<String> approveRequest(@PathVariable("reqId") Long reqId, 
	                                            @RequestBody(required = false) Map<String, String> requestBody,
	                                            Authentication authentication) {
	    if (authentication == null) {
	        return ResponseEntity.status(401).body("인증이 필요합니다.");
	    }
	    return processApproval(reqId, requestBody, "APPROVE", authentication);  
	}

    
    // 0827 반려 처리 API
    @PostMapping("/api/reject/{reqId}")
    @ResponseBody 
    public ResponseEntity<String> rejectRequest(@PathVariable("reqId") Long reqId, @RequestBody(required = false)
                                                Map<String, String> requestBody, Authentication authentication) { 
    	
        return processApproval(reqId, requestBody, "REJECT", authentication);  
    }
    // 0901 삭제
    @PostMapping("/api/delete-selected")
    @ResponseBody
    public ResponseEntity<String> deleteSelected(@RequestParam("ids") List<Long> ids, 
                                               Authentication authentication) {
        try {
            String loginId = authentication.getName();
            apprService.deleteSelected(ids, loginId);
            return ResponseEntity.ok("삭제 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("삭제 실패: " + e.getMessage());
        }
    }
    
    // ==================== Private Helper Methods ====================
    // 0827 승인/반려 공통 처리 메서드
    private ResponseEntity<String> processApproval(Long reqId, Map<String, String> requestBody, String action, Authentication authentication) {  // authentication 추가
        log.info("{} 처리 API 호출 - reqId: {}", action, reqId);
        
        try {
            String comments = extractComments(requestBody);
            String loginId = authentication.getName();  // 로그인 ID 가져오기
            log.info("{} 사유: {}, 로그인ID: {}", action, comments, loginId);
            
            if ("APPROVE".equals(action)) {
            	apprService.processApproval(reqId, loginId, ApprDecision.ACCEPT, comments);
// 기존에 있던 것                apprService.approveRequestWithComments(reqId, comments, loginId);  // loginId 추가
                log.info("승인 처리 완료 - reqId: {}", reqId);
                return ResponseEntity.ok("승인 처리가 완료되었습니다.");
            } else {
            	apprService.processApproval(reqId, loginId, ApprDecision.DENY, comments);
// 기존에 있던 것               apprService.rejectRequestWithComments(reqId, comments, loginId);  // loginId 추가
                log.info("반려 처리 완료 - reqId: {}", reqId);
                return ResponseEntity.ok("반려 처리가 완료되었습니다.");
            }
            
        } catch (Exception e) {
            log.error("{} 처리 실패 - reqId: {}, 오류: {}", action, reqId, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(action.equals("APPROVE") ? "승인 처리 중 오류가 발생했습니다." : "반려 처리 중 오류가 발생했습니다.");
        }
    }
    
    // 결재 취소 처리 API
	@PostMapping("/api/cancel/{reqId}")
	@ResponseBody
	public ResponseEntity<String> cancelRequest(@PathVariable("reqId") Long reqId, 
	                                           Authentication authentication) {
		try {
		    String loginId = authentication.getName();
		    log.info("=== 결재 취소 요청 ===");
		    log.info("reqId: {}", reqId);
		    log.info("loginId from authentication: {}", loginId);
		    log.info("authentication principal: {}", authentication.getPrincipal());
		    
		    // Service를 통해 취소 처리
		    apprService.cancelApproval(reqId, loginId);
		    
		    return ResponseEntity.ok("결재가 취소되었습니다.");
		} catch (IllegalArgumentException e) {
		    log.error("IllegalArgumentException: {}", e.getMessage());
		    return ResponseEntity.status(400).body(e.getMessage());
		} catch (SecurityException e) {
		    log.error("SecurityException: {}", e.getMessage());
		    return ResponseEntity.status(403).body(e.getMessage());
		} catch (Exception e) {
		    log.error("결재 취소 실패 - reqId: {}, 오류: {}", reqId, e.getMessage(), e);
		    return ResponseEntity.status(500).body("결재 취소 중 오류가 발생했습니다.");
		}
	}
	
    // 알림창
	@GetMapping("/api/counts")
	@ResponseBody
	public Map<String, Object> getApprovalCounts(Authentication authentication) {
	    Map<String, Object> result = new HashMap<>();
	    
	    // SecurityConfig에서 permitAll()이니까 authentication이 null일 수 있음
	    if (authentication == null || !authentication.isAuthenticated()) {
	        result.put("myPending", 0);
	        result.put("toApprove", 0);
	        result.put("myApprovalStatus", "0-0-0-0");
	        return result;
	    }
	    
	    String loginId = authentication.getName();
	    result.put("myPending", apprService.getMyPendingCount(loginId));
	    result.put("toApprove", apprService.getToApproveCount(loginId));
	    result.put("myApprovalStatus", apprService.getMyApprovalStatusSummary(loginId));
	    
	    return result;
	}
    
	//요청 본문에서 comments 추출
	private String extractComments(Map<String, String> requestBody) {
	    if (requestBody != null && requestBody.containsKey("comments")) {
	        return requestBody.get("comments");
	    }
	    return "";
	}
    
	//모델에 페이징 관련 속성 추가
	private void addPaginationAttributes(Model model, Page<ApprDTO> approvalPage, String status) {
	    model.addAttribute("approvalList", approvalPage.getContent());
	    model.addAttribute("currentPage", approvalPage.getNumber());
	    model.addAttribute("totalPages", approvalPage.getTotalPages());
	    model.addAttribute("totalElements", approvalPage.getTotalElements());
	    model.addAttribute("hasPrevious", approvalPage.hasPrevious());
	    model.addAttribute("hasNext", approvalPage.hasNext());
	    model.addAttribute("currentStatus", status);
	}
    
	//결재자 검색
	@GetMapping("/empSearch")
	@ResponseBody
	public List<PersonnelDTO> searchUser(@RequestParam("name") String name, Authentication authentication) {
		
		String loginEmpId = authentication.getName();
		
	    return apprService.getApprEmployee(name, loginEmpId);
	}
 	
    @PostMapping("/save")
    @ResponseBody
    public String registAppr(@ModelAttribute("apprDTO") @Valid ApprDTO apprDTO, BindingResult bindingResult,  @RequestParam(value = "empIds", required = false) String[] empIds, @RequestParam("docId") Long docId, Model model,
    		RedirectAttributes redirectAttributes,Authentication authentication) throws IOException {  // Authentication 추가
    	
    	log.info("empIds.length===>>>>>>"+empIds.length);
    	// empIds 필수검증
    	if (empIds == null || empIds.length < 2) {
            bindingResult.reject("empIds.empty", "결재자를 한 명 이상 지정해야 합니다.");
        }

        if (bindingResult.hasErrors()) {
        	// Flash 속성을 사용하여 오류 데이터와 입력 데이터를 임시 저장
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.apprDTO", bindingResult);
            redirectAttributes.addFlashAttribute("apprDTO", apprDTO);
            
            // GET /approval/new/{docId} 컨트롤러로 리다이렉트
            return "redirect:/approval/new/" + docId; 
        }

        if (apprDTO.getApprDetailDTOList() == null) {
            apprDTO.setApprDetailDTOList(new ArrayList<>());
        }

        // 로그인한 사용자 ID 가져와서 Service에 전달
        String loginEmpId = authentication.getName();
        Long apprId = apprService.registAppr(apprDTO, empIds, loginEmpId);  // 3개 파라미터

        //결재 리스트로 이동되게 변경해야함.
        return "<script>" +
		        "alert('신청 완료되었습니다.');" +
		        "if (window.opener) {" +
		        "   window.opener.location.href = '/approval/approval_list?status=my';" +
		        "}" +
		        "window.close();" +
		        "</script>";
    }
}
