package com.erp_mes.erp.config.util;

import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class HolidayService {

    @Value("${service.key}") // application.properties 파일에 저장된 API 키를 주입받습니다.
    private String serviceKey;

    private static final String API_URL = "https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public HolidayService(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
    }

    public List<HolidayDTO> getHolidays(Integer year, Integer month) {
    	
    	int finalYear = (year != null) ? year : Year.now().getValue();
        int finalMonth = (month != null) ? month : java.time.MonthDay.now().getMonthValue();

        String url = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("serviceKey", serviceKey)
                .queryParam("solYear", finalYear) // ⭐ finalYear 변수 사용
                .queryParam("solMonth", String.format("%02d", finalMonth)) // ⭐ finalMonth 변수 사용
                .queryParam("_type", "json")
                .build()
                .toUriString();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String jsonString = response.getBody();

            // API 호출 시 간혹 HTML 에러 페이지를 반환하는 경우를 대비해 JSON 파싱 전에 문자열을 확인합니다.
            if (jsonString == null || jsonString.startsWith("<")) {
                throw new Exception("API 응답이 유효한 JSON 형식이 아닙니다.");
            }

            JsonNode rootNode = objectMapper.readTree(jsonString);
            JsonNode itemNode = rootNode.path("response").path("body").path("items").path("item");

            List<HolidayDTO> holidays = new ArrayList<>();
            if (itemNode.isArray()) {
                holidays.addAll(objectMapper.convertValue(itemNode, new TypeReference<List<HolidayDTO>>() {}));
            } else if (!itemNode.isMissingNode()) { // 공휴일이 하나일 때
                holidays.add(objectMapper.convertValue(itemNode, HolidayDTO.class));
            }

//            log.info(">>>>>>>>>>>"+finalYear + "/" + finalMonth + " 조회 결과 : " + holidays);
            
            // isHoliday가 "Y"인 경우만 필터링하여 반환
            return holidays.stream()
                    .filter(h -> "Y".equals(h.getIsHoliday()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}