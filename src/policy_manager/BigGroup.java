package policy_manager;

import java.util.List;

import faas_exceptions.NoInvokerAvailable;
import invoker.InvokerInterface;

public class BigGroup implements PolicyManager{

    private int maxRetries;            
    private int groupSize;
    private int lastInvokerAssigned;
    private int count;
    private int retryCount;
    private long totalRamConsumed; // Variable global que almacena la RAM consumida total del grupo anterior

    public BigGroup(int maxRetries, int groupSize) {
        this.maxRetries = maxRetries;
        this.groupSize = groupSize;
        lastInvokerAssigned = 0;
        count = 0;
        retryCount = 0;
        totalRamConsumed = 0;
    }
    
    @Override
    public InvokerInterface getInvoker(List<InvokerInterface> invokers, long ram) throws Exception {

        if (invokers.isEmpty()) throw new NoInvokerAvailable("No Invokers in list.");

        InvokerInterface invoker = invokers.get(lastInvokerAssigned).selectInvoker(ram);
        
        if (invoker.getMaxRam() >= ram) {
            count++;
            totalRamConsumed = totalRamConsumed + ram;
            if (count == groupSize) {
                if (invoker.getAvailableRam() < totalRamConsumed) {
                    lastInvokerAssigned = (lastInvokerAssigned + 1) % invokers.size();
                }
                totalRamConsumed = 0;
                count = 0; 
            }
            retryCount = 0;
            return invoker;
        }

        lastInvokerAssigned = (lastInvokerAssigned + 1) % invokers.size();
        count = 0;
        totalRamConsumed = 0;
        retryCount++;

        if (retryCount > maxRetries) {
            retryCount = 0; 
            throw new NoInvokerAvailable("Unable to find a suitable invoker after"+maxRetries+" tries.");
        }

        return getInvoker(invokers, ram);                                                   // Función recursiva, hará una serie de #MAX_RETRIES intentos antes hacer el throw. 
    }
    
    /**
     * Sets group size for the BigGroup.
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
}
