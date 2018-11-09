package org.almansa.app.test.transaction.transactionmanager;

import org.almansa.app.ApplicationConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.DataAccessException;

public class Main {

	public static void main(String[] args) {
		try(AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfig.class))
		{
			context.scan("org.almansa.app");
		
			AnnotationBaseTransactionService service = context.getBean(AnnotationBaseTransactionService.class);
			
			service.deleteAll();
			try {
				service.addAccountsOccursPKException();
			}catch(DataAccessException ex) {
				
			}
			
			service.deleteAll();
			service.addAccounts();
		} 

	}

}
