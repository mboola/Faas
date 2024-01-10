package core.dynamicproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;

import core.application.Controller;
import core.application.Invokable;
import core.exceptions.*;

public class DynamicProxy implements InvocationHandler {

	//The instance which this proxy intercepts methods from.
	private boolean isSync;

	/** TODO change this definition
	 * This DynamicProxy will intercept the invocations of the methods called from target
	 * What we will do is search the method to call in the controller and send it to the controller
	 * But from the view of the Client, it will call the methods direcly from object obtained
	 * from the controller with getFunction or smtg.
	 * @param target Object referenced when method invoke is called. IT IS NOT CONTROLLER
	 * @return The proxy
	 */
	public static Object instantiate(Object target, boolean isSync) {
		Class<?> targetClass = target.getClass();
		Class<?> interfaces[] = targetClass.getInterfaces();

		return Proxy.newProxyInstance(targetClass.getClassLoader(),
				interfaces, new DynamicProxy(isSync));
	}

	private DynamicProxy(boolean isSync) {
		this.isSync = isSync;
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
		Object result;
		if (!isSync)
			result = controller.invoke_async(id, args[0]);
		else
			result = controller.invoke(id, args[0]);
		return result;
	}

	private static <T, R> Function<T, R> convertMethodToFunction(Object instance, String methodName, Class<?> inputType, Class<R> returnType) throws NoSuchMethodException, SecurityException
	{
		Method method = instance.getClass().getMethod(methodName, inputType);

		return input -> {
			try {
				// Invoke the method on the instance with the provided input
				Object result = method.invoke(instance, input);

				// Cast the result to the desired return type
				return returnType.cast(result);
			}
			catch (Exception e) {
				throw new RuntimeException("Error invoking method", e);
			}
		};
	}

	/**
	 * Retrieves a proxy object for an action with the specified ID.
	 *
	 * @param id Identifier of the action.
	 * @return Proxy object for the action.
	 * @throws Exception The exception can be caused because:
	 * <ul>
	 * 	<li>The id passed as a parameter is null.</li>
	 * 	<li>There is no action found with the id passed as a parameter.</li>
	 * </ul>
	 * //TODO: Specify more details about the potential exceptions.
	 */
	public static Object getActionProxy(String id, boolean isSync) throws OperationNotValid, NoActionRegistered
	{
		Controller	controller;
		Invokable	invokable;
		Function<Object, Class<?>>	actionProxyFunction;

		controller = Controller.instantiate();
		invokable = controller.getInvokable(id);

		//get the function that will return an instantiated class
		actionProxyFunction = (Function<Object, Class<?>>) invokable.getInvokable();

		//here I get the class
		Object resultClass = actionProxyFunction.apply(null);

		//the metadata of the class
		Class<?> actionProxyClass = resultClass.getClass();

		//and all his methods
		Method[] methods = actionProxyClass.getDeclaredMethods();

		for (Method method : methods) {
			//if there is not an action with this name
			Class<?>[] parameterTypes = method.getParameterTypes();
			Class<?> returnType = method.getReturnType();
			try {
				controller.registerAction(method.getName(), convertMethodToFunction(resultClass, method.getName(), parameterTypes[0], returnType), invokable.getRam());
			}
			catch (OperationNotValid e) {
			}
			catch (Exception e) {
				//TODO: if action couldnt be registered something weird happened.
				e.printStackTrace();
			}
		}
		return (DynamicProxy.instantiate(resultClass, isSync));
	}
}
