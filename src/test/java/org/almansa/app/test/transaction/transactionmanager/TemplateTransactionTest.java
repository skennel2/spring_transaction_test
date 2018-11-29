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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * TransactionTemplate에 콜백을 전달하는 형태로 트랜젝션을 처리하는 예제
 * 
 * @author skennel
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfig.class })
public class TemplateTransactionTest {

	@Autowired
	private TransactionTemplate transactionTemplate; // DefaultTransactionDefinition을 상속받은 형태이다.

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Before
	public void before() {
		deleteAll();
	}

	/*
	 * update, insert처럼 리턴값이 필요없는 형태의 로직은 TransactionCallbackWithoutResult의 구현체를 작성해 콜백을
	 * 전달한다.
	 */
	@Test
	public void test_TransactionCallbackWithoutResult() {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				addAccountPropagationRequires(1, "123", "123-1234-3212", 0);
				addAccountPropagationRequires(2, "123", "242-7434-3436", 0);
			}
		});
		
		assertEquals(new Integer(2), getAllAccountCount());
	}
	
	/*
	 * select 처럼 리턴값이 필요한것은 TransactionCallback<?>을 이용한다.
	 * 전달한다.
	 */
	@Test
	public void test_TransactionCallback() {
		
		String value = transactionTemplate.execute(new TransactionCallback<String>() {
			@Override
			public String doInTransaction(TransactionStatus status) {	
				return "Hello";
			}
		} );
		
		assertEquals("Hello", value);
	}	

	@Test
	public void test_처리하지_않는_비지니스_예외() {
		transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				addAccountPropagationRequires(1, "123", "123-1234-3212", 0); 
				addAccountPropagationRequires(2, "123", "242-7434-3436", 0); 

				try {
					throw new Exception("Some Business Exception");
				} catch (Exception e) {
				}
			}
		});
		
		// 처리되지 않은 예외까지 롤백이 적용되지 않는다. 
		assertEquals(new Integer(2), getAllAccountCount()); 
	}
	
	/*
	 * 비지니스 예외에 대한 롤백처리 DB에 접근하는 JdbcTemplate는 기본적으로 모든 트랜젝션 관련 예외를
	 * DataAccessException 형태로 던진다. 하지만 그 이외의 비지니스 예외에 대해서도 롤백을 처리해야 할 경우가 있다. 그런
	 * 경우에 대한 처리 예시이다.
	 */
	@Test
	public void test_비지니스_예외에_대한_롤백처리() {
		transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {
					addAccountPropagationRequires(1, "123", "123-1234-3212", 0); // rollback
					addAccountPropagationRequires(2, "123", "242-7434-3436", 0); // rollback 

					throw new Exception("Some Business Exception"); // 비지니스 예외 발생
				} catch (Exception ex) {
					// try - catch 문으로 비지니스 로직을 catch후 TransactionStatus의 setRollbackOnly를 호출하면
					// 해당 트랜젝션을 롤백한다.
					status.setRollbackOnly();
				}
			}
		});
	}
	
	@Test(expected=RuntimeException.class)
	public void test_처리되지_않은_비지니스_예외() {
		try {
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					addAccountPropagationRequires(1, "123", "123-1234-3212", 0); //rollback
					addAccountPropagationRequires(2, "123", "242-7434-3436", 0); //rollback
					
					throw new RuntimeException("Some Business Exception"); // 언체크 비지니스 예외 발생
				}
			});
		}catch(RuntimeException ex) {
			throw ex;
		}finally {
			// 언체크예외의 경우 catch문으로 예외를 잡아서 status.setRollbackOnly()를 호출해주는 
			// 코딩은 필요없다.  
			assertEquals(new Integer(0), getAllAccountCount());
		}
	}
	
	/*
	 * DefaultTransactionDefinition로 정의하는 트랜젝션 전파 레벨중, REQUIRES_NEW는 이미 시작된 트랜잭션이
	 * 존재한다면 잠시 중지시키고 새로운 트랜젝션을 시작한다.
	 */
	@Test(expected = DataAccessException.class)
	public void test_RequiresNew트랜젝션_전파() {
		transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				addAccountPropagationRequiresNew(1, "123", "123-1234-3212", 0); // commit
				addAccountPropagationRequiresNew(1, "123", "242-7434-3436", 0); // pk 에러 rollback
			}
		});
	}
	
	/*
	 * DefaultTransactionDefinition로 정의하는 트랜젝션 전파 레벨중, REQUIRES는 이미 시작된 트랜잭션이
	 * 존재한다면 그 트랜젝션에 참여한다.
	 */	
	@Test(expected = DataAccessException.class)
	public void test_예외가_발생하는_트랜젝션() {
		transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				addAccountPropagationRequires(1, "123", "123-1234-3212", 0); // rollback
				addAccountPropagationRequires(1, "123", "242-7434-3436", 0); // rollback
			}
		});
	}

	private void addAccountPropagationRequires(int id, String customerName, String accountNumber,
			int initialAmountOfBalance) {
		transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
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
	
	private void addAccountPropagationRequiresNew(int id, String customerName, String accountNumber,
			int initialAmountOfBalance) {
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

	private void deleteAll() {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				jdbcTemplate.update("DELETE FROM ACCOUNT", new HashMap<String, Object>());
			}
		});
	}
	
	private Integer getAllAccountCount() {
		return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ACCOUNT", new EmptySqlParameterSource(),
				Integer.class);
	}
}
