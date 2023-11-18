package dynamic_proxy.proxies;

import java.util.concurrent.Future;

public class Timer implements TimerProxy{

	public Timer() {
	}

	public Future<String> sleep(int time) {
		return (null);
	}
}
