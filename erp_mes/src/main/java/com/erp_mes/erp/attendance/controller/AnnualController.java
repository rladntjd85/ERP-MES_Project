package com.erp_mes.erp.attendance.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.erp_mes.erp.approval.constant.ApprStatus;
import com.erp_mes.erp.approval.entity.Appr;
import com.erp_mes.erp.attendance.dto.AnnualDTO;
import com.erp_mes.erp.attendance.entity.Annual;
import com.erp_mes.erp.attendance.service.AnnualService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;



@Controller
@RequestMapping("/attendance")
@Log4j2
@RequiredArgsConstructor
public class AnnualController {
	
	private final AnnualService annService;
	
	
// ============================================================
	
	
	// 화면 + 내 연차 조회하기 
	@GetMapping("/annualList")
	public String annualList(@RequestParam(value="year", required=false) String annYear, Model model,
            Authentication authentication) {
		
		String empId = authentication.getName(); // 로그인한 사원 empId
	    if(annYear == null) {
	        annYear = String.valueOf(java.time.LocalDate.now().getYear()); // 기본: 올해
	    }
		
	    annService.AnnUpdate(); // 연차 변경
	    
		AnnualDTO myAnn = annService.myAnnual(empId, annYear); // 내 연차 조회
		model.addAttribute("myAnn", myAnn);
		return "commute/annual_list";
	}
	
	
	// 모든 사원 연차 내역(무한스크롤)
	@GetMapping("/annListAll/{annYear}")
	@ResponseBody
	public Map<String, Object> annListAll(@PathVariable("annYear") String annYear, 
			@RequestParam(value = "page", defaultValue = "0") int page, 
			@RequestParam(value = "size", defaultValue = "20") int size,
			Authentication authentication) {
		
		String empId = authentication.getName(); 
		
		Page<AnnualDTO> annPage = annService.getAllAnnByYearPaged(empId ,annYear, PageRequest.of(page, size));
		
		Map<String, Object> result = new HashMap<>();
		result.put("totalPages", annPage.getTotalPages());
		result.put("page", page);
		result.put("data", annPage.getContent());
		
		return result;
	}

	
	// 검색창
	@GetMapping("/annSearch")
	@ResponseBody
	public List<AnnualDTO> annSearch(@RequestParam(value = "keyword", defaultValue = "") String keyword) {
		return annService.searchAnn(keyword);
	}

	// 오늘 연차자 조회
	@GetMapping("/todayAnn") 
	@ResponseBody
	public List<AnnualDTO> todayAnn() {
		return annService.getTodayAnn();
	}


	

}