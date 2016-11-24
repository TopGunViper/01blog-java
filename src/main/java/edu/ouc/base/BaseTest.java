package edu.ouc.base;

import org.apache.log4j.PropertyConfigurator;

public class BaseTest {
	
	private static String BASE_DIR = "src/main/java/edu/ouc/base/log4j.properties";
	
	static {
		 PropertyConfigurator.configure(BASE_DIR);
	}
}
