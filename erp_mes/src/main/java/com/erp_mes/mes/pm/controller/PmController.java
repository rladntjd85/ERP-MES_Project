package com.erp_mes.mes.pm.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.erp_mes.erp.personnel.dto.PersonnelDTO;
import com.erp_mes.mes.pm.dto.BomDTO;
import com.erp_mes.mes.pm.dto.OrdersDTO;
import com.erp_mes.mes.pm.dto.OrdersDetailDTO;
import com.erp_mes.mes.pm.dto.ProductDTO;
import com.erp_mes.mes.pm.dto.ProductPlanDTO;
import com.erp_mes.mes.pm.dto.WorkOrderDTO;
import com.erp_mes.mes.pm.dto.WorkOrderShortageDTO;
import com.erp_mes.mes.pm.service.PmService;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestParam;




@Log4j2
@Controller
@RequestMapping("/pm")
public class PmController {

	private final PmService pmService;
	
	public PmController(PmService pmService) {
		this.pmService = pmService;
	}
	
	// 생산계획 리스트
	@GetMapping("/productPlan")
	public String getProductPlan(Model model) {
		
		// 생산계획 리스트 들고오기
//		List<ProductPlanDTO> productPlanDTOList = productPlanService.getProductPlanList();
//		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> productPlanDTOList : " + productPlanDTOList);
		
		// 등록된 수주 들고오기
//		List<OrdersDTO> orderId = productPlanService.getOrderId();
		
		
		// 선택한 수주에 해당하는 제품명만 셀렉트박스
		List<ProductDTO> productName = pmService.getProductName();
//		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> productName : " + productName);
		model.addAttribute("productName", productName);
		
		
		
		return "pm/product_plan_list";
	}
	
	// 생산계획 리스트 그리드
	@ResponseBody
	@GetMapping("/productPlanList")
	public List<ProductPlanDTO> getProductPlanList() {
		return pmService.getProductPlanList(); // json으로 변환 => 그리드에 값 넣어야해서
	}
	
	// 수주 리스트 그리드
	@ResponseBody
	@GetMapping("/ordersList")
	public List<OrdersDTO> getOrderId() {
		return pmService.getOrderId(); // json으로 변환 => 그리드에 값 넣어야해서
	}
	
	// 수주번호에 해당하는 제품값 들고오기
	@ResponseBody
	@GetMapping("/ordersProduct")
	public List<OrdersDetailDTO> getOrdersProduct(@RequestParam(name = "order_id") String orderId,
												  @RequestParam(name = "id") Long id) {
		
		Map<String, Object> map = new HashMap<>();
		map.put("orderId", orderId);
		map.put("id", id);
		
		return pmService.getOdersProduct(map);
	}
	
	
	// 생산계획 등록
	@ResponseBody
	@PostMapping("/productPlanRegist")
	public ResponseEntity<String> productPlanRegist(@RequestBody ProductPlanDTO productPlanDTO) {
		pmService.productPlanRegist(productPlanDTO);
		return ResponseEntity.ok("sucess");
	}
	
	// ===============================================================================================
	// 작업지시
	
	// 작업지시서 리스트
	@GetMapping("/workOrder")
	public String getWorkOrder(Model model) {
		
//		List<WorkOrderDTO> workOrderList = productPlanService.getWorkOrderList();
//		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> workOrderList : " + workOrderList);
		
		// possible 생산계획
		List<ProductPlanDTO> planList = pmService.getPlanList();
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> planList : " + planList);
		model.addAttribute("planList", planList);
		
		// 담당자 셀렉트박스
		List<PersonnelDTO> empInfo = pmService.getEmpInfo();
//		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> empInfo : " + empInfo);
		model.addAttribute("empInfo", empInfo);
		
		
		
		return "pm/work_order_list";
	}
	
	// 작업지시서 리스트 그리드
	@ResponseBody
	@GetMapping("/workOrderList")
	public List<WorkOrderDTO> getWorkOrderList() {
		return pmService.getWorkOrderList(); // json으로 변환 => 그리드에 값 넣어야해서
	}
	
	// 생산계획 아이디로 제품명과 생산수량 라우트 그룹 라인명 들고오기
	@ResponseBody
	@GetMapping("/workOrderInfo")
	public ProductPlanDTO getworkOrderInfo(@RequestParam(name = "plan_id") String planId) {
		
		// 생산계획 아이디로 제품명과 생산수량 라우트 그룹 라인명 들고오기
//		List<ProductPlanDTO> workOrderInfo = productPlanService.getWorkOrderInfo(planId);
//		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> workOrderInfo : " + workOrderInfo);
		return pmService.getWorkOrderInfo(planId);
	}
	
	// bom 필요 자재수와 창고 자재 수 비교 
	@ResponseBody
	@GetMapping("/workOderInventory")
	public List<BomDTO> getWorkOderInventory(@RequestParam(name = "plan_id") String planId) {
		return pmService.getWorkOrderInventory(planId);
	}
	
	
	// 작업지시 등록
	@ResponseBody
	@PostMapping("/workOrderRegist")
	public ResponseEntity<?> registWorkOrder(@RequestBody WorkOrderDTO workOrderDTO) {
		
		try {
	        // 서비스 호출 (결과 반환)
	        String result = pmService.insertWorkOrder(workOrderDTO);
	        
	        // 성공 시
	        if ("재고부족".equals(result)) {
	            return ResponseEntity
	                    .status(HttpStatus.OK)
	                    .body(Map.of("status", "fail", "message", "자재 재고가 부족합니다."));
	        } else {
	            return ResponseEntity
	                    .status(HttpStatus.OK)
	                    .body(Map.of("status", "success", "message", "작업지시 등록 완료"));
	        }

	    } catch (Exception e) {
	        log.error("작업지시 등록 중 오류 발생", e);
	        return ResponseEntity
	                .status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("status", "error", "message", "서버 오류 발생"));
	    }
	}
	
	// 발주자재 정보
	@ResponseBody
	@GetMapping("/purchaseInfo")
	public List<BomDTO> getPurchaseInfo(@RequestParam(name = "work_order_id") String workOrderId) {
		
		List<BomDTO> bom = pmService.getPurchaseInfo(workOrderId);
		log.info(">>>>>>>>>>>>>>>> bom=" + bom);
		
		return pmService.getPurchaseInfo(workOrderId);
	}
	
	// 발주 등록
	@ResponseBody
	@PostMapping("/purchaseRegist")
	public String purchaseRegist(@RequestBody List<WorkOrderShortageDTO> workOrderShortageDTO) {
		pmService.insertPusrchase(workOrderShortageDTO);
		return "sucess";
	}
	
	
	
}
