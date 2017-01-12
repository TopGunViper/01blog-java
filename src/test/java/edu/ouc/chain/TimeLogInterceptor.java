package edu.ouc.chain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 记录方法执行时间
 * 
 * @author wqx
 *
 */
public class TimeLogInterceptor implements MethodInterceptor {

	private static Logger logger = LoggerFactory.getLogger(TimeLogInterceptor.class);
	
	String msg;
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Exception {
		long startTime = System.currentTimeMillis();
		Object relVal = invocation.executeNext();
		msg = invocation.getMethod().getName() + " execute time:" + (System.currentTimeMillis() - startTime) + "ms.";
		logger.info(msg);
		return relVal;
	}
}
