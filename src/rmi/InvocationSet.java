package rmi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.List;

/**
 * Represents a composite invoker and it's data, using a list of Longs representing the RAM of each invoker
 * inside this composite. It also has a list of composites information and the RAM of the composite.
 * This ram can be null if we want to indicate that this is the data used to represent the Controller.
 * This class is Serializable, making it suitable for transmission over a network, such as in
 * Remote Method Invocation (RMI) scenarios.
 *
 * @see core.invoker.InvokerInterface
 * @see core.invoker.Invoker
 * @see core.invoker.CompositeInvoker
 * @see Serializable
 */
public class InvocationSet implements Serializable{

	private List<Long>			invokers;
	private List<InvocationSet>	compositeInvokers;
	private Long				ram;

	/**
     * Constructs an InvocationSet with the specified lists of invokers, composite invokers, and RAM.
     *
     * @param invokers           The list of individual invokers represented by their RAM value in megabytes.
     * @param compositeInvokers  The list of composite invokers, each represented by another InvocationSet.
     * @param ram                The amount of RAM the composite has.
     */
	public InvocationSet(List<Long> invokers, List<InvocationSet> compositeInvokers, Long ram)
	{
		this.invokers = invokers;
		this.compositeInvokers = compositeInvokers;
		this.ram = ram;
	}

	/**
     * Gets the list of composite invokers associated with this InvocationSet.
     *
     * @return The list of composite invokers.
     */
	public List<InvocationSet> getComposites()
	{
		return (compositeInvokers);
	}

	/**
     * Gets the amount of RAM of the composite to create.
     *
     * @return The amount of RAM required.
     */
	public Long getRam()
	{
		return (ram);
	}

	/**
     * Gets the list of individual invokers represented by their ram.
     *
     * @return The list of individual invokers.
     */
	public List<Long> getInvokers()
	{
		return (invokers);
	}

	public static String serializeToString(Object obj) {
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
}
