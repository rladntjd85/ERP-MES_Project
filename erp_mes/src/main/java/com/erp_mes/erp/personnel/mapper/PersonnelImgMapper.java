package com.erp_mes.erp.personnel.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.erp_mes.erp.personnel.dto.PersonnelImgDTO;

@Mapper
public interface PersonnelImgMapper {

	PersonnelImgDTO findByempId(String empId);

}
