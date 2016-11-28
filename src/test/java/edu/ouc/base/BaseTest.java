package edu.ouc.base;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

public class BaseTest extends TestCase{
	
	private static String BASE_DIR = "src/test/java/edu/ouc/base/log4j.properties";
	
	static {
		 PropertyConfigurator.configure(BASE_DIR);
	}
}
