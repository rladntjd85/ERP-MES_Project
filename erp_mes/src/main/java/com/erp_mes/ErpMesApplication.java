package com.erp_mes;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@ComponentScan(basePackages = {"com.erp_mes.erp", "com.erp_mes.mes"})
@MapperScan({"com.erp_mes.erp.**.mapper", "com.erp_mes.mes.**.mapper"})
@EnableAspectJAutoProxy
public class ErpMesApplication extends SpringBootServletInitializer{

	public static void main(String[] args) {
		SpringApplication.run(ErpMesApplication.class, args);
	}

}