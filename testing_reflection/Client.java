//THIS IS A CONCEPTUAL VERSION, NOT A FUNCTIONAL ONE

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

public class Client {
    public static void main(String[] args) throws Exception {

        // this is clear
        Controller controller = Controller.instantiate();

        // this is the application client will call
		ActionProxy actionProxy = (ActionProxy) DynamicProxy.instantiate(controller);

        // here actionProxy only has the interface ActionProxy
        actionProxy.showInterfaces();
        // here actionProxy only has the public methods in Controller
        actionProxy.showMethods();

        // we register two actions. each time an action is added, actionProxy should be recompiled
        Function<Map<String, Integer>, Integer> suma = x -> x.get("x") + x.get("y");
        actionProxy.registerAction("suma", suma, 1);

        // here actionProxy has the interface ActionProxy and Suma
        actionProxy.showInterfaces();
        // here actionProxy has the public methods in Controller and suma method
        actionProxy.showMethods();

        // how the program should behave:
        // We are pretty sure this won't compile ever, but it is what we think it should do
        // based on the subject
        int result = (int)actionProxy.suma(Map.of("x", 1, "y", 2));

        // and this will work 100%
        //Method sumaMethod = actionProxy.getClass().getMethod("suma");
        //int result = (int) sumaMethod.invoke(actionProxy, Map.of("x", 1, "y", 2));

        System.out.println("Resultado de la suma: " + result);

        // we erase the action we added
        actionProxy.removeAction("suma");

        // This should throw an error
        try {
            result = (int)actionProxy.suma(Map.of("x", 1, "y", 2));

            // and this wouldn't either
            //Method sumaMethod = actionProxy.getClass().getMethod("suma");
            //result = (int) sumaMethod.invoke(actionProxy, Map.of("x", 1, "y", 2));
            System.out.println("Resultado de la suma: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
