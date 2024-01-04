package services.proxies;

import java.util.concurrent.Future;

public interface TimerProxy {

	public Future<String> sleep(int time);

}
