package org.almansa.app;

import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfig.class })
public class BalanceTransferTest {

	@Autowired 
	private PlatformTransactionManager transactionManager;
	
	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;
	
	@Test
	public void deleteAllTest() {
		TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
		
		try {
			int updatedRowCount = jdbcTemplate.update("DELETE FROM ACCOUNT", new HashMap<String, Object>());
			transactionManager.commit(status);
		}catch(DataAccessException ex) {
			transactionManager.rollback(status);
		}	
	}	
}
