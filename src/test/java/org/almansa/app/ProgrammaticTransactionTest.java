package org.almansa.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfig.class })
public class ProgrammaticTransactionTest {

	@Autowired 
	private PlatformTransactionManager transactionManager;
	
	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;
	
	
	/*
	 * 트랜젝션이 제대로 동작하지 않는다면 DB의 AutoCommit 여부를 확인할것.
	 *   
	 */
	@Test
	public void transactionTest() {
		TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
		
		try {
			deleteAll();
		
			addAccount(1, "123", "123-1234-3212", 0);
			addAccount(1, "123", "242-7434-3436", 0); // duplicated pk 
			
			transactionManager.commit(status);
		}catch(DataAccessException ex) {
			ex.printStackTrace();
			transactionManager.rollback(status);
		}finally {
			
			Integer rowCount = getAllAccountCount();
			assertEquals(new Integer(0), rowCount);
		}
	}
	
	private void deleteAll() {
		jdbcTemplate.update("DELETE FROM ACCOUNT", new HashMap<String, Object>());		
	}
	
	private void addAccount(int id, String customerName, String accountNumber, int initialAmountOfBalance) {	
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ID", id);
		params.put("CUSTOMER_NAME", customerName);
		params.put("ACCOUNT_NUMBER", accountNumber);
		params.put("BALANCE", initialAmountOfBalance);
		
		jdbcTemplate.update("INSERT INTO ACCOUNT(ID, CUSTOMER_NAME, ACCOUNT_NUMBER, BALANCE) VALUES(:ID, :CUSTOMER_NAME, :ACCOUNT_NUMBER, :BALANCE)", params);
	}
	
	private Integer getAllAccountCount() {				
		return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ACCOUNT", new EmptySqlParameterSource(), Integer.class);
	}
}