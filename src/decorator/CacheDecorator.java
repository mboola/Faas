package decorator;
import java.util.function.Function;

import application.Invoker;
import exceptions.NoResultAvaiable;

public class CacheDecorator<T, R> extends Decorator<T, R>{

	private String	id;

	public CacheDecorator(Function<T, R> function, String id) {
		super(function);
		this.id = id;
	}

	@Override
	public R apply(T t){
		R		result;

		try {
			result = Invoker.getResult(id, t);
		} //I do this in case a function is suposed to return null as a valid result
		catch (NoResultAvaiable e1) {
			result = getFunction().apply(t);
			Invoker.storeResult(id, t, result);
		}
		return (result);
	}

}
