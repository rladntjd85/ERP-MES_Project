package com.erp_mes.mes.plant.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.erp_mes.erp.commonCode.dto.CommonDetailCodeDTO;
import com.erp_mes.erp.commonCode.repository.CommonDetailCodeRepository;
import com.erp_mes.mes.plant.dto.EquipDTO;
import com.erp_mes.mes.plant.dto.EquipFixDTO;
import com.erp_mes.mes.plant.dto.ProcessDTO;
import com.erp_mes.mes.plant.dto.ProcessRouteDTO;
import com.erp_mes.mes.plant.entity.Equip;
import com.erp_mes.mes.plant.entity.Process;
import com.erp_mes.mes.plant.mapper.ProcessRouteMapper;
import com.erp_mes.mes.plant.service.EquipService;
import com.erp_mes.mes.plant.service.ProcessRouteService;
import com.erp_mes.mes.plant.service.ProcessService;
import com.erp_mes.mes.pm.dto.ProductDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/plant")
@RequiredArgsConstructor
@Log4j2
public class ProcessController {
	
	final private CommonDetailCodeRepository codeRepository;
	final private ProcessService proService ;
	final private ProcessRouteService proRouteService ;
	final private EquipService equipService ;
	
	
	//공정 관리 페이지
	@GetMapping("/process")
	public String process(Model model) {
		log.info("완성");	
		List<CommonDetailCodeDTO> comList = proService.findAllByPro();
		
		
		
		model.addAttribute("comList", comList);
		
		
		return "plant/process";
	}
	@GetMapping("/process_list")		//공정 현황 페이지로 변경
	public String process_list() {
		log.info("공정 현황 페이지");
		
		return "plant/process_list";
	}
	
	
	//요청 부분
	@ResponseBody
	@GetMapping("/processGrid")
	public List<Map<String, Object>> processGrid(){
		List<Map<String, Object>> equipList = proService.findAll();
		log.info("proList" + equipList.toString()); 
		
		
		return equipList;
	}
	
	@ResponseBody
	@PostMapping("/processAdd")
	public ResponseEntity<String> processAdd(ProcessDTO proDTO){
		log.info("공정 데이터를 전송합니다." + proDTO + "-------------------------------------------");
		
		proService.savePro(proDTO);
		
		return ResponseEntity.ok("success");		//리퀘스트 요청 후 성공 여부
	}
	
	
	//설비 및 이력 관리 페이지-------------------------------------------------------
	@GetMapping("/equipment")
	public String equipment(Model model) {
		List<CommonDetailCodeDTO> comList = proService.findAllByPro();
		
																																																																																																																							
		
		model.addAttribute("comList", comList);
		
		
		
		return "plant/equipment";
	}
	
	@GetMapping("/equipment_list")
	public String equipment_list() {
		log.info("설비 현황 페이지");

		
		return "plant/equipment_list";
	}
	@GetMapping("/maintenance")
	public String equip_fix(Model model) {
		List<Equip> equipList = proService.equipAll();
		
		
		
		model.addAttribute("equipList", equipList);
		
		
		return "plant/maintenance";
	}
	/* 모달창으로 변경
	@GetMapping("/fix_newForm")
	public String equip_Fix(Model model) {
		List<Equip> equipList = proService.equipAll();
		
		
		
		model.addAttribute("equipList", equipList);
		
		return "plant/fix_newForm";
	}*/
	@GetMapping("/fix_history")
	public String fix_history(Model model,
							@RequestParam("equipId")String equipId) {
		List<Map<String, Object>> equipList = equipService.findById(equipId);
		
		model.addAttribute("equipList",equipList);
		
		return "plant/fix_history";
	}
	@ResponseBody
	@GetMapping("/fixHistoryGrid")
	public String fixHistoryGrid(Model model,
							@RequestParam("equipId")String equipId) {
		List<Map<String, Object>> equipList = equipService.findById(equipId);
		
		model.addAttribute("equipList",equipList);
		
		return "plant/fix_history";
	}
	
	
	//요청 부분
	@ResponseBody
	@GetMapping("/equipGrid")
	public List<Map<String, Object>> equipGrid(){
		List<Map<String, Object>> equipList = equipService.findAll();
		log.info("equipList" + equipList.toString()); 
		
		
		return equipList;
	}
	
	@ResponseBody
	@GetMapping("/maintenanceGrid")
	public List<Map<String, Object>> maintenanceGrid(){
		List<Map<String, Object>> equipList = equipService.findAll();
		log.info("equipList" + equipList.toString()); 
		
		
		return equipList;
	}
	@ResponseBody
	@PostMapping("/equipAdd")
	public ResponseEntity<String> equipAdd(EquipDTO equipDTO){
		log.info("설비 데이터를 전송합니다." + equipDTO);
		
		equipService.saveEquip(equipDTO);
		
		return ResponseEntity.ok("success");
	}
	
	@ResponseBody
	@PostMapping("/fixAdd")
	public ResponseEntity<String> fixAdd(EquipFixDTO fix){
		log.info("fix" + fix.toString()); 
		
		
		equipService.saveFix(fix);
		
		
		
		return ResponseEntity.ok("success");
	}
	
	
	
	
	
	
	//공정 라우팅 페이지 관련 
	
	@GetMapping("/process_route")
	public String processRoute(Model model) {
		List<ProductDTO> productList = proRouteService.productList();
		List<Process> proList = proRouteService.proList();
		List<Equip> equipList = proRouteService.equipList();
		
		
		model.addAttribute("product", productList);
		model.addAttribute("proList", proList);
		model.addAttribute("equipList", equipList);
		
		
		
		return "plant/process_route";
	}
/*	모달 창으로 변경
	@GetMapping("/route_newForm")
	public String routeNewForm(Model model) {
		List<ProductDTO> productList = proRouteService.productList();
		List<Process> proList = proRouteService.proList();
		List<Equip> equipList = proRouteService.equipList();
		
		
		model.addAttribute("product", productList);
		model.addAttribute("proList", proList);
		model.addAttribute("equipList", equipList);
		
		
		return "plant/route_newForm";
	}
	*/
	
	@ResponseBody
	@PostMapping("/routeAdd")
	public ResponseEntity<String> routeAdd(ProcessRouteDTO routeDTO){
		log.info("설비 데이터를 전송합니다." + routeDTO);
		
		proRouteService.saveRoute(routeDTO);
		
		return ResponseEntity.ok("success");
	}
	
	@ResponseBody
	@GetMapping("/routeGrid")
	public List<Map<String, Object>> routeGrid(){
		log.info("Grid 데이터 요청 받음");
		
		List<Map<String, Object>> routeList = proRouteService.findAll();
		
		log.info("-------------------------------------------------------" + routeList);
		return routeList;
	}
	
	@ResponseBody
	@GetMapping("/materialInfo")
	public List<Map<String, Object>> materialInfo(@RequestParam("productId") String productId){
		
		log.info("Grid 데이터 요청 받음");
		
		List<Map<String, Object>> material = proRouteService.findMaterialByProductId(productId);
		
		log.info("-------------------------------------------------------" + material);
		return material;
	}
	
	
}
