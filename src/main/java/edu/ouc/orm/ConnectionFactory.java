package edu.ouc.orm;

import java.sql.Connection;

/**
 * Connection工厂接口
 * 
 * @author wqx
 *
 */
public interface ConnectionFactory {
	
	/**
	 * 新建Connection
	 * @return
	 */
	public Connection newConnection() throws Exception;
	
}
