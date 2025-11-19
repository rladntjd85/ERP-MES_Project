package com.erp_mes.erp.personnel.dto;

import org.apache.ibatis.type.Alias;

import com.erp_mes.erp.personnel.entity.PersonnelImg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Alias("personnelImg")
public class PersonnelImgDTO {
	
	private String imgId;
	private String empId;
	private String imgName;
	private String imgLocation;
	private String fileName;
	
	
	public static PersonnelImgDTO fromEntity(PersonnelImg img) {
		return PersonnelImgDTO.builder().imgId(img.getImgId()).empId(img.getPersonnel().getEmpId()).imgName(img.getImgName()).imgLocation(img.getImgLocation())
				.build();
	}
}
