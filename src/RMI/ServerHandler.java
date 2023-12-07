package RMI;

public class ServerHandler {
	public static void main(String[] args) {

        System.out.println("ServerHandler working.");

		String[] portInvoker1 = new String[] {"1099"};
		ServerInvoker.main(portInvoker1);

		String[] portInvoker2 = new String[] {"1199"};
		ServerInvoker.main(portInvoker2);
       
    }
}
