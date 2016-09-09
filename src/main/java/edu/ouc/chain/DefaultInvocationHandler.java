package edu.ouc.chain;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

public class DefaultInvocationHandler implements InvocationHandler {

	private Object target;
	
	private InterceptorChainFactory chainFactory = new InterceptorChainFactory();
	
	public DefaultInvocationHandler(Object target){
		this.target = target;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		MethodInvocation methodInvocation;
		
		List<Object> chain = chainFactory.getInterceptorList();
		
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
