package edu.ouc.chain;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

public class DefaultInvocationHandler implements InvocationHandler {

	private Object target;
	
	private List<MethodInterceptor> interceptorsChain;
	
	public DefaultInvocationHandler(Object target, List<MethodInterceptor> interceptorsChain){
		this.target = target;
		this.interceptorsChain = interceptorsChain;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		MethodInvocation methodInvocation;
		
		List<MethodInterceptor> chain = interceptorsChain;
		
		Object relVal;
		if(chain != null && !chain.isEmpty()){
			methodInvocation = new DefaultMethodInvocation(target,proxy,method,args,chain);
			relVal = methodInvocation.executeNext();
		}else{//直接调用目标方法
			relVal = method.invoke(target, args);
		}
		return relVal;
	}
}
