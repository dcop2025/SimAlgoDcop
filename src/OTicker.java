import java.util.HashMap;
import java.util.Map;

public class OTicker {
	
	private Map<String,Integer> tickers;
	
	public OTicker() {
		tickers = new HashMap<String, Integer>();
	}
	
	public boolean ticker(String tickerKey, int goal) {
		if (goal == 1) {
			return true;
		}
		Integer tick  = tickers.get(tickerKey);
		if (tick == null) {
			tick = 0;
		}
		tick++;
		if (tick == goal) {
			tickers.remove(tickerKey);
			return true;
		}
		tickers.put(tickerKey, tick);
		return false;
	}
}
