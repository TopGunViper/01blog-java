package edu.ouc.chain;

public interface MethodInterceptor {
	
	Object invoke(MethodInvocation invocation) throws Exception;
	
}
