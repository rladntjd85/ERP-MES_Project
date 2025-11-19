package com.erp_mes.erp.groupware.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.erp_mes.erp.commonCode.entity.CommonDetailCode;
import com.erp_mes.erp.commonCode.service.CommonCodeService;
import com.erp_mes.erp.groupware.dto.NoticeDTO;
import com.erp_mes.erp.groupware.entity.Notice;
import com.erp_mes.erp.groupware.repository.NoticeRepository;
import com.erp_mes.erp.groupware.service.NoticeService;
import com.erp_mes.erp.personnel.dto.PersonnelLoginDTO;
import com.erp_mes.erp.personnel.entity.Personnel;
import com.erp_mes.erp.personnel.repository.PersonnelRepository;

import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/notice")
@Log4j2
public class NoticeController {

	private final NoticeRepository noticeRepository;
	private final NoticeService noticeService;
	private final PersonnelRepository personnelRepository;
	private final CommonCodeService commonCodeService;

	public NoticeController(NoticeRepository noticeRepository, NoticeService noticeService,
			PersonnelRepository personnelRepository, CommonCodeService commonCodeService) {
		super();
		this.noticeRepository = noticeRepository;
		this.noticeService = noticeService;
		this.personnelRepository = personnelRepository;
		this.commonCodeService = commonCodeService;
	}

	// 공지사항 목록 페이지를 보여주는 메서드
	@GetMapping("")
	public String noticeList(Model model, @AuthenticationPrincipal PersonnelLoginDTO personnelLoginDTO) {
		log.info("NoticeController noticeList()");

		// 1. 전체 공지사항을 조회하여 모델에 추가 (기존 코드와 동일)
		List<Notice> notices = noticeRepository.findAll();
		model.addAttribute("notices", notices);

		// 2. 현재 로그인한 사용자 정보 가져오기
		String empDeptName = null;

		if (personnelLoginDTO != null) {
			// 부서 ID로 부서명 조회
			if (commonCodeService != null) {
				CommonDetailCode deptCode = commonCodeService.getCommonDetailCode(personnelLoginDTO.getEmpDeptId());
				if (deptCode != null) {
					empDeptName = deptCode.getComDtNm();
				}
			}
			log.info("로그인한 사용자의 부서 ID: " + personnelLoginDTO.getEmpDeptId());
		}

		// 3. 로그인한 사용자의 부서 ID로 부서별 공지사항을 조회
		List<Notice> deptNotices = new ArrayList<Notice>();
		if (empDeptName != null) {
			// "부서별" 타입의 공지사항 중, 현재 사용자의 부서ID와 일치하는 목록을 조회
			deptNotices = noticeRepository.findByEmpDeptIdAndNotType(personnelLoginDTO.getEmpDeptId(), empDeptName);
		}
		model.addAttribute("deptNotices", deptNotices);
		model.addAttribute("empDeptName", empDeptName);
		model.addAttribute("currentUserId", personnelLoginDTO.getEmpId());
		model.addAttribute("currentUsername", personnelLoginDTO.getName());

		return "gw/notice";
	}

	// 공지사항 등록 페이지를 보여주는 메서드
	@GetMapping("/ntcWrite")
	public String ntcWrite(Model model, @AuthenticationPrincipal PersonnelLoginDTO personnelLoginDTO) {
		log.info("NoticeController ntcWrite()");

		boolean isAdmin = personnelLoginDTO.getEmpLevelId().equals("AUT001");
		String empDeptName = personnelLoginDTO.getEmpDeptName();

		// 드롭다운에 표시할 공지 유형 목록 생성
		List<String> noticeTypes = new ArrayList<>();

		if (isAdmin) {
			noticeTypes.add("전체공지");
			List<CommonDetailCode> allDepartments = commonCodeService.findByComId("DEP");
			for (CommonDetailCode dept : allDepartments) {
				noticeTypes.add(dept.getComDtNm());
			}
		} else if (empDeptName != null) {
			noticeTypes.add(empDeptName);
		}

		// 모델에 데이터 추가
		NoticeDTO noticeDTO = new NoticeDTO();
		noticeDTO.setEmpId(personnelLoginDTO.getName());

		model.addAttribute("noticeDTO", noticeDTO);
		model.addAttribute("empName", personnelLoginDTO.getName());
		model.addAttribute("departments", commonCodeService.findByComId("DEP"));
		model.addAttribute("isAdmin", isAdmin);

		if (!isAdmin) {
			model.addAttribute("empDeptName", empDeptName);
		} else {
			model.addAttribute("noticeTypes", noticeTypes);
		}
		return "gw/ntcWrite";
	}

	// 공지사항 등록 폼에서 데이터가 제출되면 호출되는 메서드
	@PostMapping("/save")
	public String saveNotice(NoticeDTO noticeDTO, @AuthenticationPrincipal PersonnelLoginDTO personnelLoginDTO) {
		log.info("NoticeController saveNotice()");

		Notice notice = new Notice();

		// 로그인한 사용자의 ID로 Personnel 엔티티를 조회하여 Notice에 설정
		if (personnelLoginDTO != null) {
			Personnel personnel = personnelRepository.findById(personnelLoginDTO.getEmpId()).orElse(null);
			if (personnel != null) {
				// Notice 엔티티는 Personnel 객체를 가짐
				notice.setEmployee(personnel);
			}
		}

		// 3. NoticeDTO의 다른 필드를 Notice 엔티티에 설정
		notice.setNotTitle(noticeDTO.getNotTitle());
		notice.setNotContent(noticeDTO.getNotContent());
		notice.setNotType(noticeDTO.getNotType());
		notice.setCreateAt(LocalDate.now());
		notice.setUpdateAt(LocalDate.now());

		noticeRepository.save(notice);

		return "redirect:/notice";
	}

	// 공지 수정
	@PostMapping("/ntcUpdate")
	@ResponseBody
	public Map<String, Object> updateNotice(@RequestBody Notice notice) {

		Map<String, Object> response = new HashMap<>();
		try {
			noticeService.updateNotice(notice);
			response.put("success", true);
			response.put("message", "공지사항이 성공적으로 수정되었습니다.");
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "수정 중 오류가 발생했습니다.");
			log.error("Error updating notice", e);
		}

		return response;
	}

	// 공지사항 삭제 처리
	@DeleteMapping("/ntcDelete")
	@ResponseBody
	public Map<String, Object> deleteNotice(@RequestBody Map<String, Long> payload) {
		Map<String, Object> response = new HashMap<>();
		try {
			Long id = payload.get("notId");
			noticeService.deleteNoticeById(id);
			response.put("success", true);
			response.put("message", "공지사항이 성공적으로 삭제되었습니다.");
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "삭제 중 오류가 발생했습니다.");
			log.error("Error deleting notice", e);
		}
		return response;
	}
}