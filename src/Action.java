public class Action<Integer, Object> {

	private int		ram;
	private Object	function;

	public Action(int ram, Object function) {
		this.ram = ram;
		this.function = function;
	}

	public int getRam()
	{
		return (this.ram);
	}

	public Object getFunction()
	{
		return (this.function);
	}
}
