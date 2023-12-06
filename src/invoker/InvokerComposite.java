package invoker;

import java.util.LinkedList;
import java.util.List;

import faas_exceptions.NoPolicyManagerRegistered;
import faas_exceptions.OperationNotValid;
import policy_manager.PolicyManager;

public class InvokerComposite extends Invoker {

	private List<InvokerInterface>	invokers;
	private	PolicyManager			policyManager;

	public static InvokerComposite createInvoker(long ram)
	{
		if (ram <= 0)
			return (null);
		else
			return (new InvokerComposite(ram));
	}

	private InvokerComposite(long ram) 
	{
		super(ram);
		invokers = new LinkedList<InvokerInterface>();
	}

	/**
	 * Adds the implementation of InvokerInterface to the list of registered invokers.
	 * 
	 * @param invoker The InvokerInterface to be registered.
	 * @throws OperationNotValid If the invoker passed as a parameter is null or is already inside the list.
	 */
	public void registerInvoker(InvokerInterface invoker) throws OperationNotValid
	{
		if (invoker == null) throw new OperationNotValid("Invoker to register cannot be null.");
		if (invokers.contains(invoker)) throw new OperationNotValid("Invoker is already registered.");
		invokers.add(invoker);
		invoker.setPolicyManager(policyManager);
	}

	public void	setPolicyManager(PolicyManager policyManager)
	{
		this.policyManager = policyManager.copy();
	}

	/**
	 * Removes the implementation of InvokerInterface from the list of registered invokers.
	 * 
	 * @param invoker The InvokerInterface to be removed.
	 * @throws OperationNotValid If the invoker passed as a parameter is null or is not inside the list.
	 */
	public void deleteInvoker(InvokerInterface invoker) throws OperationNotValid
	{
		if (invoker == null) throw new OperationNotValid("Invoker to delete cannot be null.");
		if (!invokers.contains(invoker)) throw new OperationNotValid("Invoker is not registered.");
		invokers.remove(invoker);
	}

	//TODO: redo this javadoc
	/**
	 * Method used to select a invoker to execute a function based on the ram it consumes and the policy we have assigned.
	 * @param ram
	 * @return
	 * @throws Exception <p>The exception can be caused because:</p>
	 * <ul>
	 * 	<li>NoPolicyManagerRegistered: There is no policyManager registered.</li>
	 * 	<li>NoInvokerAvailable: There is no invoker with enough max ram to execute the invokable.</li>
	 *  <li>Exeption: something goes wrong with RMI.</li>
	 * </ul>
	 */
	@Override
	public InvokerInterface selectInvoker(long ram) throws Exception
	{
		InvokerInterface invoker;

		if (policyManager == null) throw new NoPolicyManagerRegistered("There isn't a policy manager registered.");
		invoker = policyManager.getInvoker(invokers, ram);
		if (invoker.getAvailableRam() == 0 && getAvailableRam() >= ram)
			return (this);
		return (invoker);
	}

}
