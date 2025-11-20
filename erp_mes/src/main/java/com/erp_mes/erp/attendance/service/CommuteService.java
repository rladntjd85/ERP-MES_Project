package com.erp_mes.erp.attendance.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.erp_mes.erp.attendance.dto.AdminCommuteDTO;
import com.erp_mes.erp.attendance.dto.CommuteDTO;
import com.erp_mes.erp.attendance.dto.CommuteDeleteLogDTO;
import com.erp_mes.erp.attendance.dto.CommuteScheduleDTO;
import com.erp_mes.erp.attendance.mapper.CommuteMapper;
import com.erp_mes.erp.attendance.mapper.CommuteScheduleMapper;
import com.erp_mes.erp.commonCode.dto.CommonDetailCodeDTO;
import com.erp_mes.erp.personnel.dto.PersonnelDTO;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class CommuteService {

	private final CommuteMapper commuteMapper;
	private final CommuteScheduleMapper commuteScheduleMapper;

	public CommuteService(CommuteMapper commuteMapper, CommuteScheduleMapper commuteScheduleMapper) {
		this.commuteMapper = commuteMapper;
		this.commuteScheduleMapper = commuteScheduleMapper;
	}

	// 출근현황 리스트
//	public List<CommuteDTO> getDeptCommuteList(String empId, LocalDate queryDate) {
	public List<CommuteDTO> getDeptCommuteList(Map<String, Object> paramMap) {
		log.info("paramMap : " + paramMap);
		return commuteMapper.getDeptCommuteList(paramMap);
	}


	// 출근버튼
	@Transactional
	public CommuteDTO checkIn(String empId) {
		
		// 오늘 출근 기록이 있는지 확인
	    int count = commuteMapper.getTodayCheckInCount(empId);
//	    log.info("count : " + count);
	    if (count > 0) {
	        throw new IllegalStateException("이미 오늘 출근 기록이 존재합니다.");
	    }
		
		// 근무 기준시간 조회
		CommuteScheduleDTO schedule = commuteScheduleMapper.getCurrentSchedule();
		
		if (schedule == null || schedule.getWorkStartTime() == null) {
		    throw new IllegalStateException("근무 스케줄 정보가 존재하지 않습니다.");
		}
		
		// 지각 여부 판별
		LocalTime startTime = schedule.getWorkStartTime().toLocalTime(); // db에서 가져온 출근시작시간
		// db에는 날짜로만 insert 해도 결국 년/월/일이 다 입력 되기에 toLocalTime()을 써서 시간끼리만 비교해야함
		LocalTime nowTime = LocalDateTime.now().toLocalTime(); // 현재시간
		LocalDateTime now = LocalDateTime.now();
		
	    String workStatus;
	    if (nowTime.isAfter(startTime)) {
	        workStatus = "WSTA003"; // 지각
	    } else {
	        workStatus = "WSTA001"; // 출근
	    }
		
		CommuteDTO commute = new CommuteDTO();
		commute.setEmpId(empId);
		commute.setCheckInTime(now);
		commute.setWorkStatus(workStatus);
//		log.info("commute : " + commute);

		commuteMapper.insertCommuteCheckIn(commute);
		
		return commute;
	}

	// 퇴근버튼
	public CommuteDTO checkOut(String empId) {
		
		// 오늘 출근 기록이 있는지 확인
	    int cnt = commuteMapper.getTodayCheckInCount(empId);
//	    log.info("count : " + count);
	    if (cnt <= 0) {
	    	throw new IllegalStateException("NO_CHECKIN");
	    }
		
		// 오늘 퇴근 기록이 있는지 확인
		int count = commuteMapper.getTodayCheckOutCount(empId);
//		log.info("count : " + count);
		if (count > 0) {
			throw new IllegalStateException("ALREADY_CHECKOUT");
		}
		
	    LocalDateTime now = LocalDateTime.now();
		
		CommuteDTO commute = new CommuteDTO();
		commute.setEmpId(empId);
		commute.setCheckOutTime(now);
		commute.setWorkStatus("WSTA002");
		
		commuteMapper.updateCommuteCheckOut(commute);
		
		return commute;
	}
	

	// 부서 공통코드
	public List<CommonDetailCodeDTO> getCommonDept() {
		List<CommonDetailCodeDTO> commonDept = commuteMapper.getCommonDept("DEP");
//		log.info("commonDept : " + commonDept);
		return commonDept;
	}

	// 전체 부서 조회
	public List<AdminCommuteDTO> getAllDeptCommuteList(Map<String, Object> paramMap) {
		return commuteMapper.getAllDeptCommuteList(paramMap);
	}
	// 특정 부서 조회
	public List<AdminCommuteDTO> getSpecificDeptCommuteList(Map<String, Object> paramMap) {
		return commuteMapper.getSpecificDeptCommuteList(paramMap);
	}

	// 근무상태 공통코드
	public List<CommonDetailCodeDTO> getCommonStatus() {
		List<CommonDetailCodeDTO> commonStatus = commuteMapper.getCommonStatus("WSTA");
//		log.info("commonStatus : " + commonStatus);
		return commonStatus;
	}

	// 관리자 수정버튼
	public int updateWorkStatus(List<AdminCommuteDTO> updateList) {
		int updatedCount = 0;
		
        for (AdminCommuteDTO dto : updateList) {
            updatedCount += commuteMapper.updateWorkStatus(dto); // 개별 UPDATE 호출
        }
		
	    return updatedCount;
	}

	// 출근기록 삭제
	public String deleteCommuteRecord(Map<String, Object> deleteLogData) {
		
		// 삭제할 출근기록 조회
		CommuteDTO checkWork =  commuteMapper.checkTodayWork(deleteLogData);
		log.info("checkWork : " + checkWork);
		if (checkWork == null) {
		    throw new IllegalArgumentException("삭제할 출근 기록이 존재하지 않습니다.");
		}
		
		// 출근기록 삭제
		int deleteWork = commuteMapper.deleteWorkData(deleteLogData);
		log.info("deleteWork : " + deleteWork);
		
		// 출근기록 삭제한 데이터 로그저장
		int insertLogData = commuteMapper.insertLogData(deleteLogData);
		log.info("insertLogData : " + insertLogData);
		
		return "삭제 완료";
	}

	// 삭제된 출근 로그데이터 가져오기
	public List<CommuteDeleteLogDTO> getLogData() {
		return commuteMapper.getLogData();
	}
	

}
