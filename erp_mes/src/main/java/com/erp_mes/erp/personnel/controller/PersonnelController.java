package com.erp_mes.erp.personnel.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.erp_mes.erp.commonCode.dto.CommonDetailCodeDTO;
import com.erp_mes.erp.commonCode.entity.CommonDetailCode;
import com.erp_mes.erp.commonCode.service.CommonCodeService;
import com.erp_mes.erp.personnel.dto.PersonnelDTO;
import com.erp_mes.erp.personnel.dto.PersonnelImgDTO;
import com.erp_mes.erp.personnel.dto.PersonnelLoginDTO;
import com.erp_mes.erp.personnel.dto.PersonnelTransferDTO;
import com.erp_mes.erp.personnel.service.PersonnelImgService;
import com.erp_mes.erp.personnel.service.PersonnelService;
import com.erp_mes.erp.personnel.service.PersonnelTransferService;
import com.erp_mes.erp.personnel.util.FileUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/personnel")
@RequiredArgsConstructor
@Log4j2
public class PersonnelController {
	
    private final PersonnelService personnelService;
    private final PersonnelTransferService personnelTransferService;
	private final CommonCodeService commonCodeService;
    private final PersonnelImgService personnelImgService;
    private final FileUtils fileUtils;
    //이미지 경로 
    @Value("${file.uploadBaseLocation}")
	private String uploadBaseLocation;
	
	@Value("${file.itemImgLocation}")
	private String itemImgLocation;
    
	@GetMapping("/current")
	public String current(Model model) {
		List<PersonnelDTO> personnels = personnelService.getAllPersonnels();
		
		//인사현황에서 인사팀 또는 최상위 계정이 아닐경우 개인의 정보만 보여줄 수 있도록 고침
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String currentUserId = null; // ✅ 현재 로그인 사용자의 ID를 담을 변수
	    String currentUsername = null; // ⭐현재 로그인 사용자의 username

		// 사용자 정보에서 부서 ID를 담을 변수
		String empDeptId = null;
		String empDeptName = null;
		
		String empLevelId = null;

		// 사용자가 로그인되어 있고, UserDetails 객체가 PersonnelLoginDTO 타입인지 확인
		if (authentication != null && authentication.getPrincipal() instanceof PersonnelLoginDTO) {
			PersonnelLoginDTO personnelLoginDTO = (PersonnelLoginDTO) authentication.getPrincipal();
			currentUserId = personnelLoginDTO.getEmpId();
			currentUsername = personnelLoginDTO.getUsername();
			empDeptId = personnelLoginDTO.getEmpDeptId();
			empLevelId = personnelLoginDTO.getEmpLevelId();
			// 부서 ID로 부서명을 조회하는 로직
			// 이전에 CommonCodeService에 추가한 메서드를 활용해야 합니다.
//			log.info("로그인한 사용자의 부서 ID: " + empDeptId);
			if (commonCodeService != null) {
				CommonDetailCode deptCode = commonCodeService.getCommonDetailCode(empDeptId);
				if (deptCode != null) {
					empDeptName = deptCode.getComDtNm(); // ✅ 부서명 변수에 값 할당
				}
			}
			
			
		}
		PersonnelDTO loginEmp = null;

		for(PersonnelDTO a : personnels) {
			
			if(currentUserId.equals(a.getEmpId()) ) {
				loginEmp = a;
				break;
			}
			
		}
		
//		log.info("로그인 한 사람 정보 : " + loginEmp.toString());
		
		
		if("DEP001".equals(loginEmp.getDeptId()) || "AUT001".equals(loginEmp.getLevId())) {
			model.addAttribute("personnels", personnels); 
			
		}else {
			model.addAttribute("personnels", loginEmp); 
			
		}

		return "hrn/personnelCurrent";
	}
	//이미지 요청 리퀘스트 제어 
	@GetMapping("/showImg/{empId}")
	public ResponseEntity<Resource> getImg(@PathVariable("empId") String empId) {
//		log.info("사원 번호 : " + empId);
	
		
		
		return personnelImgService.getImgLocation(empId);
	}
	
