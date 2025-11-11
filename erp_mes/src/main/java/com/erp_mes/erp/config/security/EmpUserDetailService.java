package com.erp_mes.erp.config.security;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.erp_mes.erp.personnel.dto.PersonnelLoginDTO;
import com.erp_mes.erp.personnel.entity.Personnel;
import com.erp_mes.erp.personnel.repository.PersonnelRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class EmpUserDetailService implements UserDetailsService {
	private final PersonnelRepository personnelRepository;

	@Override
	public UserDetails loadUserByUsername(String empId) throws UsernameNotFoundException {
//		log.info("EmpUserDetailService() : " + empId);
//		int n = personnelRepository.countByEmpIdNative(empId);
//		log.debug(">>>>>>>>>>>>>native count for empId='{}' => {}", empId, n);
		
//		log.debug("username='{}' len={}", empId, empId != null ? empId.length() : -1);
//		for (int i = 0; i < (empId != null ? empId.length() : 0); i++) {
//		    log.debug("char[{}] codepoint={}", i, (int) empId.charAt(i));
//		}
		
		Optional<Personnel> opt = personnelRepository.findByEmpId(empId);
		log.debug("findByEmpId('{}') present? {}", empId, opt.isPresent());
		if (opt.isEmpty()) {
		    throw new UsernameNotFoundException(empId + " : 사용자 조회 실패");
		}
		Personnel p = opt.get();
//		log.debug("loaded empId={}, deptId={}, levelId={}",
//		    p.getEmpId(),
//		    p.getDepartment() != null ? p.getDepartment().getComDtId() : "null",
//		    p.getLevel() != null ? p.getLevel().getComDtId() : "null");

//		PersonnelLoginDTO personnelLoginDTO = ModelMapperUtils.convertObjectByMap(personnel, PersonnelLoginDTO.class);
//		personnelLoginDTO.setEmpId(personnel.getEmpId());
//		personnelLoginDTO.setName(personnel.getName());
//		personnelLoginDTO.setPasswd(personnel.getPasswd());
//		personnelLoginDTO.setEmpDeptId(personnel.getDepartment().getComDtId());
//		personnelLoginDTO.setEmpLevelId(personnel.getLevel().getComDtId());
//		log.info("로그인객체 : " + personnelLoginDTO.toString());
		return  new PersonnelLoginDTO(p);
	}

}