package com.erp_mes.mes.lot.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.erp_mes.erp.config.util.SessionUtil;
import com.erp_mes.mes.lot.constant.LotDomain;
import com.erp_mes.mes.lot.dto.LotDTO;
import com.erp_mes.mes.lot.dto.LotDetailDTO;
import com.erp_mes.mes.lot.repository.LotRepository;
import com.erp_mes.mes.lot.service.LotService;
import com.erp_mes.mes.lot.trace.TrackLot;
import com.erp_mes.mes.pop.dto.WorkResultDTO;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/lot")
@Log4j2
@RequiredArgsConstructor
public class LotController {

	private final LotService lotService;
	
	private final LotRepository lotRepository;

	//로트 추적 리스트
	@GetMapping("")
	public String showLotTrackingList(@RequestParam(value = "page", defaultValue = "0") int page, 
			@RequestParam(value = "size", defaultValue = "20") int size, Model model) {
		
		List<LotDTO> lotDTOList = lotService.getLotTrackingList(page, size);
		model.addAttribute("lotDTOList", lotDTOList);
		
		return "lot/lot_list";
	}
	
	//자식 공정 가져오기
	@PostMapping("/getChildren/process/{productId}")
	@ResponseBody // ★ 반드시 붙이기
	public List<Map<String, Object>> getProcess(@PathVariable("productId") String productId){
	    List<Map<String, Object>> children = lotService.findByProcess(productId);
//		log.info("proList" + children.toString()); 
		return children;
	}
	
	//자식 자재 가져오기
	@PostMapping("/getChildren/material/{workOrderId}")
	@ResponseBody // ★ 반드시 붙이기
	public List<LotDetailDTO> getMaterial(@PathVariable("workOrderId") String workOrderId){
	    List<LotDetailDTO> children = lotService.findByMaterial(workOrderId);
		return children;
	}
	
	//설비 가져오기
	@PostMapping("/getChildren/equipment/{productId}")
	@ResponseBody // ★ 반드시 붙이기
	public List<LotDetailDTO> getEquipment(@PathVariable("productId") String productId){
	    List<LotDetailDTO> children = lotService.findByEquipment(productId);
		return children;
	}
	
	//상세정보
	@PostMapping("/getLotDetail/{workOrderId}")
	@ResponseBody // ★ 반드시 붙이기
	public List<WorkResultDTO> getLotDetail(@PathVariable("workOrderId") Long workOrderId){
	    List<WorkResultDTO> orderDetail = lotService.findDetail(workOrderId);
		return orderDetail;
	}
}