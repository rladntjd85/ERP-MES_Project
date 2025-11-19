package com.erp_mes.erp.groupware.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.erp_mes.erp.groupware.dto.DocumentDTO;
import com.erp_mes.erp.groupware.entity.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

	@Query(value = """ 
			SELECT
				d.*,
				e.emp_name as empName
			FROM 
				shared_document d 
			LEFT JOIN 
				employee e
			ON
			 	d.emp_id = e.emp_id
			""",  nativeQuery = true)
	Collection<Document> getAllDocumentWithEmployee();

}
