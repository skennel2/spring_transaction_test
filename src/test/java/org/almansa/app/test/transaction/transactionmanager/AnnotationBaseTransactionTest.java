package org.almansa.app.test.transaction.transactionmanager;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.almansa.app.ApplicationConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfig.class })
public class AnnotationBaseTransactionTest {

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Before
	public void before() {
		deleteAll();
	}

	// @Rollback은 테스트 메소드가 완료된 후 테스트 관리 트랜잭션을 롤백할지 여부를 나타내는 데 사용되는 테스트 주석입니다.
	// 테스트 컨텍스트에서 @Transactional이 붙은 메소드는 자동으로 롤백되는데, 그 디폴트 동작을 막는 옵션이다.
	// @Commit과 @Rollback(false) 이 둘은 같은 의미이다.
	@Rollback(true) 
	@Transactional(propagation = Propagation.REQUIRED)
	@Test(expected = DataAccessException.class)
	public void test_예외가_발생하는_트랜젝션() {
		addAccount(1, "1", "123-1234-3212", 0);
		addAccount(1, "1", "123-1234-3212", 0);
	}
	
	@Rollback(true) 
	@Transactional(propagation = Propagation.REQUIRED)
	@Test
	public void test_예외가_발생하지_않는_트랜젝션() {
		addAccount(111, "aaa", "123-1234-3212", 0);
		addAccount(222, "bbb", "123-1234-3212", 0);
		
		assertEquals(new Integer(2), getAllAccountCount());
	}	

	@Transactional
	public void duplicatedPkInsert() {
		addAccount(2, "2", "123-1234-3212", 0);
		addAccount(2, "3", "242-7434-3436", 0);
	}

	@Transactional
	public void deleteAll() {
		jdbcTemplate.update("DELETE FROM ACCOUNT", new HashMap<String, Object>());
	}

	@Transactional(propagation = Propagation.REQUIRED)
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
