package org.almansa.app.test.transaction.transactionmanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

public class TransactionManagerTest {
	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;
	
	public void test() {
		
	}
	
}
