package crypto.mpc.compare;


import crypto.mpc.interfaces.*;

public class OSecureCompareCtx implements IContext, crypto.mpc.compare.OSecureCompareHalfPrimeCtx.IContextOwner, OOneSubRequsetAckMsg.IOneSubCtx {

	public interface IContextOwner {
		IShamirAgent agnet();
		void compareDone(String key);
	}
	
	 enum zstate {
		 	INIT,
		 	COLLECT_MULTI_RES,
		  }

	
	private IContextOwner owner;
	private IShamirAgent agent;
	private String contextKey;
	private zstate state;
	
	private String aKey;
	private String bKey;
	private String oKey;
	
	private String aSbKey;
	private String yContextKey;

	
	private int subAck;

	public OSecureCompareCtx(String contextKey, String aKey, String bKey, String oKey, IContextOwner owner) {
		this.contextKey = contextKey;
		this.aKey = aKey;
		this.bKey = bKey;
		this.oKey = oKey;
		
		this.owner = owner;
		this.agent = owner.agnet();
		
		this.aSbKey = String.format("%s-%s", aKey, bKey);
		this.yContextKey = String.format("%s-%s-y", contextKey, aSbKey);
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
		OSubtractionRequsetMsg request = new OSubtractionRequsetMsg(contextKey, aKey, bKey, aSbKey);
		subAck = agent.networkSize();
		agent.BroadcastMsg(request);
	}
	
	public void collectSubtractionAck(int otherID) {
		subAck--;
		if (subAck != 0) {
			return;
		}
		OSecureCompareHalfPrimeCtx cmpHalfPrime = new OSecureCompareHalfPrimeCtx(
				yContextKey,
				aSbKey,
				oKey,
				this);
		agent.storeContext(cmpHalfPrime.contextKey(), cmpHalfPrime);
		cmpHalfPrime.init();
	}
		
		
	private void noAction() {
		
	}

	@Override
	public String contextKey() {		
		return contextKey;
	}
	
	public IShamirAgent agnet() {
		return agent;
	}
	
	public void compareHalfPrimeDone(String key) {
		// the result should be 1-compare-half-prime, but no real need to do it, as it cancled
		// by not doing it in the LSB as well.
		owner.compareDone(contextKey);
	}


	public void collectOneSubAck(String context, String subContext, int otherID) {
		subAck--;
		if (subAck != 0) {
			return;
		}
		owner.compareDone(this.contextKey);
	}
}
