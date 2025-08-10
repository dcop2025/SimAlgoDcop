package crypto.mpc.multiply;

import java.util.Vector;

import crypto.mpc.interfaces.*;
import crypto.utils.shamir.ShamirSharedGen;
import crypto.utils.shamir.Shared;


public class OSecureMultiplyCtx implements IContext {

	public interface IContextOwner {
		IShamirAgent agnet();
		void multiplyDone(String key);
	}
	
	 enum zstate {
		 	INIT,
		 	COLLECT_MULTI_RES,
		  }

	
	private IContextOwner owner;
	private IShamirAgent agent;
	private String contextKey;
	private zstate state;
	private Vector<Shared> collected;
	
	private String aKey;
	private String bKey;
	private String cKey;

	
	private int requsetCounter;

	public OSecureMultiplyCtx(String contextKey, String aKey, String bKey, String cKey, IContextOwner owner) {
		this.contextKey = contextKey;
		this.aKey = aKey;
		this.bKey = bKey;
		this.cKey = cKey;
		this.owner = owner;
		this.agent = owner.agnet();
	}
	
	public void action() {
		switch (this.state) {
	    case INIT:
	    	init();	    	
	        break;
	    default:
	    	noAction();
		}
	}
		
	public void init() {
		
		collected = new Vector<Shared>();
		// Send a mutli requset message to eveyone
		OMultiplyRequsetMsg request = new OMultiplyRequsetMsg(contextKey, aKey, bKey);
		requsetCounter = agent.networkSize();
		agent.BroadcastMsg(request);
	}
	
	public void collectResult(int otherID, Shared c_2t_r) {
		requsetCounter--;
		collected.add(c_2t_r);
		if (requsetCounter != 0) {
			return;
		}
		long prime = agent.prime();
		
		long reconstruct  = ShamirSharedGen.reconstruct(collected, prime);
		// TODO Send a message to everyone with the reconstruct value
		OMultiplyResultMsg result = new OMultiplyResultMsg(contextKey, cKey, reconstruct);
		requsetCounter = agent.networkSize();
		agent.BroadcastMsg(result);
	}
		
	public void multiplyDoneAck() {
		requsetCounter--;
		if (requsetCounter != 0) {
			return;
		}
		owner.multiplyDone(contextKey);
	}
	
	private void noAction() {
		
	}

	public String contextKey() {		
		return contextKey;
	}
	
}
