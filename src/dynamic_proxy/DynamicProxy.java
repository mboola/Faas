package dynamic_proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import application.Controller;

public class DynamicProxy implements InvocationHandler {

	//The instance which this proxy intercepts methods from.

	/** TODO change this definition
	 * This DynamicProxy will intercept the invocations of the methods called from target
	 * What we will do is search the method to call in the controller and send it to the controller
	 * But from the view of the Client, it will call the methods direcly from object obtained
	 * from the controller with getFunction or smtg.
	 * @param target Object referenced when method invoke is called. IT IS NOT CONTROLLER
	 * @return The proxy
	 */
	public static Object instantiate(Object target) {
		Class<?> targetClass = target.getClass();
		Class<?> interfaces[] = targetClass.getInterfaces();

		return Proxy.newProxyInstance(targetClass.getClassLoader(),
				interfaces, new DynamicProxy());
	}

	private DynamicProxy() {
	}
	  
	/** TODO: change this definition
	 * This interceptes the method when called. If the method name is registerAction,
	 * we create a new interface with the method passed as a parameter and we define
	 * an implementation (the method passed as a parameter). Then we redefine proxyObject,
	 * If the name is removeAction, we substact the method from the list of interfaces and
	 * redefine proxyObject. Else, we call the method normally.
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Exception
	{
		String	id = method.getName();
		Controller	controller;
		controller = Controller.instantiate();
		Object result = controller.invoke_async(id, args[0]);
		return result;
	}
}
