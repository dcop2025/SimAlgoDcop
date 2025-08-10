package crypto.mpc.sub;

import crypto.mpc.interfaces.*;


public class OSecureKnownSubCtx implements IContext {

	public interface IContextOwner {
		IShamirAgent agnet();
		void subDone(String key);
	}
	
	
	private IContextOwner owner;
	private IShamirAgent agent;
	private String contextKey;
	
	private int aValue;
	private String bKey;
	private String oKey;

	
	private int requsetCounter;

	public OSecureKnownSubCtx(String contextKey, int aValue, String bKey, String oKey, IContextOwner owner) {
		this.contextKey = contextKey;
		this.aValue = aValue;
		this.bKey = bKey;
		this.oKey = oKey;
		this.owner = owner;
		this.agent = owner.agnet();
	}
	
	public void action() {
		init();
	}
		
	public void init() {		
		// Send a mutli requset message to eveyone
		OSubKnownRequsetMsg request = new OSubKnownRequsetMsg(contextKey, aValue, bKey, oKey);
		requsetCounter = agent.networkSize();
		agent.BroadcastMsg(request);
	}
	
	public void collectResult(int otherID) {
		requsetCounter--;
		if (requsetCounter != 0) {
			return;
		}
		done();
	}
		
	public void done() {
		owner.subDone(contextKey);
	}
	

	@Override
	public String contextKey() {		
		return contextKey;
	}
	
}
