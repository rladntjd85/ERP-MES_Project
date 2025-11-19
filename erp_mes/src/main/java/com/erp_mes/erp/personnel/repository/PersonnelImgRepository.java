package com.erp_mes.erp.personnel.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import com.erp_mes.erp.personnel.entity.PersonnelImg;

public interface PersonnelImgRepository extends JpaRepository<PersonnelImg, String> {



	Optional<PersonnelImg> findByPersonnel_EmpId(@Param("empId")String empId);

}
