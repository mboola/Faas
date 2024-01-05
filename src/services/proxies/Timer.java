package services.proxies;

import java.time.Duration;

public class Timer implements TimerProxy{

	public Timer() {
	}

	public Object waitSec(int time) throws RuntimeException{
		long sec = Duration.ofMillis(time).toMillis();
		try {
			Thread.sleep(sec);
			return "Done!";
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
