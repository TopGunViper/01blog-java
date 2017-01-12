package edu.ouc.chain;

import java.util.HashSet;
import java.util.Set;

public class MethodFilterInterceptor implements MethodInterceptor {
	
	private Set<String> exclusions = new HashSet<>();
	
	public MethodFilterInterceptor(Set<String> exclusions){
		this.exclusions = exclusions;
	}
	@Override
	public Object invoke(MethodInvocation invocation) throws Exception {
		String methodName = invocation.getMethod().getName();
		if(exclusions.contains(methodName)){
			throw new MethodNotAllowedException("method " + methodName + " is not allowed!");
		}
		return invocation.executeNext();
	}
	static class MethodNotAllowedException extends RuntimeException{
		private static final long serialVersionUID = 3908651373650374107L;

		public MethodNotAllowedException(String msg){
			super(msg);
		}
	}
}
