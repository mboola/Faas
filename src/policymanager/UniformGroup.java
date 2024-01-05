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
    private long chargeDistributed;

    private int	lastInvokerAssigned;

    //values to distribute the charge in single invocations without knowing the ram it will consume
    private void setSingleUniformValues()
    {
        groupSize = 1;
        chargeDistributed = 0;
    }

    private void setGroupUniformValues(List<InvokerInterface> invokers, int numInvocations, long ram)
    {
        //get the average of invokers ram available, num of invokers, and how may invokers can execute the function
        long    numInvokersMaxRamAvailable;
        long    numInvokersRamAvailable;
        
        //here I get the num of invokers that can execute the function
        numInvokersMaxRamAvailable = invokers.stream()
            .filter(value -> {
                try {
                    return value.getMaxRam() >= ram;
                } catch (RemoteException e) {
                    return false;
                }
            })
            .count();

        //and here I get the num of invokers that have enough ram to execute
        numInvokersRamAvailable = invokers.stream()
            .filter(value -> {
                try {
                    return value.getAvailableRam() >= ram;
                } catch (RemoteException e) {
                    return false;
                }
            })
            .count();
        
        if (numInvokersRamAvailable )
        
        
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
