package edu.ouc.chain;

import java.lang.reflect.Method;
import java.util.List;

/**
 * MethodInvocation的默认实现
 * 
 * @author wqx
 *
 */
public class DefaultMethodInvocation implements MethodInvocation {

	//目标对象
	private Object target;
	//代理
	private Object proxy;
	//目标方法
	private Method method;
	//参数
	private Object[] parameters;
	
	//拦截器链
	private List<?> interceptors;
	//当前执行的Interceptor的索引（范围：0-interceptors.size()-1），初始为-1
	private int currentIndex = -1;
	
	public DefaultMethodInvocation(Object target,Object proxy,Method method,Object[] parameters, List<?> interceptors){
		this.target = target;
		this.proxy = proxy;
		this.method = method;
		this.parameters = parameters;
		this.interceptors = interceptors;
	}
	
	@Override
	public Object executeNext() throws Exception {
		//判断拦截器链是否执行完
		if(this.currentIndex == this.interceptors.size() - 1){
			//如果执行完，直接执行目标方法
			method.setAccessible(true);
			return method.invoke(target, parameters);
		}
		Object interceptor = this.interceptors.get(++this.currentIndex);
		MethodInterceptor methodInterceptor = (MethodInterceptor)interceptor;
		return methodInterceptor.invoke(this);
	}
	
	//getter setter
	public Object getTarget() {
		return target;
	}
	public Object getProxy() {
		return proxy;
	}
	@Override
	public Method getMethod() {
		// TODO Auto-generated method stub
		return method;
	}

	@Override
	public Object[] getParameters() {
		// TODO Auto-generated method stub
		return parameters;
	}
	public List<?> getInterceptors() {
		return interceptors;
	}
	public int getCurrentIndex() {
		return currentIndex;
	}
	public void setTarget(Object target) {
		this.target = target;
	}
	public void setProxy(Object proxy) {
		this.proxy = proxy;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}
	public void setInterceptors(List<?> interceptors) {
		this.interceptors = interceptors;
	}
	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}

}
