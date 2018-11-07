package org.almansa.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SomeService {
	
	@Autowired
	private NamedParameterJdbcTemplate template;
		
	public String test() {
		String query = "SELECT * FROM COMMENT";

		return "test";
	}
}
