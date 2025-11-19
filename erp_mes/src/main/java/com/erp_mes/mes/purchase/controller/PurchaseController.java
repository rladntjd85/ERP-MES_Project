package com.erp_mes.mes.purchase.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.erp_mes.erp.personnel.dto.PersonnelLoginDTO;
import com.erp_mes.mes.pm.dto.WorkOrderShortageDTO;
import com.erp_mes.mes.purchase.dto.PurchaseDTO;
import com.erp_mes.mes.purchase.dto.PurchaseDetailDTO;
import com.erp_mes.mes.purchase.service.PurchaseService;
import com.erp_mes.mes.stock.dto.MaterialDTO;

import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/purchase")
@Log4j2
public class PurchaseController {
	private final PurchaseService purchaseService;

	public PurchaseController(PurchaseService purchaseService) {
		this.purchaseService = purchaseService;
	}
	
	// 발주 화면
	@GetMapping("purchaseOrder")
	public String order(Model model, @AuthenticationPrincipal PersonnelLoginDTO userDetails) {
		
		String userDeptId = userDetails.getEmpDeptId();
		String userLevelId = userDetails.getEmpLevelId();
		
		// 부서 코드가 'DEP007'(구매팀)일 경우, 버튼 표시 여부를 true로 설정
        boolean isPurTeam = "DEP007".equals(userDeptId);
        model.addAttribute("isPURTeam", isPurTeam);
        
        boolean isAutLevel = "AUT001".equals(userLevelId);
        model.addAttribute("isAUTLevel", isAutLevel);

		return "purchase/purchaseOrder";
	}

	// 발주 등록
	@PostMapping("api/purchase/submit")
	public ResponseEntity<?> submitOrder(@RequestBody PurchaseDTO purchaseDTO, @AuthenticationPrincipal PersonnelLoginDTO userDetails) {
		try {
			// 로그인 사용자 정보 세팅
			purchaseDTO.setEmpId(userDetails.getEmpId());
			//purchaseDTO.setEmpName(userDetails.getName());

			// 서비스 호출
			String purchaseId = purchaseService.createPurchase(purchaseDTO);

			return ResponseEntity.ok(Map.of("purchaseId", purchaseId, "status", "success", "message", "발주 정상 등록"));

		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(Map.of("status", "fail", "error", ex.getMessage()));
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "error", "서버 오류: " + ex.getMessage()));
		}
	}
	
	// 자재 리스트
	@GetMapping("/api/materials")
	@ResponseBody
	public List<MaterialDTO> getAllMaterial() {
		log.info("자재 목록 조회 요청");

		return purchaseService.getAllMaterial();
	}

	// 발주 목록 조회
	@GetMapping("/api/purchase")
	@ResponseBody
	public List<PurchaseDTO> getAllPurchaseOrder() {
		log.info("발주 목록 조회 요청");
		
		return purchaseService.getAllPurchase();
	}
	
	// 발주 상세 목록 조회
	@GetMapping("/api/purchase/{purchaseId}/details")
	@ResponseBody
	public List<PurchaseDetailDTO> getPurchaseDetailsByOrderId(@PathVariable("purchaseId") String purchaseId) {
		log.info("발주 상세 목록 조회 요청, 발주 ID: {}", purchaseId);
		
		return purchaseService.getPurchaseDetailsByOrderId(purchaseId);
	}
	
	// 발주 단건 조회 -> 발주 수정 모달창에서 발주 등록 때 입력했던 데이터값 가져오기 위해
	@GetMapping("/api/purchase/{purchaseId}")
	@ResponseBody
	public ResponseEntity<?> getPurchase(@PathVariable("purchaseId") String purchaseId) {
		PurchaseDTO purchase = purchaseService.getPurchaseById(purchaseId);
		if (purchase == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("해당 발주를 찾을 수 없습니다.");
		}
		return ResponseEntity.ok(purchase);
	}
	
	// 발주 수정
	@PutMapping("/api/purchase/{purchaseId}")
    public ResponseEntity<?> updatePurchase(@PathVariable("purchaseId") String purchaseId, @RequestBody PurchaseDTO purchaseDTO) {
    	log.info("updatePurchase body: {}", purchaseDTO); 
    	purchaseDTO.setPurchaseId(purchaseId);
        
    	// materials 안에도 materialId 세팅
        if (purchaseDTO.getMaterials() != null) {
            purchaseDTO.getMaterials().forEach(material -> material.setPurchaseId(purchaseId));
        }
        
        purchaseService.updatePurchase(purchaseDTO);
        
        return ResponseEntity.ok(Map.of("purchaseId", purchaseId, "message", "발주 수정이 완료되었습니다."));

    }
	
	// 발주 취소 
    @PutMapping("/api/purchase/{purchaseId}/cancel")
    public ResponseEntity<?> cancelPurchase(@PathVariable("purchaseId") String purchaseId, @RequestBody PurchaseDTO reason) {
        try {
        	String cancelReason = reason.getReason();
        	purchaseService.cancelPurchase(purchaseId, cancelReason);
            return ResponseEntity.ok(Map.of("purchaseId", purchaseId, "newStatus", "CANCELED"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("발주 취소 실패");
        }
    }
     
    // WorkOrderShortage 목록 불러옴
    @GetMapping("/api/work-orders/shortage")
    public ResponseEntity<List<WorkOrderShortageDTO>> getWorkOrderShortage() {
    	log.info("작업지시 목록 조회 요청");
        List<WorkOrderShortageDTO> shortages = purchaseService.getWorkOrderShortages();
        return ResponseEntity.ok(shortages);
    }
    
    // 특정 작업지시의 상세 자재 목록을 불러옴
    @GetMapping("/api/work-orders/{workOrderId}/details")
    public ResponseEntity<List<WorkOrderShortageDTO>> getWorkOrderDetails(@PathVariable("workOrderId") String workOrderId) {
    	log.info("작업지시 자재 목록 조회 요청");
        List<WorkOrderShortageDTO> details = purchaseService.getWorkOrderDetailsForPurchase(workOrderId);
        return ResponseEntity.ok(details);
    }
	
}
