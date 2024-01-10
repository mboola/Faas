package core.dynamicproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;

import core.application.Controller;
import core.application.Invokable;
import core.exceptions.OperationNotValid;
import core.exceptions.NoActionRegistered;

/**
 * A dynamic proxy class that implements the {@link InvocationHandler} interface.
 * It allows the creation of proxy instances for objects and handles method invocations on those objects.
 *
 * @see InvocationHandler
 */
public class DynamicProxy implements InvocationHandler {

	/** Flag indicating whether the proxy should operate in a synchronous manner. */
	private boolean isSync;

	/**
	 * Instantiates a DynamicProxy with the specified synchronization flag.
	 *
	 * @param isSync a boolean flag indicating whether the proxy should operate in a synchronous manner
	 */
	private DynamicProxy(boolean isSync) {
		this.isSync = isSync;
	}

	/**
	 * Creates a proxy instance for the specified target object with the given synchronization flag.
	 *
	 * @param target the target object for which a proxy is to be created
	 * @param isSync a boolean flag indicating whether the proxy should operate in a synchronous manner
	 * @return a proxy instance for the target object
	 */
	public static Object instantiate(Object target, boolean isSync) {
		Class<?> targetClass = target.getClass();
		Class<?> interfaces[] = targetClass.getInterfaces();

		return Proxy.newProxyInstance(targetClass.getClassLoader(),
				interfaces, new DynamicProxy(isSync));
	}

	/**
	 * Handles method invocations on the proxy object.
	 *
	 * @param proxy  the proxy object
	 * @param method the method being invoked
	 * @param args   the arguments passed to the method
	 * @return the result of the method invocation
	 * @throws Exception if an error occurs during method invocation
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Exception
	{
		String	id = method.getName();
		Controller	controller = Controller.instantiate();

		if (!isSync)
			return controller.invoke_async(id, args[0]);
		else
			return controller.invoke(id, args[0]);
	}

	/**
	 * Converts a method of an object into a {@link Function} that can be used as an invokable action.
	 *
	 * @param instance    the object instance
	 * @param methodName  the name of the method to convert
	 * @param inputType   the input type of the method
	 * @param returnType  the return type of the method
	 * @param <T>         the type of the input
	 * @param <R>         the type of the output
	 * @return a {@link Function} representing the converted method
	 * @throws NoSuchMethodException if the specified method is not found
	 * @throws SecurityException    if there is a security violation
	 */
	private static <T, R> Function<T, R> convertMethodToFunction(Object instance, String methodName, Class<?> inputType, Class<R> returnType) throws NoSuchMethodException, SecurityException
	{
		Method method = instance.getClass().getMethod(methodName, inputType);

		return input -> {
			try {
				Object result = method.invoke(instance, input);

				return returnType.cast(result);
			}
			catch (Exception e) {
				throw new RuntimeException("Error invoking method", e);
			}
		};
	}

	/**
	 * Gets the proxy for a specified action based on its ID.
	 *
	 * @param id     the ID of the action
	 * @param isSync a boolean flag indicating whether the action should operate in a synchronous manner
	 * @return a proxy for the specified ID action
	 * @throws OperationNotValid    if the ID is null
	 * @throws NoActionRegistered   if no action is registered with the specified ID
	 */
	@SuppressWarnings({"unchecked","rawtypes"})
	public static Object getActionProxy(String id, boolean isSync) throws OperationNotValid, NoActionRegistered
	{
		Controller controller = Controller.instantiate();
		//If id is not valid or doesn't exist this will throw OperationNotValid or NoActionRegistered
		Invokable invokable = controller.retrieveInvokable(id);
		//Get the function that will return an instantiated class in resultClass
		Function<Object, Class<?>>	actionProxyFunction = (Function<Object, Class<?>>) invokable.getInvokable();
		Object resultClass = actionProxyFunction.apply(null);
		//I get the metadata of the class the proxy will be created to intercept invocation calls
		Class<?> actionProxyClass = resultClass.getClass();
		Method[] methods = actionProxyClass.getDeclaredMethods();

		Class<?>[] parameterTypes;
		Class<?> returnType;
		for (Method method : methods) {
			parameterTypes = method.getParameterTypes();
			returnType = method.getReturnType();
			try {
				controller.registerAction(method.getName(), convertMethodToFunction(resultClass, method.getName(), parameterTypes[0], returnType), invokable.getRam());
			}
			catch (OperationNotValid e) {
				//This means a function with the method name as an ID has already been registered. In this case we don't care
				//but there could be flag to override the function (deleting the function with that ID and registering the new one).
			}
			catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}

		return (DynamicProxy.instantiate(resultClass, isSync));
	}
}
