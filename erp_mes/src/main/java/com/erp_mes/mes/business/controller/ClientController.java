package com.erp_mes.mes.business.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.erp_mes.erp.personnel.dto.PersonnelLoginDTO;
import com.erp_mes.mes.business.dto.ClientDTO;
import com.erp_mes.mes.business.service.ClientService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@Controller
@RequestMapping("/business")
@RequiredArgsConstructor
@Log4j2
public class ClientController {
	
	private final ClientService clientService;
//	private final RestTemplate restTemplate = new RestTemplate();

//    @Value("${data.go.kr.api.key}") // yml에 저장
//    private String serviceKey;
	
	// 화면 이동과 데이터 조회로 분리한 이유는 테스트와 유지보수를 편하게 하기 위해 + 책임 분리
	// 거래처 화면
	@GetMapping("client")
	public String client(Model model, @AuthenticationPrincipal PersonnelLoginDTO userDetails) {

		String userLevelId = userDetails.getEmpLevelId();
        
        boolean isAutLevel = "AUT001".equals(userLevelId);
        model.addAttribute("isAUTLevel", isAutLevel);
		
		return "business/client";
	}
	
	// 거래처 전체 목록 조회
    @GetMapping("/api/clients")
    @ResponseBody
    public List<ClientDTO> getAllClients() {
        log.info("거래처 전체 목록 조회 요청");
        
        return clientService.getAllClients();
    }
	
    // 거래처 등록
	@PostMapping("/api/clients/submit")
    public ResponseEntity<?> createClient(@RequestBody ClientDTO clientDto) {
		log.info("컨트롤러 수행: {}", clientDto); 
        try {
            clientService.saveClient(clientDto);
            log.info("컨트롤러 수행했고 성공");
            return ResponseEntity.ok(Map.of("status", "success", "message", "Client created successfully"));
        } catch (Exception e) {
			log.error("컨트롤러 수행 실패: {}", e.getMessage()); 
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
	
	// 거래처 수정
	@PutMapping("/api/clients/update/{clientId}")
	public ResponseEntity<?> updateClient(@RequestBody ClientDTO clientDto) {
	    try {
	        clientService.updateClient(clientDto);
	        return ResponseEntity.ok(Map.of("status", "success", "message", "Client updated successfully"));
	    } catch (Exception e) {
	        return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
	    }
	}
	
	// 매출사 거래처 목록
	@GetMapping("/api/clients/order-type")
	@ResponseBody
	public List<ClientDTO> getOrderClients() {
	    return clientService.getOrderClients("ORDER", "ACTIVE");
	}
	
//	// 거래처 등록, 수정 시 사업자등록번호 검증
//	@PostMapping("/api/validateBizNo")
//    public ResponseEntity<?> validateBusinessNumber(@RequestBody Map<String, String> request) {
//        String bizNo = request.get("businessNumber");
//        log.info("사업자번호 검증 요청: {}", bizNo);
//        log.info("serviceKey = {}", serviceKey);
//        
//        if (serviceKey == null || serviceKey.isEmpty()) {
//            log.error("환경설정(yml/properties)에서 data.go.kr.api.key를 로드하지 못했습니다.");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("API 서비스 키 설정 오류.");
//        }
//        log.info("로드된 serviceKey (길이): {}", serviceKey.length()); // 키의 존재 여부만 확인
//
//        // 공공데이터 API URL
//        String url = "https://api.odcloud.kr/api/nts-businessman/v1/status"
//                + "?serviceKey=" + URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);
//
//        // API 요청 body
//        Map<String, Object> body = new HashMap<>();
//        body.put("b_no", Collections.singletonList(bizNo)); // 사업자번호 리스트
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
//
//        try {
//            // 💡 2. URL 인코딩은 StandardCharsets.UTF_8로 변경하여 IOException을 던지지 않도록 처리
//            String encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8.toString());
//
//            String url = "https://api.odcloud.kr/api/nts-businessman/v1/status"
//                    + "?serviceKey=" + serviceKey;
//
//            // RestTemplate 호출 시 에러가 가장 많이 발생함
//            ResponseEntity<String> response = restTemplate.exchange(
//                    url, HttpMethod.POST, entity, String.class);
//            
//            // 응답 상태 코드가 2xx가 아닌 경우 처리
//            if (!response.getStatusCode().is2xxSuccessful()) {
//                log.error("공공데이터 API 응답 실패. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body("공공데이터 API 호출 실패. 응답 코드: " + response.getStatusCodeValue());
//            }
//            
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode json = mapper.readTree(response.getBody());
//            log.info("API 응답 JSON 파싱 성공: {}", json.toPrettyString()); // 디버깅용 로그 추가
//
//            
//            return ResponseEntity.ok(response.getBody());
//
//        } catch (org.springframework.web.client.HttpClientErrorException | org.springframework.web.client.HttpServerErrorException e) {
//            // RestTemplate 호출 중 4xx (클라이언트 에러) 또는 5xx (서버 에러) 발생 시
//            log.error("공공데이터 API 호출 HTTP 에러: {}", e.getResponseBodyAsString(), e);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body("사업자번호 검증 API 호출 HTTP 에러: " + e.getMessage());
//
//        } catch (Exception e) {
//            // 그 외 모든 예외 (IO, JSON 파싱 등)
//            log.error("사업자번호 검증 중 예외 발생: {}", e.getMessage(), e);
//            // 클라이언트에게는 자세한 에러 메시지를 숨기고 일반적인 메시지를 전달
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("API 호출 중 서버 내부 오류 발생. 로그를 확인하세요.");
//        }
//        
//        try {
//            ResponseEntity<String> response = restTemplate.exchange(
//                    url, HttpMethod.POST, entity, String.class);
//            
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode json = mapper.readTree(response.getBody());
//
//            return ResponseEntity.ok(response.getBody());
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("API 호출 실패: " + e.getMessage());
//        }
//    }
	
}
