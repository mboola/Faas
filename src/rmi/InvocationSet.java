package rmi;

import java.io.Serializable;
import java.util.List;

public class InvocationSet implements Serializable{

	private List<Long>			invokers;
	private List<InvocationSet>	compositeInvokers;
	private Long				ram;

	public InvocationSet(List<Long> invokers, List<InvocationSet> compositeInvokers, Long ram)
	{
		this.invokers = invokers;
		this.compositeInvokers = compositeInvokers;
		this.ram = ram;
	}

	public List<InvocationSet> getComposites()
	{
		return (compositeInvokers);
	}

	public Long getRam()
	{
		return (ram);
	}

	public List<Long> getInvokers()
	{
		return (invokers);
	}

	@Override
	public String toString()
	{
		String str;
		if (ram != null)
			str = "\nThis InvocationSet has " + ram + " of ram.";
		else
			str = "";
		str += "It's invokers are: ";
		for (Long invoker : invokers) {
			str += "Invoker of " + invoker + " of ram, ";
		}
		if (compositeInvokers == null)
			return (str);
		str += " and it's composite are: ";
		for (InvocationSet composite : compositeInvokers) {
			str = str + "\t" +composite.toString();
		}
		return (str);
	}
}
