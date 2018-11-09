package org.almansa.app.test.transaction.transactionmanager;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnnotationBaseTransactionService {

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void addAccountsOccursPKException() {
		addAccount(1, "1", "123-1234-3212", 0);
		addAccount(1, "1", "123-1234-3212", 0);
	}	
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void addAccounts() {
		addAccount(1, "1", "123-1234-3212", 0);
		addAccount(2, "1", "123-1234-3212", 0);
	}	
		
	@Transactional
	public void deleteAll() {
		jdbcTemplate.update("DELETE FROM ACCOUNT", new HashMap<String, Object>());
	}

	@Transactional
	public void addAccount(int id, String customerName, String accountNumber, int initialAmountOfBalance) {
		MapSqlParameterSource parameterSource = new MapSqlParameterSource();
		parameterSource.addValue("ID", id);
		parameterSource.addValue("CUSTOMER_NAME", customerName);
		parameterSource.addValue("ACCOUNT_NUMBER", accountNumber);
		parameterSource.addValue("BALANCE", initialAmountOfBalance);
		
		jdbcTemplate.update(
				"INSERT INTO ACCOUNT(ID, CUSTOMER_NAME, ACCOUNT_NUMBER, BALANCE) VALUES(:ID, :CUSTOMER_NAME, :ACCOUNT_NUMBER, :BALANCE)",
				parameterSource);
	}

	public Integer getAllAccountCount() {
		return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ACCOUNT", new EmptySqlParameterSource(),
				Integer.class);
	}	
}
