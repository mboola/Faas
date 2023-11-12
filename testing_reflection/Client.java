//THIS IS A CONCEPTUAL VERSION, NOT A FUNCTIONAL ONE

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

public class Client {
    public static void main(String[] args) throws Exception {

        //this is clear
        Controller controller = Controller.instantiate();

        //this is the application client will call
		ActionProxy actionProxy = (ActionProxy) DynamicProxy.instantiate(controller);

        //** here actionProxy doesn't have the method suma and only has the interface ActionProxy */
        //Interfaces of Proxy:
        /*
        for (Class<?> interf : actionProxy.getClass().getInterfaces()) {
            System.out.println(interf.getName());
        }
        */
        //System.out.println("Acciones disponibles: " + actionProxy.actions());

        //we register two actions. each time an action is added, actionProxy should be recompiled
        Function<Map<String, Integer>, Integer> suma = x -> x.get("x") + x.get("y");
        actionProxy.registerAction("suma", suma, 1);

        //** here actionProxy has the method suma and the interface ActionProxy and suma */
        //Interfaces of Proxy:
        /*
        for (Class<?> interf : actionProxy.getClass().getInterfaces()) {
            System.out.println(interf.getName());
        }
        */
        //System.out.println("Acciones disponibles: " + actionProxy.actions());

        //testing of behaviour (?)
        try {
            // Aquí obtenemos el metodo "suma" mediante reflexión
            Method sumaMethod = actionProxy.getClass().getMethod("suma");
            // Aquí lo invocamos mediente reflexión
            int resultadoSuma = (int) sumaMethod.invoke(actionProxy, 2, 3);
            System.out.println("Resultado de la suma: " + resultadoSuma);
            // ¿Esto no iría un poco  en contra de lo que queríamos conseguir? En principio, deberíamos poder llamar a actionProxy.suma();
            // ¿El problema? Creo que daría un error de compilación, ya que suma no está definida en la interfaz por defecto
            // CONSULTAR

            Method restaMethod = actionProxy.getClass().getMethod("resta");
            int resultadoResta = (int) restaMethod.invoke(actionProxy, 5, 2);
            System.out.println("Resultado de la resta: " + resultadoResta);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Eliminamos la suma de la lista
        actionProxy.removeAction("suma");
        // Esto debería dar error
        try {
             Method sumaMethod = actionProxy.getClass().getMethod("suma");
            int resultadoSuma = (int) sumaMethod.invoke(actionProxy, 2, 3);
            System.out.println("Resultado de la suma: " + resultadoSuma);
        } catch (Exception e) {
            e.printStackTrace();
        }
		
    }
}
