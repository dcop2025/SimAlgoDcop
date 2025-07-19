package crypto.mpc.compare;

import crypto.mpc.interfaces.*;

public class OSecureCompareHalfPrimeCtx  implements IContext, crypto.mpc.compare.OSecureLSBCtx.IContextOwner {

	public interface IContextOwner {
		IShamirAgent agnet();
		void compareHalfPrimeDone(String key);
	}
	
	 enum zstate {
		 	INIT,
		 	COLLECT_MULTI_RES,
		  }

	
	private IContextOwner owner;
	private IShamirAgent agent;
	private String contextKey;	
	private String aKey;
	private String oKey;
	
	
	private String a2Key;
	private String lsbContextKey;
	
	private int multiAck;

	public OSecureCompareHalfPrimeCtx(String contextKey, String aKey, String oKey, IContextOwner owner) {
		this.contextKey = contextKey;
		this.aKey = aKey;
		this.oKey = oKey;
		this.owner = owner;
		this.agent = owner.agnet();
		
		this.a2Key = String.format("%s-x2", this.aKey);
		this.lsbContextKey = String.format("%s-lsb", contextKey);
	}
	
	public void init( ) {
		OConstMultiRequsetMsg request = new OConstMultiRequsetMsg(contextKey, aKey, 2, a2Key);
		multiAck = agent.networkSize();
		agent.BroadcastMsg(request);
	}

	@Override
	public String contextKey() {		
		return contextKey;
	}

	public void collectConstMultiAck(int otherId ) {
		multiAck--;
		if (multiAck != 0) {
			return;
		}
		OSecureLSBCtx lsbCtx = new OSecureLSBCtx(
				lsbContextKey,
				a2Key,
				oKey,
				this);
		agent.storeContext(lsbCtx.contextKey(), lsbCtx);
		lsbCtx.init();
	}
	
	public void lsbDone(String key) {
		// After extracting the LSB, a proper compare to half prime should do 1 - [[lsb(a)]]
		// but since the total compare operation will do 1 - [[compare-half-prime(a)]] this is redundant, let's override just return the result 
		/*
		OOneSubRequsetMsg request = new OOneSubRequsetMsg(contextKey, aKey, oKey);
		subAck = agent.networkSize();
		agent.BroadcastMsg(request);
		*/		
		owner.compareHalfPrimeDone(contextKey);
	}
	
	public IShamirAgent agnet() {
		return agent;
	}

}
