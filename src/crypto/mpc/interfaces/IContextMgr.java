package crypto.mpc.interfaces;

// IConetxtMgr is an interface to a Context Manager object
public interface IContextMgr {

	// context retrieves the context by the context key
	IContext context(String contextKey);
	
	// storeConetxt stores a context under the context key
	void storeContext(String contextKey, IContext ctx);
}