	@GetMapping("/loginShowImg/{empId}")
	public ResponseEntity<Resource> getImg2(@PathVariable("empId") String empId) {
			
//			return img;
//			 try catch 문으로 변경
		try {
			ResponseEntity<Resource> img = personnelImgService.getImgLocation(empId);
			return img;
		} catch (Exception e)  {
			log.error("이미지 로드 실패, 기본 이미지 반환: " + empId, e);
			
			Resource resource = new ClassPathResource("static/bootstrap/img/undraw_profile.svg");
			return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("image/svg+xml")) // 명확한 MIME 타입 지정
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(resource);
					 	
		}
	}
	// 인사현황 데이터 응답
	@GetMapping("/api/personnels")
    public ResponseEntity<List<PersonnelDTO>> getAllPersonnels() {
        List<PersonnelDTO> personnels = personnelService.getAllPersonnels();
        
        return ResponseEntity.ok(personnels);
    }
	

	@GetMapping("/detailInfo")
    public String detailInfo(@RequestParam("empId") String empId, Model model) {
        // Service를 통해 해당 사원의 상세 정보를 가져와 모델에 추가
        Optional<PersonnelDTO> personnelOpt = personnelService.getPersonnelDetails(empId);

        if (personnelOpt.isPresent()) {
            model.addAttribute("personnel", personnelOpt.get());
        } else {
            // 사원 정보가 없을 경우 리스트 페이지로 리다이렉트
            return "redirect:/personnel/current";
        }

        // 부서 및 직책 리스트도 모델에 추가 (select 박스 생성을 위해 필요)
        List<CommonDetailCodeDTO> departments = personnelService.getAllDepartments();
        model.addAttribute("departments", departments);
//        log.info("personnel : " + personnelOpt);
//        log.info("부서 정보 : " + departments.toString());

        List<CommonDetailCodeDTO> position = personnelService.getAllPositions();
        model.addAttribute("position", position);
//        log.info("직책 정보 : " + position.toString());
    	//추가 된 부분 ----------------------------------------------------
    	//재직 리스트
		List<CommonDetailCodeDTO> status = personnelService.getAllStatus();
		model.addAttribute("status", status);
//        log.info("상태 정보 : " + status.toString());

		//보안등급
		List<CommonDetailCodeDTO> level = personnelService.getAllLevel();
		model.addAttribute("level", level);
//        log.info("보안등급 정보 : " + level.toString());
        
//        log.info("사원등급 정보 : " + departments.toString());
        
        
        // 현재 로그인 한 로그인 정보 저장해서 사용
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String currentUserId = null; // ✅ 현재 로그인 사용자의 ID를 담을 변수
	    String currentUsername = null; // ⭐현재 로그인 사용자의 username

		// 사용자 정보에서 부서 ID를 담을 변수
		String empDeptId = null;
		String empDeptName = null;
		
		String empLevelId = null;

		// 사용자가 로그인되어 있고, UserDetails 객체가 PersonnelLoginDTO 타입인지 확인
		if (authentication != null && authentication.getPrincipal() instanceof PersonnelLoginDTO) {
			PersonnelLoginDTO personnelLoginDTO = (PersonnelLoginDTO) authentication.getPrincipal();
			currentUserId = personnelLoginDTO.getEmpId();
			currentUsername = personnelLoginDTO.getUsername();
			empDeptId = personnelLoginDTO.getEmpDeptId();
			empLevelId = personnelLoginDTO.getEmpLevelId();
			// 부서 ID로 부서명을 조회하는 로직
			// 이전에 CommonCodeService에 추가한 메서드를 활용해야 합니다.
//			log.info("로그인한 사용자의 부서 ID: " + empDeptId);
			if (commonCodeService != null) {
				CommonDetailCode deptCode = commonCodeService.getCommonDetailCode(empDeptId);
				if (deptCode != null) {
					empDeptName = deptCode.getComDtNm(); // ✅ 부서명 변수에 값 할당
				}
			}
			
			
		}
		//이미지 정보 불러옴 : 아직 미구현 일단 잠금
		/**
		PersonnelImgDTO perImg = personnelImgService.getMapperImg(empId);
		
		
		
		
		if(perImg != null) {
			log.info("이미지 매퍼로 가지고온 정보 : " + perImg.toString());
			model.addAttribute("perImg", perImg);
			model.addAttribute("location", uploadBaseLocation);
		}
		**/
		
		// 인사팀 계정이거나  관리자 계정일경우 와 다른 부서 또는 관리자 이하 계정일경우 분리해서 접속 
//		log.info("empLevelId='{}', empDeptId='{}'", empLevelId, empDeptId);
		if("AUT001".equals(empLevelId) || "DEP001".equals(empDeptId) ) {
//			log.info("인사 팀 또는 관리자 계정으로 진입");
			
			
			return "hrn/personnelDetailInfo";
		}else {
//			log.info("인사팀 외 계정 또는 일반 계정");

			
			return "hrn/personnelDetailInfo2";
		}

		
//        return "hrn/personnelDetailInfo";
       
		
    }

    // 인사현황 -> 상세조회 버튼 -> 정보 수정시 수행 
    @PostMapping("/updatePro")
    public String updatePro(PersonnelDTO personnelDTO, RedirectAttributes redirectAttributes,
    		 @RequestParam("empImg") MultipartFile empImg) {
//        log.info("수정할 사원 정보 : " + personnelDTO.toString());
        
        try {
            personnelService.updatePersonnel(personnelDTO, empImg);		//프로필 사진 저장 기능 추가
            redirectAttributes.addFlashAttribute("message", "사원 정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "사원 정보 수정에 실패했습니다.");
        }
        
        return "redirect:/personnel/current";
    }
	
	@GetMapping("/regist")
	public String regist(Model model) {
//		log.info("PersonnelController regist()");
		
		// 부서 리스트
		List<CommonDetailCodeDTO> departments = personnelService.getAllDepartments();
		model.addAttribute("departments", departments);
		
		// 직책 리스트 
		List<CommonDetailCodeDTO> position = personnelService.getAllPositions();
		model.addAttribute("position", position);
		
		
		//추가 된 부분 ----------------------------------------------------
		
		//재직 리스트 
		List<CommonDetailCodeDTO> status = personnelService.getStatus();
		model.addAttribute("status", status);
		
		//보안등급 리스트
		List<CommonDetailCodeDTO> level = personnelService.getAllLevel();
		model.addAttribute("level", level);
		//추가 된 부분 ----------------------------------------------------
		
//		log.info("position" + position.toString());
//		log.info("departments" + departments.toString());
		

		//추가 된 부분 : 사유 로그인 한 사람이 인사팀이거나 관리자 일경우에만 접속 허용
		  
        // 현재 로그인 한 로그인 정보 저장해서 사용
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String currentUserId = null; // ✅ 현재 로그인 사용자의 ID를 담을 변수
	    String currentUsername = null; // ⭐현재 로그인 사용자의 username

		// 사용자 정보에서 부서 ID를 담을 변수
		String empDeptId = null;
		String empDeptName = null;
		
		String empLevelId = null;

		PersonnelLoginDTO personnelLoginDTO = (PersonnelLoginDTO) authentication.getPrincipal();
		// 사용자가 로그인되어 있고, UserDetails 객체가 PersonnelLoginDTO 타입인지 확인
		if (authentication != null && authentication.getPrincipal() instanceof PersonnelLoginDTO) {
			currentUserId = personnelLoginDTO.getEmpId();
			currentUsername = personnelLoginDTO.getUsername();
			empDeptId = personnelLoginDTO.getEmpDeptId();
			empLevelId = personnelLoginDTO.getEmpLevelId();
			// 부서 ID로 부서명을 조회하는 로직
			// 이전에 CommonCodeService에 추가한 메서드를 활용해야 합니다.
//			log.info("로그인한 사용자의 부서 ID: " + empDeptId);
			if (commonCodeService != null) {
				CommonDetailCode deptCode = commonCodeService.getCommonDetailCode(empDeptId);
				if (deptCode != null) {
					empDeptName = deptCode.getComDtNm(); // ✅ 부서명 변수에 값 할당
				}
			}
		}
		
		if("DEP001".equals(personnelLoginDTO.getEmpDeptId()) || "AUT001".equals(personnelLoginDTO.getEmpLevelId())) {
//			log.info("인사팀 혹은 관리자 계정입니다. 접속 허용합니다.");
			
			return "hrn/personnelRegist";
		}else {
			return "redirect:/main";
		}
		
	}
	
	@PostMapping("/registPro")
	public String registPro(PersonnelDTO personnelDTO, @RequestParam("empImg") MultipartFile empImg) throws IOException {
//		log.info("등록할 사원 정보 : " + personnelDTO.toString());
		
		personnelService.personRegist(personnelDTO, empImg);

		return "redirect:/personnel/current";
	}
	
	// 인사발령
	@GetMapping("/trans")
	public String trans(Model model, @AuthenticationPrincipal PersonnelLoginDTO userDetails) {
//		log.info("PersonnelController trans()");
		
		// 로그인한 사용자의 부서 정보 확인
		String userDeptId = userDetails.getEmpDeptId(); 

        // 부서 코드가 'DEP001'(인사팀)일 경우, 버튼 표시 여부를 true로 설정
        boolean isHrTeam = "DEP001".equals(userDeptId);
        model.addAttribute("isHRTeam", isHrTeam);
        
        // 인사발령 테이블 조회
        List<PersonnelTransferDTO> personnelTransfer = personnelTransferService.getTransferPersonnels();
        
		model.addAttribute("personnelTransfer", personnelTransfer);
		
		return "hrn/personnelTrans";
	}
	
	// 인사발령 정보 출력
	@GetMapping("/api/transfers")
	@ResponseBody
	public List<PersonnelTransferDTO> getTransferList() {
//	    log.info("GET /api/transfers 호출");
	    return personnelTransferService.getTransferPersonnels();
	}
	
	// 발령 등록 폼
	@GetMapping("/trans/save")
    public String transSave(Model model) {
//        log.info("PersonnelController transSave()");
        
        // 팝업 페이지에 필요한 부서, 직급 리스트 추가
        List<CommonDetailCodeDTO> departments = personnelService.getAllDepartments();
        model.addAttribute("departments", departments);

        List<CommonDetailCodeDTO> position = personnelService.getAllPositions();
        model.addAttribute("position", position);
        
        // 전체 사원 리스트
        List<PersonnelDTO> allEmployees = personnelService.getAllPersonnels();
        model.addAttribute("allEmployees", allEmployees);
        
        // 인사팀 and 중간관리자 or 상위관리자 사원 목록
        List<PersonnelDTO> approvers = personnelService.getEmployeesByDeptIdLevel("DEP001", "AUT001", "AUT002");
        model.addAttribute("approvers", approvers);

        return "hrn/personnelTransSave";  
    }
	
	// 전자결재로 넘김
	@PostMapping("/trans/submit")
    public ResponseEntity<String> transPersonnel(@RequestBody Map<String, Object> payload, Principal principal) {
//        log.info("인사 발령 요청 수신: {}", payload);
        String loginEmpId = principal.getName(); // 로그인한 사용자 ID
        try {
            // Service 계층으로 발령 데이터 전달
        	personnelService.submitTransPersonnel(payload, loginEmpId);
            return ResponseEntity.ok("인사 발령 신청이 성공적으로 처리되었습니다.");
        } catch (Exception e) {
            log.error("인사 발령 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().body("인사 발령 처리 실패: " + e.getMessage());
        }
    }

	@GetMapping("/orgChart")
    public String showOrgChart(Model model) {
        // 모든 부서 목록을 가져와 모델에 추가
        List<CommonDetailCodeDTO> departments = personnelService.getAllDepartments();
        model.addAttribute("departments", departments);
		
		// 초기 직원 목록을 가져오는 구문
        List<PersonnelDTO> personnels;
        if (!departments.isEmpty()) {
            // 첫 번째 부서의 ID를 가져와 해당 부서의 직원 목록을 조회
            String firstDeptId = departments.get(0).getComDtId();
            personnels = personnelService.getEmployeesByDepartmentId(firstDeptId);
        } else {
            // 부서가 없을 경우 빈 목록을 추가
        	personnels = Collections.emptyList();
        }
        
        // 모델에 직원 목록을 추가하여 초기 화면에 표시
        model.addAttribute("personnels", personnels);

        return "hrn/orgChart"; 
    }
	
    // AJAX 요청을 처리하여 특정 부서의 직원 정보를 JSON 형태로 반환
    @GetMapping("/employees")
    public ResponseEntity<List<PersonnelDTO>> getPersonnels(@RequestParam("deptId") String comDtId) {
        List<PersonnelDTO> personnels = personnelService.getEmployeesByDepartmentId(comDtId);

        return ResponseEntity.ok(personnels);
    }
    
    // 사원테이블에서 부서목록 json형태로 반환
    @GetMapping("/departments") 
    @ResponseBody
    public List<CommonDetailCodeDTO> getDepartments() {
        return personnelService.getAllDepartments();
    }

}