package com.erp_mes.erp.attendance.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.erp_mes.erp.attendance.dto.CommuteDTO;
import com.erp_mes.erp.attendance.service.CommuteService;
import com.erp_mes.erp.groupware.entity.Notice;
import com.erp_mes.erp.groupware.repository.NoticeRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
public class mainController {
	
	private final CommuteService commuteService;
	private final NoticeRepository noticeRepository;
	
	public mainController(CommuteService commuteService, NoticeRepository noticeRepository) {
		this.commuteService = commuteService;
		this.noticeRepository = noticeRepository;
	}

	// 출퇴근관리 리스트
		@GetMapping("/main")
		public String getComuuteList(Model model,
		        @RequestParam(name = "startDate", required = false) String startDate,
		        @RequestParam(name = "endDate", required = false) String endDate) {
			
			// 로그인한 사용자 객체 꺼내기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		    String empId = null; // 변수 초기화

		    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
		        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		        empId = userDetails.getUsername(); // usernameParameter("empId") 값 그대로 들어옴
		        log.info("로그인 사용자 ID: " + empId);
		    } else {
		        // 로그인 안 된 상태라면 로그인 페이지로
		        return "redirect:/login";
//		        return "/commute/commute_list";
		    }
		    // ==============================================================================
			
		    // 기본값은 오늘
		    LocalDate today = LocalDate.now();
		    if (startDate == null || startDate.isEmpty()) startDate = today.toString();
		    if (endDate == null || endDate.isEmpty()) endDate = today.toString();
		    
		    
		    Map<String, Object> paramMap = new HashMap<>();
		    paramMap.put("empId", empId);
		    paramMap.put("startDate", startDate);
		    paramMap.put("endDate", endDate);
		    
//			log.info("startDate : " + startDate);		
//			log.info("endDate : " + endDate);		
			
			List<CommuteDTO> commuteDTOList = commuteService.getDeptCommuteList(paramMap);
			List<Notice> notices = noticeRepository.findAll();
			
			model.addAttribute("notices", notices);
			model.addAttribute("commuteDTOList", commuteDTOList);
			model.addAttribute("startDate", startDate);
			model.addAttribute("endDate", endDate);

			// =================================
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
			
		    if (commuteDTOList != null && !commuteDTOList.isEmpty()) {
		        CommuteDTO todayCommute = commuteDTOList.get(0);

		        model.addAttribute("checkInTimeStr", todayCommute.getCheckInTime() != null
		                ? todayCommute.getCheckInTime().format(formatter)
		                : "");
		        model.addAttribute("checkOutTimeStr", todayCommute.getCheckOutTime() != null
		                ? todayCommute.getCheckOutTime().format(formatter)
		                : "");
		    } else {
		        model.addAttribute("checkInTimeStr", "");
		        model.addAttribute("checkOutTimeStr", "");
		    }


			return "main";
		}
}
