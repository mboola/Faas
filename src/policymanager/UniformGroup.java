package policymanager;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import core.exceptions.NoInvokerAvailable;
import core.exceptions.NoPolicyManagerRegistered;
import core.invoker.InvokerInterface;

/**
 * Implementation of the PolicyManager interface using a uniform distribution
 * of function assignments among Invokers.
 */
public class UniformGroup implements PolicyManager{

    private boolean executingGroup;

    private long groupSize;
    private long invocationsDistributed;

    private int	lastInvokerAssigned;

    //values to distribute the charge in single invocations without knowing the ram it will consume
    private void setSingleUniformValues()
    {
        groupSize = 1;
        invocationsDistributed = 0;
    }

    private void setGroupUniformValues(List<InvokerInterface> invokers, int numInvocations, long ram) throws NoInvokerAvailable
    {
        //get the average of invokers ram available, num of invokers, and how may invokers can execute the function
        long    invokersMaxRamUsable;
        long    invokersRamAvailable;
        double  averageRamAvailable;
        
        //here I get the num of invokers that can execute the function
        invokersMaxRamUsable = invokers.stream()
            .filter(value -> {
                try {
                    return value.getMaxRam() >= ram;
                } catch (RemoteException e) {
                    return false;
                }
            })
            .count();
        //If not a single invoker can execute this function
        if (invokersMaxRamUsable == 0)
            throw new NoInvokerAvailable("No Invoker Avaiable with at least " + ram + " RAM.");

        //and here I get the invokers that have available ram
        invokersRamAvailable = invokers.stream()
            .filter(value -> {
                try {
                    return value.getAvailableRam() >= ram;
                } catch (RemoteException e) {
                    return false;
                }
            })
            .count();

        //and here I get the average of those who have available ram
        averageRamAvailable = invokers.stream()
            .filter(value -> {
                try {
                    return value.getAvailableRam() >= ram;
                } catch (RemoteException e) {
                    return false;
                }
            })
            .mapToDouble(value -> {
                try {
                    return value.getAvailableRam();
                } catch (RemoteException e) {
                    return 0;
                }
            })
            .average()
            .orElse(0.0);
        
        //if the charge can be distributed between the available invokers and their ram
        if (averageRamAvailable * invokersRamAvailable >= numInvocations * ram)
        {
            //we will distribute the charge to only available ones.
            
        }
        else
        {
            //if it cannot be distributed to only available ones, we distribute it
            //to all of them (that can execute it)

        }
    }

    public UniformGroup() {
        lastInvokerAssigned = 0;
        executingGroup = false;
        setSingleUniformValues();
    }

    @Override
    public PolicyManager copy() {
        return (new UniformGroup());
    }

    public void	prepareDistribution(List<InvokerInterface> invokers, int size, long ram, boolean singleInvocation)
    {
        //do we need to prepare the group in the most efficient distribution possible?
        if (singleInvocation && executingGroup)
        {
            executingGroup = false;
            setSingleUniformValues();
        }
        else if (!singleInvocation)
        {
            executingGroup = true;
            setGroupUniformValues(invokers, size, ram);
        }
    }

}
