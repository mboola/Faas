package invoker;

import java.util.LinkedList;
import java.util.List;

import faas_exceptions.NoInvokerAvailable;
import faas_exceptions.NoPolicyManagerRegistered;
import faas_exceptions.OperationNotValid;
import policy_manager.PolicyManager;

public class InvokerComposite extends Invoker {

	private List<InvokerInterface>	invokers;
	private	PolicyManager			policyManager;

	private void maldad()
	{
		System.out.print("sin este print no funciona");
	}

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
		if (policyManager != null)
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
	//TODO: preguntar a Ussama
	// va demasiado rapido y no se asignan correctamente los invokers porque cuando accede a la ramAvailable esta aun no ha
	//sido actualizada
	@Override
	public InvokerInterface selectInvoker(long ram) throws Exception
	{
		InvokerInterface invoker;

		if (policyManager == null) throw new NoPolicyManagerRegistered("There isn't a policy manager registered.");
		try {
			invoker = policyManager.getInvoker(invokers, ram);
		}
		catch (NoInvokerAvailable e) {
			//if no invokers from the list have enough max ram we check if this composite has enough
			if (getMaxRam() < ram) throw new NoInvokerAvailable("");
			return (this);
		}
		System.out.println("invoker selected: " + invoker.getId() + invoker.getAvailableRam());
		//if all invokers are full this will return
		if (invoker.getAvailableRam() - ram >= 0)
		{
			System.out.println("invoker used: " +invoker.getId() + invoker.getAvailableRam());
			return (invoker);
		}
		//we have a full invoker, so we watch if the composite can be returned
		if (this.getMaxRam() >= ram)
		{
			//System.out.println("invoker used: " +this.getId() + this.getAvailableRam());
			return (this);
		}
		//System.out.println("invoker used: " +invoker.getId() + invoker.getAvailableRam());
		// if we are here, it means our invoker cannot execute the invokable but a child invoker can
		// in this case, we return the child
		// TODO: think about this
		return (invoker);
	}

}
