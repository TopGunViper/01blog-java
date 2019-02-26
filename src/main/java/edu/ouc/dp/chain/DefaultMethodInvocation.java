package edu.ouc.dp.chain;

import java.lang.reflect.Method;
import java.util.List;

/**
 * MethodInvocation��Ĭ��ʵ��
 * 
 * @author wqx
 *
 */
public class DefaultMethodInvocation implements MethodInvocation {

	//Ŀ�����
	private Object target;
	//����
	private Object proxy;
	//Ŀ�귽��
	private Method method;
	//����
	private Object[] parameters;
	
	//��������
	private List<?> interceptors;
	//��ǰִ�е�Interceptor����������Χ��0-interceptors.size()-1������ʼΪ-1
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
		//�ж����������Ƿ�ִ����
		if(this.currentIndex == this.interceptors.size() - 1){
			//���ִ���ֱ꣬��ִ��Ŀ�귽��
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
