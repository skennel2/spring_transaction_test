package org.almansa.app;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Component
public class AccountService {
	
	@Autowired
	private NamedParameterJdbcTemplate template;
	
	@Autowired
	private PlatformTransactionManager transactionManager;
	
	@Autowired
	private Logger logger;
		
	public void deleteAllAccount() {
		TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
		
		try {
			int updatedRowCount = template.update("DELETE FROM ACCOUNT", new HashMap<String, Object>());
			
			logger.log(updatedRowCount + "row updated");
			
			transactionManager.commit(status);
		}catch(DataAccessException ex) {
			transactionManager.rollback(status);
		}				
	}	
}
