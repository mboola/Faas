import java.util.function.Function;

public class TimerDecorator<T, R> extends Decorator<T, R>{

	public TimerDecorator(Function<T, R> function) {
		super(function);
	}

	@Override
	public R apply(T t) {
		long	timeExecution;
		R		result;

		timeExecution = System.currentTimeMillis();
		result = getFunction().apply(t);
		timeExecution = System.currentTimeMillis() - timeExecution;
		System.out.println("Time of execution is " + timeExecution);
		return (result);
	}

}
