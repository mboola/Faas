//THIS IS A CONCEPTUAL VERSION, NOT A FUNCTIONAL ONE

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class DynamicProxy implements InvocationHandler {

    //the methods will be intercepted from this class.
    private Object target = null;
    
    //we get a Proxy that will intercept all the calls from target
	public static Object instantiate(Object target){
        Class<?> targetClass = target.getClass();
        Class<?> interfaces[] = targetClass.getInterfaces();
        return Proxy.newProxyInstance(targetClass.getClassLoader(),
                interfaces, new DynamicProxy(target));
    }

    private DynamicProxy(Object target)
	{
        this.target = target;
    }

    //interception of calls
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Exception
	{
        Object	    invocationResult = null;
		Class<?>	targetClass = proxy.getClass();
		String	    actionName = method.getName();

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