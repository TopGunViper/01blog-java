package edu.ouc.chain;

import java.lang.reflect.Method;

/**
 * MethodInvocation代表方法的执行
 * 
 * @author wqx
 *
 */
public interface MethodInvocation {
	
	/**
	 * 获取方法对象
	 * 
	 * @return
	 */
	public Method getMethod();
	
	/**
	 * 获取参数
	 * 
	 * @return
	 */
	public Object[] getParameters();
	
	/**
	 * 执行下一个方法
	 * 
	 * @return
	 * @throws Exception
	 */
	Object executeNext() throws Exception;
	
	
}
