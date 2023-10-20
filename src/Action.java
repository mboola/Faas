import java.util.function.Function;

public interface Action<T, R> extends Function<T, R> {
    R run(T arg) throws Exception;
}
