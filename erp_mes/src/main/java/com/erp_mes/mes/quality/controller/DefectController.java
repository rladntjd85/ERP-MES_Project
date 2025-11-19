package com.erp_mes.mes.quality.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.erp_mes.erp.commonCode.entity.CommonDetailCode;
import com.erp_mes.erp.commonCode.service.CommonCodeService;
import com.erp_mes.mes.pop.dto.DefectDTO;
import com.erp_mes.mes.quality.mapper.QualityMapper;
import com.erp_mes.mes.quality.service.DefectqcService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/defect")
@Log4j2
@RequiredArgsConstructor
public class DefectController {

	private final DefectqcService defectqcService;
	private final CommonCodeService commonCodeService;
	private final QualityMapper qualityMapper;

	@GetMapping("")
	public String getMethodName(Model model) {
		List<CommonDetailCode> defectTypes = commonCodeService.findByComId("DEFECT");
		model.addAttribute("defectTypes", defectTypes);

		return "qc/defect";
	}

	// 불량 내역 상세 조회를 위한 API 엔드포인트
	@GetMapping("/api/defect-history")
	@ResponseBody
	public Map<String, Object> getDefectHistory(
			@RequestParam(required = false) String defectLocation,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "perPage", defaultValue = "100") int perPage) {

		Map<String, Object> response = new HashMap<>();
		try {
			// Service 호출: 불필요한 null 파라미터 대신 defectLocation만 전달
			List<DefectDTO> allDefects = defectqcService.getDefectHistory(defectLocation);

			// TUI Grid를 위한 페이징 처리
			int totalCount = allDefects.size();
			int start = (page - 1) * perPage;
			int end = Math.min(start + perPage, totalCount);

			List<DefectDTO> pagedDefects = allDefects.subList(start, end);

			Map<String, Object> data = new HashMap<>();
			data.put("contents", pagedDefects);
			data.put("pagination", Map.of("totalCount", totalCount));

			response.put("result", true);
			response.put("data", data);
		} catch (Exception e) {
			log.error("Failed to fetch defect history: {}", e.getMessage());
			response.put("result", false);
			response.put("data", Map.of());
		}
		return response;
	}

	// 통계데이터 제공하는 API
	@GetMapping("/api/defect-dashboard")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getDefectDashboard() {
		try {
			// Service에서 집계된 대시보드 데이터를 가져옴
			Map<String, Object> dashboardData = defectqcService.getDefectDashboardData();
			return new ResponseEntity<>(dashboardData, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Failed to fetch defect dashboard data: {}", e.getMessage());
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("error", "데이터 조회 중 오류 발생");
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}