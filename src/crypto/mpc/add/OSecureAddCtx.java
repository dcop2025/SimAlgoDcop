package crypto.mpc.add;

import crypto.mpc.interfaces.*;


public class OSecureAddCtx implements IContext {

	public interface IContextOwner {
		IShamirAgent agnet();
		void addDone(String key);
	}
	
	
	private IContextOwner owner;
	private IShamirAgent agent;
	private String contextKey;
	
	private String aKey;
	private String bKey;
	private String oKey;

	
	private int requsetCounter;

	public OSecureAddCtx(String contextKey, String aKey, String bKey, String oKey, IContextOwner owner) {
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
		OAddRequsetMsg request = new OAddRequsetMsg(contextKey, aKey, bKey, oKey);
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
		owner.addDone(contextKey);
	}
	

	@Override
	public String contextKey() {		
		return contextKey;
	}
	
}
