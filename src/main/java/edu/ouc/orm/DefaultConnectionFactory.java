package edu.ouc.orm;

import java.sql.Connection;
import java.sql.DriverManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConnectionFactory implements ConnectionFactory{
	
	private static Logger logger = LoggerFactory.getLogger(DefaultConnectionFactory.class);
	
	private String url = "jdbc:sqlserver://localhost:1433;DatabaseName=fdyuntu";
	private String user = "sa";
	private String password = "880307";
	
	static{
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} catch (ClassNotFoundException e) {
			logger.warn("SQLServerDriver registered failure");
		}
		logger.info("SQLServerDriver register successfully");
	}
	
	@Override
	public Connection newConnection() throws Exception {
		Connection con = DriverManager.getConnection(url, user, password);
		return con;
	}
}
