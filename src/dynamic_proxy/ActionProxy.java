package dynamic_proxy;

import java.lang.reflect.Method;
import java.util.function.Function;

import application.Controller;
import application.Invokable;
import faas_exceptions.NoActionRegistered;
import faas_exceptions.OperationNotValid;

public class ActionProxy {

	private final Controller controller;

	// Constructor that takes a Controller as a parameter
	public ActionProxy() {
		controller = Controller.instantiate();
	}
    
	private <T, R> Function<T, R> convertMethodToFunction(Object instance, String methodName, Class<T> inputType, Class<R> returnType) throws NoSuchMethodException, SecurityException
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
	public Object getActionProxy(String id) throws Exception
	{
		Invokable	invokable;
		Function<Object, Class<?>>	actionProxyFunction;

		try {
			invokable = controller.getInvokable(id);
		}
		catch (OperationNotValid | NoActionRegistered e) {
			return (null);
		}

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
			catch (Exception e) {
				//TODO: if action couldnt be registered something weird happened.
				e.printStackTrace();
			}
		}
		return (DynamicProxy.instantiate(resultClass));
	}
}
