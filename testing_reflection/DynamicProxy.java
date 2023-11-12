//THIS IS A CONCEPTUAL VERSION, NOT A FUNCTIONAL ONE

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DynamicProxy implements InvocationHandler {

    //the methods will be intercepted from this class.
    private Object          target = null;
    //this is what will be recreated every time a new action is registered
    private Object          actionProxy;
    private List<Class<?>>  interfaces;
    //private Class<?>    interfaces[];
    private ClassLoader classLoader;
    
    //we get a Proxy that will intercept all the calls from target
	public static Object instantiate(Object target) {

        DynamicProxy    dynamicProxy = new DynamicProxy(target);
        return (dynamicProxy.getActionProxy());
    }
    private Object getActionProxy() {
        return (actionProxy);
    }
    private DynamicProxy(Object target) {
        this.target = target;
        this.classLoader = target.getClass().getClassLoader();
        Class<?> interfacesArr[] = target.getClass().getInterfaces();
        interfaces = Arrays.asList(interfacesArr);
        redoActionProxy();
    }

    //this redoes the actionProxy with all the changes it has
    private void redoActionProxy() 
    {
        Class<?> interfacesArr[] = new Class<?>[interfaces.size()];
        interfaces.toArray(interfacesArr);
        this.actionProxy = Proxy.newProxyInstance(classLoader, interfacesArr, this);
    }

    private void    createAndAddNewInterface(String interfaceName)
    {
        Class<?> dynamicInterface = InterfaceGenerator.generateInterface(interfaceName);
        interfaces.add(dynamicInterface);
    }

    private void    eraseInterface(String interfaceName)
    {
        //idk maybe search by name or something and remove
    }

    //interception of calls
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Exception
	{
        Object	invocationResult = null;
        String  methodName = method.getName();

        //Are we registering an action?
        if (methodName.equals("registerAction"))
        {
            //we create a new interface. It's name will be the id of the action (args[0])
            createAndAddNewInterface((String)args[0]);
            //we must redo the actionProxy with the changes we did
            redoActionProxy();
        }
        else if (methodName.equals("removeAction"))
        {
            //we erase an interface. It's name will be the id of the action (args[0])
            eraseInterface((String)args[0]);
            //we must redo the actionProxy with the changes we did
            redoActionProxy();
        }

        //if this method exists inside controller it means it is a controller operation
        if (Arrays.asList(proxy.getClass().getMethods())
            .stream()
            .map(m -> m.toString())
            .collect(Collectors.toList())
            .contains(methodName))
        {
            invocationResult = method.invoke(this.target, args);
        }
        //then it means it is an invocation of a function
        else
        {
            invocationResult = ((Controller)target).invoke(methodName, args);
            //maybe better do invoke_async
        }
        return invocationResult;
    }

}