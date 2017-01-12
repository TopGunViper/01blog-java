package edu.ouc.chain;

import java.lang.reflect.Proxy;

public class AdvisorChainTest {
	
	public static void main(String args[]){
		
		UserService user = new UserServiceImpl();
		UserService proxy = (UserService)ProxyBuilder.buildProxy(UserService.class,user);
		proxy.insert(null);
	}
	static class ProxyBuilder{
		public static Object buildProxy(Class<?> interfaces, Object target){
			
			return Proxy.newProxyInstance(ProxyBuilder.class.getClassLoader(), 
					new Class<?>[]{interfaces},  new DefaultInvocationHandler(target));
		}
	}
}
