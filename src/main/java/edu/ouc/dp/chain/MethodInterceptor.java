package edu.ouc.dp.chain;

public interface MethodInterceptor {
	
	Object invoke(MethodInvocation invocation) throws Exception;
	
}
