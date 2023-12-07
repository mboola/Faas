package decorator;
import java.util.function.Function;

public abstract class Decorator<T, R> implements Function<T, R> {

	protected Function<T, R> function;

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
