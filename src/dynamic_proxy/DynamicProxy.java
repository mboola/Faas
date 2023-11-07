package dynamic_proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.stream.Collectors;

import application.Controller;

public class DynamicProxy implements InvocationHandler {

    private Object target = null;
    
	public static Object newInstance(Object target){
        Class targetClass = target.getClass();
        Class interfaces[] = targetClass.getInterfaces();
        return Proxy.newProxyInstance(targetClass.getClassLoader(),
                interfaces, new DynamicProxy(target));
    }

    private DynamicProxy(Object target)
	{
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Exception
	{
        Object	invocationResult = null;
		Class	targetClass = proxy.getClass();
		String	actionName = method.getName();

        if (Arrays.asList(targetClass.getMethods()).stream().map(m -> m.toString()).collect(Collectors.toList()).contains(actionName))
				invocationResult = method.invoke(this.target, args);
			else
			{
				//if not, we search in the dictionary of actions the name of the method we are trying to invoke
				//if it doesnt exist, we throw an error
				invocationResult = ((Controller)target).invoke_async(actionName, args);
			}
        return invocationResult;
    }

}