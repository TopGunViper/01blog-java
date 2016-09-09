package edu.ouc.chain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 记录方法执行时间
 * 
 * @author wqx
 *
 */
public class TimeInterceptor implements MethodInterceptor {

	private static Logger logger = LoggerFactory.getLogger(TimeInterceptor.class);
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Exception {
		long startTime = System.currentTimeMillis();
		Object relVal = invocation.executeNext();
		logger.info(invocation.getMethod().getName() + " execute time:" + (System.currentTimeMillis() - startTime) + "ms.");
		return relVal;
	}
}
