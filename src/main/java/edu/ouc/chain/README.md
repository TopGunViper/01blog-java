# SpringAOP实现原理（Proxy + chain of responsibility）
#### proxy对象的生成是通过JDK动态代理机制实现。
#### 责任链模式
测试代码：
```
public class AdvisorChainTest {
	
	public static void main(String args[]){
		//需要代理的目标对象
		UserService user = new UserServiceImpl();
		//构造代理对象
		UserService proxy = (UserService)ProxyBuilder.buildProxy(UserService.class,user);
		
		//通过代理对象调用目标方法
		proxy.insert(null);
	}
	//ProxyBuilder封装了JDK动态代理生成代理对象的过程
	static class ProxyBuilder{
		public static Object buildProxy(Class<?> interfaces, Object target){
			
			return Proxy.newProxyInstance(ProxyBuilder.class.getClassLoader(), 
					new Class<?>[]{interfaces},  new DefaultInvocationHandler(target));
		}
	}
}
```
输出：
> insert user into db

> INFO  edu.ouc.chain.TimeInterceptor - insert execute time:0ms.

将拦截器链的工厂类做下修改，新增insert方法拦截。
```
public class InterceptorChainFactory {

	public List<Object> getInterceptorList(){
		List<Object> interceptorList = new ArrayList<>();
		interceptorList.add(new TimeInterceptor());
		Set<String> exclusions = new HashSet<String>();
		exclusions.add("test");
		exclusions.add("insert");//新需要增拦截方法名
		interceptorList.add(new MethodFilterInterceptor(exclusions));
		return interceptorList;
	}
}
```
输出：
> Exception in thread "main" edu.ouc.chain.MethodFilterInterceptor$MethodNotAllowedException: method insert is not allowed!
