package edu.ouc.dp.chain;

import static org.junit.Assert.*;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ouc.dp.base.BaseTest;
import edu.ouc.dp.chain.MethodFilterInterceptor.MethodNotAllowedException;

public class AdvisorChainTest extends BaseTest{
	private static final Logger LOG = LoggerFactory.getLogger(AdvisorChainTest.class);

	static class ProxyBuilder{
		public static Object buildProxy(Class<?> interfaces, Object target, List<MethodInterceptor> list){
			return Proxy.newProxyInstance(ProxyBuilder.class.getClassLoader(), 
					new Class<?>[]{interfaces},  new DefaultInvocationHandler(target, list));
		}
	}

	@Test
	public void testAOP() {
		
		Set<String> exclusionMethodNames = new HashSet<String>();
		exclusionMethodNames.add("delete");//����delete����
		
		//���˷�����������
		MethodFilterInterceptor filter = new MethodFilterInterceptor(exclusionMethodNames);
		
		//��¼����ִ��ʱ��������
		TimeLogInterceptor time = new TimeLogInterceptor();
		
		List<MethodInterceptor> list = Arrays.asList(filter,time);
		
		UserService user = new UserServiceImpl();
		UserService proxy = (UserService)ProxyBuilder.buildProxy(UserService.class,user,list);

		proxy.update(null);
		assertTrue(time.msg.contains("update execute time:"));
		
		try{
			proxy.delete(null);
			fail("method delete is not allowed!");
		}catch(Exception e){
			assertEquals(MethodNotAllowedException.class, e.getClass());
		}
	}
}
