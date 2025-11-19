package com.erp_mes.erp.personnel.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.erp_mes.erp.personnel.dto.PersonnelImgDTO;
import com.erp_mes.erp.personnel.entity.Personnel;
import com.erp_mes.erp.personnel.entity.PersonnelImg;
import com.erp_mes.erp.personnel.mapper.PersonnelImgMapper;
import com.erp_mes.erp.personnel.repository.PersonnelImgRepository;
import com.erp_mes.erp.personnel.repository.PersonnelRepository;
import com.erp_mes.erp.personnel.util.FileUtils;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성
@Log4j2
public class PersonnelImgService {

	@Value("${file.uploadBaseLocation}")
	private String uploadBaseLocation;
	
	@Value("${file.itemImgLocation}")
	private String itemImgLocation;
	
	private final PersonnelImgRepository personnelImgRepository;
	private final PersonnelImgMapper personnelImgMapper;
	private final PersonnelRepository personnelRepository;
	
	
	@Autowired
	private FileUtils fileUtils;
	public void registImg(Personnel personnel, MultipartFile empImg) throws IOException {
		
		// 원본 파일명 추출 
		String originalFileName = empImg.getOriginalFilename();
		
		//파일 이름 중복방지 대책
		String fileName = UUID.randomUUID().toString() + "_" + originalFileName;
		
		//기본 경로 + 상세 경로 서브 디렉토리 결합하여 디렉토리 생성	파일명을 사원번호로 저장되겠금  ex) personnel/image/2025082601/ham.png
		Path uploadDir = Paths.get(uploadBaseLocation, itemImgLocation, personnel.getEmpId()).toAbsolutePath().normalize();
		
		
		//생성된 Path 객체에 해당하는 디렉토리가 실제 디렉토리로 존재하지 않을 경우 해당 디렉토리 생성
		if(!Files.exists(uploadDir)) { 
			Files.createDirectories(uploadDir); // 하위 경로를 포함한 경로 상의 모든 디렉토리 생성
		}
		
		
		// 디렉토리와 파일명 결합하여 Path 객체 생성
		// => 기존 경로를 담고 있는 Path 객체의 resolve() 메서드를 사용하여 기존 경로에 파일명 추가
		Path uploadPath = uploadDir.resolve(fileName);
		
		
		// 임시 경로에 보관되어 있는 첨부파일 1개를 실제 업로드 경로로 이동
		empImg.transferTo(uploadPath);
		
		

		//이미지 존재하지 않을 경우 img_id 값이랑 emp_id 넣어줌
		PersonnelImg perImg = personnelImgRepository.findByPersonnel_EmpId(personnel.getEmpId())		//findById 는 조회를 못해서 조회가능하도록 바꿈
 	            .orElseGet(() -> {
 	            	List<PersonnelImg> imgId = personnelImgRepository.findAll();
	 	   			int number = imgId.size() + 1;
	 	   			String id = String.format("IMG%03d", number);
 	            
	 	   			PersonnelImg newImg = new PersonnelImg();
	 	   			newImg.setImgId(id);
	 	   			newImg.setPersonnel(personnel);
	 	   			return newImg;	
 	            });
		
		perImg.setImgName(fileName);
		perImg.setFileName(originalFileName);
		perImg.setImgLocation(itemImgLocation + "/" + personnel.getEmpId());
		personnelImgRepository.save(perImg);
	}

	// 이미지 등록 하나로 합치고 이미지 불러오는 로직 추가 
	
	public ResponseEntity<Resource> getImgLocation(String empId) {
		Personnel emp = personnelRepository.findById(empId)
				.orElseThrow(() -> new EntityNotFoundException("해당 이미지가 존재하지 않습니다!"));
		
		PersonnelImg perImg = personnelImgRepository.findByPersonnel_EmpId(emp.getEmpId())
				.orElseThrow(() -> new EntityNotFoundException("해당 이미지가 존재하지 않습니다!"));
		
//		log.info("이미지 정보 불러오기" + perImg.toString());
		
		ResponseEntity<Resource> responseEntity = fileUtils.showImg(PersonnelImgDTO.fromEntity(perImg));
		return responseEntity;
	}
	
	
}
