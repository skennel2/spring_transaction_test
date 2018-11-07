package org.almansa.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
public class BookShopService {
	
	@Autowired
	private NamedParameterJdbcTemplate template;
	
	@Autowired
	private PlatformTransactionManager transactionManager;
		
	public void addBooks() {
	}
}
