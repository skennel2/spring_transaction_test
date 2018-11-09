package org.almansa.app;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableTransactionManagement // @Transactional을 사용할수 있게 설정해주는 어노테이션
public class ApplicationConfig {

	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setUrl("jdbc:h2:~/bank;autocommit=on");
		
		dataSource.setUsername("sa");

		return dataSource;
	}
	
	/*
	 * PlatformTransactionManager는 여러 트랜젝션 API를 기술독립적으로 캡슐화하는 메인 트랜젝션 인터페이스이다. 
	 */
	@Bean
	public PlatformTransactionManager transactionManager() {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
		transactionManager.setDataSource(dataSource());
		
		return transactionManager;
	}
	
	@Bean
	public NamedParameterJdbcTemplate  jdbcTemplate() {
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource());
		return jdbcTemplate;
	}
	
	/*
	 * TransactionTemplate은 Jdbc와 유사한 원리로 
	 * PlatformTransactionManager을 사용한 트랜젝션이 적용될 템플릿 코드를 캡슐화한다. 
	 * 
	 * 스레드 세이프한 객체여서 빈으로 등록해서 사용해도 무관하다.
	 */
	@Bean
	public TransactionTemplate transactionTemplate() {
		TransactionTemplate transactionTemplate = new TransactionTemplate();
		transactionTemplate.setTransactionManager(transactionManager());
		
		return transactionTemplate;
	}
}
