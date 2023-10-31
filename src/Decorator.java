import java.util.function.Function;

public class Decorator<T, R> implements Function<T, R> {

	private Function<T, R> function;

	public Decorator(Function<T, R> function) {
		super();
		this.function = function;
	}

	@Override
	public R apply(T t) {
		return (function.apply(t));
	}

	public Function<T, R> getFunction()
	{
		return (function);
	}

}
