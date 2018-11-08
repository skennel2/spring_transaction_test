package org.almansa.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
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

	@Before
	public void before() {
		deleteAll();
	}

	/*
	 * 트랜젝션이 제대로 동작하지 않는다면 DB의 AutoCommit 여부를 확인할것.
	 */
	@Test(expected = DataAccessException.class)
	public void test_예외가_발생하는_트랜젝션() {
		TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

		try {
			addAccount(1, "123", "123-1234-3212", 0);
			addAccount(1, "123", "242-7434-3436", 0); // duplicated pk
			transactionManager.commit(status);
		} catch (DataAccessException ex) {
			transactionManager.rollback(status);
			throw ex;
		} finally {
			Integer rowCount = getAllAccountCount();
			assertEquals(new Integer(0), rowCount);
		}
	}

	@Test
	public void test_예외가_발생하지_않는_트랜젝션() {
		TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			addAccount(1, "222", "123-1234-3212", 0);
			addAccount(2, "222", "242-7434-3436", 0);
			transactionManager.commit(status);
		} catch (DataAccessException ex) {
			transactionManager.rollback(status);
			throw ex;
		} finally {
			Integer rowCount = getAllAccountCount();
			assertEquals(new Integer(2), rowCount);
		}
	}

	@Test(expected = DataAccessException.class)
	public void 예외가_발생하는_트랜젝션_메소드호출_() {
		TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			addAccount(398, "333", "123-1234-3212", 0);
			addAccount(324, "333", "242-7434-3436", 0);

			test_예외가_발생하는_트랜젝션();

			transactionManager.commit(status);
		} catch (DataAccessException ex) {
			transactionManager.rollback(status);
			throw ex;
		} finally {
			Integer rowCount = getAllAccountCount();
			assertEquals(new Integer(0), rowCount);
		}
	}

	private void deleteAll() {
		TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			jdbcTemplate.update("DELETE FROM ACCOUNT", new HashMap<String, Object>());
		} catch (DataAccessException ex) {
			transactionManager.rollback(status);
		}
		transactionManager.commit(status);
	}

	private void addAccount(int id, String customerName, String accountNumber, int initialAmountOfBalance) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ID", id);
		params.put("CUSTOMER_NAME", customerName);
		params.put("ACCOUNT_NUMBER", accountNumber);
		params.put("BALANCE", initialAmountOfBalance);

		jdbcTemplate.update(
				"INSERT INTO ACCOUNT(ID, CUSTOMER_NAME, ACCOUNT_NUMBER, BALANCE) VALUES(:ID, :CUSTOMER_NAME, :ACCOUNT_NUMBER, :BALANCE)",
				params);
	}

	private Integer getAllAccountCount() {
		return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ACCOUNT", new EmptySqlParameterSource(),
				Integer.class);
	}
}