package com.erp_mes.erp.attendance.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import com.erp_mes.erp.attendance.dto.AdminCommuteDTO;
import com.erp_mes.erp.attendance.dto.CommuteDTO;
import com.erp_mes.erp.attendance.dto.CommuteDeleteLogDTO;
import com.erp_mes.erp.attendance.dto.CommuteScheduleDTO;
import com.erp_mes.erp.commonCode.dto.CommonDetailCodeDTO;
import com.erp_mes.erp.personnel.dto.PersonnelDTO;

@Mapper
public interface CommuteMapper {

	// 출퇴근리스트(오늘날짜)
	List<CommuteDTO> getDeptCommuteList(Map<String, Object> paramMap);
	
	// 오늘 출근 기록이 있는지 확인
	int getTodayCheckInCount(String empId);
	
	// 출근버튼 눌렀을때 출근시간 commute_record 테이블에 저장
	int insertCommuteCheckIn(CommuteDTO commute);

	// 오늘 퇴근 기록이 있는지 확인
	int getTodayCheckOutCount(String empId);

	// 퇴근버튼 눌렀을때 퇴근시간 commute_record에 저장
	void updateCommuteCheckOut(CommuteDTO commute);

	// 부서 공통코드 조회
	List<CommonDetailCodeDTO> getCommonDept(String comId);

	// 전체부서 인원(오늘날짜) - 공통코드 셀렉박스
	List<AdminCommuteDTO> getAllDeptCommuteList(Map<String, Object> paramMap);
	
	// 특정부서 인원(오늘날짜) - 공통코드 셀렉박스
	List<AdminCommuteDTO> getSpecificDeptCommuteList(Map<String, Object> paramMap);

	// 근무상태 공통코드 조회
	List<CommonDetailCodeDTO> getCommonStatus(String comId);

	// 관리자 수정버튼
	int updateWorkStatus(AdminCommuteDTO dto);

	// 삭제하기 위한 출근기록 조회
	CommuteDTO checkTodayWork(Map<String, Object> deleteLogData);

	// 출근기록 삭제
	int deleteWorkData(Map<String, Object> deleteLogData);

	// 삭제한 출근기록 데이터 로그저장
	int insertLogData(Map<String, Object> deleteLogData);

	// 삭제된 출근 로그 데이터 가져오기
	List<CommuteDeleteLogDTO> getLogData();




}
