package com.erp_mes.mes.pm.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.erp_mes.erp.personnel.dto.PersonnelDTO;
import com.erp_mes.mes.business.service.OrderService;
import com.erp_mes.mes.pm.dto.BomDTO;
import com.erp_mes.mes.pm.dto.OrdersDTO;
import com.erp_mes.mes.pm.dto.OrdersDetailDTO;
import com.erp_mes.mes.pm.dto.ProductDTO;
import com.erp_mes.mes.pm.dto.ProductPlanDTO;
import com.erp_mes.mes.pm.dto.WorkOrderDTO;
import com.erp_mes.mes.pm.dto.WorkOrderShortageDTO;
import com.erp_mes.mes.pm.mapper.PmMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class PmService {
	
	private final PmMapper pmMapper;
	private final OrderService orderService;

	public PmService(PmMapper pmMapper, OrderService orderService) {
		this.pmMapper = pmMapper;
		this.orderService = orderService;
	}

	// 생산계획 리스트
	public List<ProductPlanDTO> getProductPlanList() {
		return pmMapper.getProductPlanList();
	}

	// 제품명 셀렉트박스
	public List<ProductDTO> getProductName() {
		// 생산계획에 등록된 제품이라면 안보이게 처리
		return pmMapper.getProductName();
	}

	// 담당자 셀렉트박스
	public List<PersonnelDTO> getEmpInfo() {
		return pmMapper.getEmpInfo();
	}

	// 생산계획 등록
	public void productPlanRegist(ProductPlanDTO productPlanDTO) {
		
		productPlanDTO.setPlanStatus("POSSIBLE");
		pmMapper.insertProductPlan(productPlanDTO);
		orderService.updateOrderStatusToInProduction(productPlanDTO.getOrderId(), productPlanDTO.getProductId());
	}

	// 작업지시서 리스트
	public List<WorkOrderDTO> getWorkOrderList() {
		return pmMapper.getWorkOrderList();
	}

	// 등록된 수주 리스트
	public List<OrdersDTO> getOrderId() {
		return pmMapper.getOrderId();
	}

	// 수주번호에 해당하는 제품
	public List<OrdersDetailDTO> getOdersProduct(Map<String, Object> map) {
		return pmMapper.getOrdersProduct(map);
	}

	// possible 상태인 생산계획만 가져오기
	public List<ProductPlanDTO> getPlanList() {
		return pmMapper.getPlanList();
	}

	// 생산계획 아이디로 제품명과 생산수량 라우트 그룹 라인명 들고오기
	public ProductPlanDTO getWorkOrderInfo(String planId) {
		return pmMapper.getWorkOderInfo(planId);
	}

	// bom 필요 자재수와 창고 자재 수 비교 
	public List<BomDTO> getWorkOrderInventory(String planId) {
		return pmMapper.getWorkOderInventory(planId);
	}

	// 작업지시 등록
	public String insertWorkOrder(WorkOrderDTO workOrderDTO) {
		
		// bom 자재 조회
	    List<BomDTO> bomList = pmMapper.getMaterialsByBomId(workOrderDTO.getBomId());

	    // 상태값 결정
	    boolean shortageExists = false;
	    
	    for (BomDTO bom : bomList) {
	    	BigDecimal planQty = BigDecimal.valueOf(workOrderDTO.getPlanQuantity()); 
	    	BigDecimal totalNeeded = planQty.multiply(bom.getQuantity());

	        if (bom.getStockQuantity().compareTo(totalNeeded) < 0) {
	            shortageExists = true;
	            break; // 하나라도 부족하면 재고부족으로 판단하고 반복 종료

	        }
	    }

	    // 상태값 결정
	    if (shortageExists) {
	        workOrderDTO.setWorkOrderStatus("재고부족");
	    } else {
	        workOrderDTO.setWorkOrderStatus("미착수");
	    }
	    
		// 작업지시 등록 (부족 여부에 따라 상태값 다르게)
		pmMapper.insertWorkOrder(workOrderDTO);
		
		// 생산계획 상태값 변경
		pmMapper.updatePlanStatus(workOrderDTO.getPlanId());
		
		return workOrderDTO.getWorkOrderStatus();
		
	}

	// 발주 자재정보
	public List<BomDTO> getPurchaseInfo(String workOrderId) {
		return pmMapper.getPurchaseInfo(workOrderId);
	}

	// 발주 등록
	public void insertPusrchase(List<WorkOrderShortageDTO> workOrderShortageDTO) {
		
        for (WorkOrderShortageDTO purchase : workOrderShortageDTO) {
        	purchase.setStatus("요청"); // 상태 세팅
            pmMapper.insertPusrchase(purchase); // 단일 insert 반복 호출
        }
	}

}
