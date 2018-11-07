package org.almansa.app;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {

	public static void main(String[] args) {
		try(AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfig.class)){
			context.scan("org.almansa.app");
			
			SomeService service = context.getBean(SomeService.class);

		}
	}

}
