package crypto.mpc.interfaces;

import crypto.utils.shamir.Shared;

public interface ISharedStorage {
	public Shared storeShared(String key, Shared shared);
	public Shared shared(String key);
}
