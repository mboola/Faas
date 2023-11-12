//THIS IS A CONCEPTUAL VERSION, NOT A FUNCTIONAL ONE

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;

// Creo que es así cómo se utiliza el bytebuddy. Obviamente hay que importarlo. 
// Hay 2 o 3 opciones para importarlo. Una es mover el proyecto a Maven, que lo hace automático
// Otra es moverlo a otro sitio más chungo aún y la tercera es ponerlo mamnualmente. Yo lo movería a Maven.
// (Que sirve basicamente para importar dependencias automaticamente, nada mas)
public class InterfaceGenerator {
    public static Class<?> generateInterface(String interfaceName) {

        Class<?> interf = new ByteBuddy()
            .subclass(Object.class)
            .name(interfaceName)
            .defineMethod(interfaceName, void, Modifier.PUBLIC)
            .make()
            .load(
                InterfaceGenerator.getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
            .getLoaded();
        return (interf);
    }
}
