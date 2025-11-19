package com.erp_mes.erp.personnel.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.erp_mes.erp.personnel.dto.PersonnelImgDTO;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class FileUtils {
	
	@Value("${file.uploadBaseLocation}")
	private String uploadBaseLocation;
	
	@Value("${file.itemImgLocation}")
	private String itemImgLocation;

	public ResponseEntity<Resource> showImg(PersonnelImgDTO perImg) {		//이미지 경로 불러와서 이미지 형식으로 변환 해서 사용할수 있게 처리 
		Path basePath = Paths.get(uploadBaseLocation, perImg.getImgLocation()).toAbsolutePath().normalize();
		
		// --------------------------------------------------------------------
		// 참고) 만약, 외부로부터 다운로드 경로 중의 일부라도 전송받을 경우 경로에 대한 조작(Directory Traversal) 등이 일어날 수 있음
		// ex) ItemImgDTO 내의 imgName 등이 외부로부터 전달받은 경로 또는 파일명이라고 가정
		log.info(perImg.getImgName()+">>>>>>>>>>>>>>>>>>@@@@@@@@@@@@@@@@@@@@@@");
		Path targetPath = basePath.resolve(perImg.getImgName())
									.normalize();
		
		
		// 보안 체크 사항 - targetPath 값의 시작 부분이 basePath 값과 같은지 판별
		if(!targetPath.startsWith(basePath)) {
			throw new SecurityException("허용되지 않은 경로 접근 시도!");
		}
		// --------------------------------------------------------------------
		try {
			// 이미지 대한 UrlResource 객체 생성
			Resource resource = new UrlResource(targetPath.toUri());	
			
			
			log.info("targetPath={}", targetPath);
			log.info("exists={}, readable={}", Files.exists(targetPath), Files.isReadable(targetPath));

			
			// 이미지 경로 및 파일 존재여부 판별하고, 파일 접근 가능 여부도 체크
			if(Files.notExists(targetPath) || !Files.isReadable(targetPath)) {
//				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "허용되지 않은 경로 접근 시도!");
			}
			
			// 파일의 MIME 타입(= 컨텐츠 타입) 설정(실제 파일로부터 타입 알아내기)
			String contentType = Files.probeContentType(targetPath);
//			System.out.println("contentType : " + contentType);
			
			// 컨텐츠 타입 조회 실패 시 기본적인 바이너리 파일 형태로 타입 강제 고정
			if(contentType == null) {
				contentType = "application/octet-stream";
			}
			
			// 파일명이 한글, 공백 등이 포함된 파일일 경우 ContentDisposition 객체를 통해 UTF-8 타입 인코딩 설정
			String originalFileName = perImg.getFileName();
			ContentDisposition contentDisposition = ContentDisposition.builder("inline") //   이미지 형식으로 객체 생성
					.filename(originalFileName, StandardCharsets.UTF_8) // 파일명을 UTF-8 타입으로 인코딩
					.build();
			
			// 응답데이터를 관리하는 ResponseEntity 객체 생성 및 리턴
			return ResponseEntity.ok() // 정상적인 응답 형태로 설정하기 위해 ok() 메서드 호출
					.contentType(MediaType.parseMediaType(contentType)) // 실제 파일로부터 읽어들인 컨텐츠 타입 설정
					.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString()) // ContentDisposition 객체 활용하여 헤더 설정
					.body(resource);
		} catch (IOException e) { // MalformedURLException 을 포함하여 공통 예외 처리
			log.error(">>>>>>>>>>>>>>> 파일 처리 실패!");
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 처리 실패!");
		}
		
		
		
	}

}
