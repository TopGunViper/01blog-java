package edu.ouc.dp.chain;

import java.lang.reflect.Method;

/**
 * MethodInvocation��������ִ��
 * 
 * @author wqx
 *
 */
public interface MethodInvocation {
	
	/**
	 * ��ȡ��������
	 * 
	 * @return
	 */
	public Method getMethod();
	
	/**
	 * ��ȡ����
	 * 
	 * @return
	 */
	public Object[] getParameters();
	
	/**
	 * ִ����һ������
	 * 
	 * @return
	 * @throws Exception
	 */
	Object executeNext() throws Exception;
	
	
}
