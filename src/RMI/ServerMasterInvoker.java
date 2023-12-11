package RMI;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Base64;

import application.Controller;

/*
 * This class is different from ServerInvoker. ServerInvoker only creates an invoker but doesn't
 * register invokers to his list.
 * This class will register Invokers to his list.
 */
public class ServerMasterInvoker {

	private static String serializeToString(Object obj) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {

            oos.writeObject(obj);
            byte[] bytes = bos.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

	public static void main(String[] args) {

		InvocationSet composite = new InvocationSet(Arrays.asList(1L, 1L), null, 1L);
		InvocationSet controllerSet = new InvocationSet(Arrays.asList(2L), Arrays.asList(composite), null);
		
		//System.out.println(controller.toString());
		String serializedController = serializeToString(controllerSet);
		String[] argServerHandler = new String[]{serializedController};

		Controller controller = Controller.instantiate();

		ServerHandler.main(argServerHandler);
	}

}
