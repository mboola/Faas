package policy_manager;
import java.util.List;

import faas_exceptions.NoInvokerAvailable;
import invoker.InvokerInterface;

/**
 * Implementation of the PolicyManager interface using a uniform distribution
 * of function assignments among Invokers.
 */
public class UniformGroup implements PolicyManager{

    private static int maxRetries= 3;            
    private int groupSize;
    private int lastInvokerAssigned;
    private int count;
    private int retryCount;

    public UniformGroup() {
        groupSize = 2;
        lastInvokerAssigned = 0;
        count = 0;
        retryCount = 0;
    }

    
    /**
     * Retrieves an available Invoker based on the uniform distribution policy.
     *
     * * This method implements the Uniform Group policy to distribute function invocations
	 * in predefined group sizes among available Invokers.
     * 
     * @param invokers The list of available Invokers.
	 * @param ram The minimum RAM required for the function invocation.
	 * @return The selected Invoker.
     * @throws NoInvokerAvailable If no suitable Invoker can be found within the specified retries.
     */
    @Override
    public InvokerInterface getInvoker(List<InvokerInterface> invokers, long ram) throws Exception {
        InvokerInterface invoker = invokers.get(lastInvokerAssigned);

        if (invokers.isEmpty()) throw new NoInvokerAvailable("No Invokers in list.");
        
        if (invoker.getMaxRam() >= ram) {
            count++;
            if (count == groupSize) {
                lastInvokerAssigned = (lastInvokerAssigned + 1) % invokers.size();
                count = 0; 
            }
            retryCount = 0;
            return invoker;
        }

        lastInvokerAssigned = (lastInvokerAssigned + 1) % invokers.size();
        count = 0;
        retryCount++;

        if (retryCount > maxRetries) {
            retryCount = 0; 
            throw new NoInvokerAvailable("Unable to find a suitable invoker after"+maxRetries+" tries.");
        }

        return getInvoker(invokers, ram);                                                   // Función recursiva, hará una serie de #MAX_RETRIES intentos antes hacer el throw. 
    }
    
    
    /**
     * Sets group size for the UniformGroup.
     *
     * @param newSize The new group size to set. Must be greater than 0.
     * @throws IllegalArgumentException If the provided group size is not greater than 0.
     */
    public void setSize(int newSize) throws IllegalArgumentException {                      // No se si esto lo necesitaremos, pero no está de más tenerlo.  
        if (newSize > 0) {
            this.groupSize = newSize;
        } else {
            throw new IllegalArgumentException("Group size must be greater than 0.");
        }
    }

    /**
     * Sets the maximum number of retries allowed to find a suitable Invoker.
     *
     * @param newMaxRetries The new value for maxRetries.
     * @throws IllegalArgumentException If the provided value is less than 0.
     */
    public static void setMaxRetries(int newMaxRetries) throws IllegalArgumentException {   // Esto tampoco se si hará falta, pero sería interesante
        if (newMaxRetries >= 0) {                                                           // de cara a que la función se adaptara si necesitara hacer
            maxRetries = newMaxRetries;                                                     // más retries. 
        } else {
            throw new IllegalArgumentException("Max retries must be greater than or equal to 0.");
        }
    }


    @Override
    public PolicyManager copy() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }


}
