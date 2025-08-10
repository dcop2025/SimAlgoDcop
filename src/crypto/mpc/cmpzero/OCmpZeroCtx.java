package crypto.mpc.cmpzero;

import crypto.mpc.compare.OOneSubRequsetMsg;
import crypto.mpc.interfaces.IContext;
import crypto.mpc.interfaces.IShamirAgent;
import crypto.mpc.multiply.OSecureMultiplyCtx;
import crypto.utils.shamir.Shared;

public class OCmpZeroCtx implements 
		IContext,
		crypto.mpc.multiply.OSecureMultiplyCtx.IContextOwner,
		crypto.mpc.compare.OOneSubRequsetAckMsg.IOneSubCtx {

	public interface IContextOwner {
		IShamirAgent agnet();
		void cmpZeroDone(String key);
	}

	
	private IContextOwner owner;
	private IShamirAgent me;
	private String contextKey;
	
	private String aKey;
	private String cKey;

	enum zstate {
		 	STEP_1,
		 	STEP_2a,
		 	STEP_2b,
		 	DONE,
	}
	private zstate state;
	
	private int s;
	 
	
	public OCmpZeroCtx(String contextKey, String aKey, String cKey, IContextOwner owner) {
		this.owner = owner;
		this.me = owner.agnet();
		this.aKey = aKey;
		this.cKey = cKey;
		this.state = zstate.STEP_1;
	}
	
	public void action() {
		switch (this.state) {
	    case STEP_1:
	    	multiply(cKey, aKey, aKey);
	        break;
	    case STEP_2a:
	    	multiply(cKey, cKey, aKey);
	    	break;
	    case STEP_2b:
	    	multiply(cKey, cKey, cKey);
	    	break;
	    case DONE:
	    	subOneResult();
	    	//owner.cmpZeroDone(contextKey);
	    	break;
	    //default:
	    	// TODO P2 handle this errorShmuelg 
	    	//noAction();
		}

	}
	
	@Override	
	public String contextKey() {
		return contextKey;
	}
	
	private void multiply(String cKey, String xKey, String yKey) {
		OSecureMultiplyCtx mCtx = new OSecureMultiplyCtx(String.format("%s-m", contextKey), xKey, yKey, cKey, this);
		me.storeContext(mCtx.contextKey(), mCtx);
		mCtx.action();
	}

	@Override
	public IShamirAgent agnet() {
		return me;
	}

	@Override
	public void multiplyDone(String key) {
		Shared c = me.shared(cKey);
		switch (this.state) {
	    case STEP_1:
	    	s = 0;
	    	this.state = zstate.STEP_2a;
	        break;
	    case STEP_2a:
	    	this.state = zstate.STEP_2b;
	    	break;
	    case STEP_2b:
	    	if (s == 28) {
	    		this.state = zstate.DONE;
	    	} else {
		    	s++;
		    	this.state = zstate.STEP_2a;
	    	}
	    	break;
		}
		action();		
	}

	private int subAck;
	private void subOneResult() {
		OOneSubRequsetMsg msg = new OOneSubRequsetMsg(contextKey, cKey, cKey);
		subAck = me.networkSize();
		me.BroadcastMsg(msg);

	}
	
	public void collectOneSubAck(String context, String subContext, int otherID) {
		subAck--;
		if (subAck != 0) {
			return;
		}
		owner.cmpZeroDone(context);
	}
}
