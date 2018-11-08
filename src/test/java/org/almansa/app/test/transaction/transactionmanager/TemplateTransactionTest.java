package org.almansa.app.test.transaction.transactionmanager;

import java.util.HashMap;
import java.util.Map;

import org.almansa.app.ApplicationConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfig.class })
public class TemplateTransactionTest {

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Before
	public void before() {
		deleteAll();
	}

	@Test(expected = DataAccessException.class)
	public void test_예외가_발생하는_트랜젝션() {
		transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				addAccountPropagationRequires(1, "123", "123-1234-3212", 0);
				addAccountPropagationRequires(1, "123", "242-7434-3436", 0);
			}
		});
	}

	@Test
	public void test_비지니스_예외에_대한_롤백처리() {
		transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {
					addAccountPropagationRequires(1, "123", "123-1234-3212", 0);
					addAccountPropagationRequires(2, "123", "242-7434-3436", 0);
					
					// DB에 접근하는 JdbcTemplate는 기본적으로 모든 트랜젝션 관련 예외를 DataAccessException 형태로 던진다.
					// 하지만 그 이외의 비지니스 예외에 대해서도 롤백을 처리해야 할 경우가 있다. 
					throw new RuntimeException("Some Business Exception");
				}catch(RuntimeException ex) {
					// try - catch 문으로 비지니스 로직을 catch후 TransactionStatus의 setRollbackOnly를 호출하면 
					// 해당 트랜젝션을 롤백한다. 
					status.setRollbackOnly();
				}
			}
		});
	}
	
	@Test(expected = DataAccessException.class)
	public void test_RequiresNew트랜젝션_전파() {
		transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				// DefaultTransactionDefinition로 정의하는 트랜젝션 전파 레벨중, REQUIRES_NEW는 
				// 이미 시작된 트랜잭션이 존재한다면 잠시 중지시키고 새로운 트랜젝션을 시작한다. 
				addAccountPropagationRequiresNew(1, "123", "123-1234-3212", 0); // 이미 커밋되어 db에 반영된다.
				addAccountPropagationRequiresNew(1, "123", "242-7434-3436", 0);
			}
		});
	}
	
	private void deleteAll() {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				jdbcTemplate.update("DELETE FROM ACCOUNT", new HashMap<String, Object>());
			}
		});
	}

	
	private void addAccountPropagationRequires(int id, String customerName, String accountNumber, int initialAmountOfBalance) {
		transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("ID", id);
				params.put("CUSTOMER_NAME", customerName);
				params.put("ACCOUNT_NUMBER", accountNumber);
				params.put("BALANCE", initialAmountOfBalance);

				jdbcTemplate.update(
						"INSERT INTO ACCOUNT(ID, CUSTOMER_NAME, ACCOUNT_NUMBER, BALANCE) VALUES(:ID, :CUSTOMER_NAME, :ACCOUNT_NUMBER, :BALANCE)",
						params);
			}
		});
	}
	
	private void addAccountPropagationRequiresNew(int id, String customerName, String accountNumber, int initialAmountOfBalance) {
		transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("ID", id);
				params.put("CUSTOMER_NAME", customerName);
				params.put("ACCOUNT_NUMBER", accountNumber);
				params.put("BALANCE", initialAmountOfBalance);

				jdbcTemplate.update(
						"INSERT INTO ACCOUNT(ID, CUSTOMER_NAME, ACCOUNT_NUMBER, BALANCE) VALUES(:ID, :CUSTOMER_NAME, :ACCOUNT_NUMBER, :BALANCE)",
						params);
			}
		});
	}

	private Integer getAllAccountCount() {
		return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ACCOUNT", new EmptySqlParameterSource(),
				Integer.class);
	}
}
