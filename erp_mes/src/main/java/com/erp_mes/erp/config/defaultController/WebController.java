package com.erp_mes.erp.config.defaultController;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.erp_mes.erp.commonCode.entity.CommonDetailCode;
import com.erp_mes.erp.commonCode.service.CommonCodeService;
import com.erp_mes.erp.personnel.dto.PersonnelLoginDTO;

@Controller
public class WebController {

	private final CommonCodeService commonCodeService;

	public WebController(CommonCodeService commonCodeService) {
		this.commonCodeService = commonCodeService;
	}

	@RequestMapping("/")
	public String login(@CookieValue(value = "remember-id", required = false) String rememberId, Model model,
			@AuthenticationPrincipal PersonnelLoginDTO loginDTO) {
		
		
		
		//로그인 화면 진입시 로그인 정보가 존재할경우 메인으로 리다이렉트
		if(loginDTO != null) {
			// 쿠키값 Model 객체에 추가
			return "redirect:/main";
		}else {
			
			model.addAttribute("rememberId", rememberId);
			model.addAttribute("rememberChecked", true);
			return "login";
		}
				
		
	}

	@RequestMapping("/main")
	public String scheduleList(Model model, @AuthenticationPrincipal PersonnelLoginDTO personnelLoginDTO) {

		String empDeptName = null;
		boolean isAdmin = false;
		
		if (personnelLoginDTO == null) {
	        return "redirect:/";  // 로그인 안되어있으면 로그인 페이지로
	    }

		String empDeptId = personnelLoginDTO.getEmpDeptId();
		if (commonCodeService != null) {
			CommonDetailCode deptCode = commonCodeService.getCommonDetailCode(empDeptId);
			if (deptCode != null) {
				empDeptName = deptCode.getComDtNm();
			}
		}

		if (personnelLoginDTO.getEmpLevelId().equals("AUT001")) {
			isAdmin = true;
			List<CommonDetailCode> allDepartments = commonCodeService.findByComId("DEP");
			model.addAttribute("allDepartments", allDepartments);
		}

		model.addAttribute("currentEmpId", personnelLoginDTO.getEmpId());
		model.addAttribute("currentEmpName", personnelLoginDTO.getName());
		model.addAttribute("isAdmin", isAdmin);
		model.addAttribute("empDeptName", empDeptName);
		model.addAttribute("empDeptId", empDeptId);
		model.addAttribute("empName", personnelLoginDTO.getName());

		return "main";
	}

	@RequestMapping("register")
	public String regist() {

		return "register";
	}

	@RequestMapping("blank")
	public String blank() {

		return "blank";
	}

}