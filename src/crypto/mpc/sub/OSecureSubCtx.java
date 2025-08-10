package crypto.mpc.sub;

import crypto.mpc.interfaces.*;


public class OSecureSubCtx implements IContext {

	public interface IContextOwner {
		IShamirAgent agnet();
		void subDone(String key);
	}
	
	
	private IContextOwner owner;
	private IShamirAgent agent;
	private String contextKey;
	
	private String aKey;
	private String bKey;
	private String oKey;

	
	private int requsetCounter;

	public OSecureSubCtx(String contextKey, String aKey, String bKey, String oKey, IContextOwner owner) {
		this.contextKey = contextKey;
		this.aKey = aKey;
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
		OSubRequsetMsg request = new OSubRequsetMsg(contextKey, aKey, bKey, oKey);
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
