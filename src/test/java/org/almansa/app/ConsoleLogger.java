package org.almansa.app;

public class ConsoleLogger implements Logger{

	@Override
	public void log(Object source) {
		if(source == null) {
			return;
		}
		
		System.out.println(source.toString());		
	}

}
