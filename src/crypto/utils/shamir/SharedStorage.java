package crypto.utils.shamir;

import java.util.HashMap;
import java.util.Map;

public class SharedStorage {
	private Map<String, Shared> shortTermStorage;
	private Map<String, Shared> longTermStorage;

	
	public SharedStorage() {
		shortTermStorage = new HashMap<String, Shared>();
		longTermStorage  = new HashMap<String, Shared>();
	}
	
	
	public Shared put(String key, Shared shared) {
		return shortTermStorage.put(key, shared);		
	}
	
	public Shared get(String key) {
		return shortTermStorage.get(key);
	}
	
	
	public void InjectRef(String refKey, Shared ref, Shared refBits[]) {
		injectRef(shortTermStorage, refKey, ref, refBits);
		injectRef(longTermStorage, refKey, ref, refBits);
	}
	
	
	public void clear(boolean fullCleanUp) {
		shortTermStorage.clear();
		if (fullCleanUp == true) {
			longTermStorage.clear();
		} else {
			//  if not clearing the long term, than the short term should hold the long term as well
			shortTermStorage = new HashMap<String, Shared>(longTermStorage);
		}
	}

	private void injectRef(Map<String, Shared> storage, String refKey, Shared ref, Shared refBits[]) {
		storage.put(refKey, ref);
		if (refBits == null) {
			return;
		}
		for (int i = 0; i < refBits.length; i++) {			
			storage.put(bitKey(refKey, i), refBits[i]);
		}
	}
	
	static public String bitKey(String key, int index) {
		return String.format("%s(%d)", key, index);
	}
}

