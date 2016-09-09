package edu.ouc.chain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InterceptorChainFactory {
	
	public List<Object> getInterceptorList(){
		List<Object> interceptorList = new ArrayList<>();
		interceptorList.add(new TimeInterceptor());
		Set<String> exclusions = new HashSet<String>();
		exclusions.add("test");
		exclusions.add("insert");
		interceptorList.add(new MethodFilterInterceptor(exclusions));
		return interceptorList;
	}
}
