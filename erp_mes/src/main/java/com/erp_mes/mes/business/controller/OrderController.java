package com.erp_mes.mes.business.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.erp_mes.erp.personnel.dto.PersonnelLoginDTO;
import com.erp_mes.mes.business.dto.OrderDTO;
import com.erp_mes.mes.business.dto.OrderDetailDTO;
import com.erp_mes.mes.business.service.OrderService;
import com.erp_mes.mes.pm.dto.ProductDTO;

import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/business")
@Log4j2
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	// 수주 화면
	@GetMapping("order")
	public String order(Model model, @AuthenticationPrincipal PersonnelLoginDTO userDetails) {
		log.info(userDetails);
		String userDeptId = userDetails.getEmpDeptId();
		String userLevelId = userDetails.getEmpLevelId();
		
		// 부서 코드가 'DEP006'(영업팀)일 경우, 버튼 표시 여부를 true로 설정
        boolean isBusTeam = "DEP006".equals(userDeptId);
        model.addAttribute("isBUSTeam", isBusTeam);
        
        boolean isAutLevel = "AUT001".equals(userLevelId);
        model.addAttribute("isAUTLevel", isAutLevel);

		return "business/order";
	}
	
	// 수주 등록
	@PostMapping("api/orders/submit")
	public ResponseEntity<?> submitOrder(@RequestBody OrderDTO orderDTO, @AuthenticationPrincipal PersonnelLoginDTO userDetails) {

		try {
			// 로그인 사용자 정보 세팅
	        orderDTO.setEmpId(userDetails.getEmpId());
	        orderDTO.setEmpName(userDetails.getName());

	        // 서비스 호출
	        String orderId = orderService.createOrder(orderDTO);

			return ResponseEntity.ok(Map.of("orderId", orderId, "status", "success", "message", "주문이 정상적으로 등록되었습니다."));

		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(Map.of("status", "fail", "error", ex.getMessage()));
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "error", "서버 오류: " + ex.getMessage()));
		}
	}

	// ------------------------------------------

	// 수주 목록 조회
	@GetMapping("/api/orders")
	@ResponseBody
	public List<OrderDTO> getAllOrder() {
		log.info("수주 목록 조회 요청");

		return orderService.getAllOrder();
	}
	
	// 수주 단건 조회
	@GetMapping("/api/orders/{orderId}")
	@ResponseBody
	public ResponseEntity<?> getOrder(@PathVariable("orderId") String orderId) {
	    OrderDTO order = orderService.getOrderById(orderId);
	    if (order == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body("해당 수주를 찾을 수 없습니다.");
	    }
	    return ResponseEntity.ok(order);
	}

	// 수주 상세 목록 조회
	@GetMapping("/api/orders/{orderId}/details")
	@ResponseBody
	public List<OrderDetailDTO> getOrderDetailsByOrderId(@PathVariable("orderId") String orderId) {
		log.info("수주 상세 목록 조회 요청, 수주 ID: {}", orderId);
		return orderService.getOrderDetailsByOrderId(orderId);
	}

	// 품목 리스트 
	@GetMapping("/api/products")
	@ResponseBody
	public List<ProductDTO> getAllProduct() {
		log.info("품목 목록 조회 요청");

		return orderService.getAllProduct();
	}
	
	// 수주 취소 
    @PutMapping("/api/orders/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable("orderId") String orderId, @RequestBody OrderDTO reason) {
        try {
        	String cancelReason = reason.getReason();
        	orderService.cancelOrder(orderId, cancelReason);
            return ResponseEntity.ok(Map.of("orderId", orderId, "newStatus", "CANCELED"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("수주 취소 실패");
        }
    }
    
    // 수주 수정
    @PutMapping("/api/orders/{orderId}")
    public ResponseEntity<?> updateOrder(@PathVariable("orderId") String orderId, @RequestBody OrderDTO orderDTO) {
    	log.info("updateOrder body: {}", orderDTO); 
        orderDTO.setOrderId(orderId);
        
     // items 안에도 orderId 세팅
        if (orderDTO.getItems() != null) {
            orderDTO.getItems().forEach(item -> item.setOrderId(orderId));
        }
        
        orderService.updateOrder(orderDTO);
        
        return ResponseEntity.ok(Map.of("orderId", orderId, "message", "수주 수정이 완료되었습니다."));

    }
    
	
}
