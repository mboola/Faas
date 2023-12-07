package decorator;
import java.util.function.Function;

import faas_exceptions.NoResultAvailable;
import invoker.Invoker;

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
			result = Invoker.getCacheResult(id, t);
		} //I do this in case a function is suposed to return null as a valid result
		catch (NoResultAvailable e1) {
			result = getFunction().apply(t);
			Invoker.cacheResult(id, t, result);
		}
		return (result);
	}

}
