package crypto.mpc.compare;

import java.util.Vector;

import crypto.mpc.interfaces.*;
import crypto.mpc.multiply.OSecureMultiplyCtx;
import crypto.utils.shamir.ShamirSharedGen;
import crypto.utils.shamir.Shared;

public class OSecureLSBCtx implements IContext, crypto.mpc.multiply.OSecureMultiplyCtx.IContextOwner {

	public interface IContextOwner {
		IShamirAgent agnet();
		void lsbDone(String key);
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
	private String oKey;
		
	private Vector<Shared> cCollected;
	private long c;

	private int index;
	
	private int prepAckCollect;
	private int phase1AckCounter;
	private int phase2AckCounter;
	private int finaleAckCounter;
	
	public OSecureLSBCtx(String contextKey, String aKey, String oKey, IContextOwner owner) {
		this.contextKey = contextKey;
		this.aKey = aKey;
		this.oKey = oKey;
		
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
		cCollected = new Vector<Shared>();
		OPrepLSBRequestMsg msg = new OPrepLSBRequestMsg(contextKey, aKey, "r-key");
		prepAckCollect = agent.networkSize();
		agent.BroadcastMsg(msg);		
	}
	
		
	public void collectPrep(int otherID, Shared cShared) {
		prepAckCollect--;
		cCollected.add(cShared);
		if (prepAckCollect != 0) {
			return;
		}
		long prime = agent.prime();
		
		this.c  = ShamirSharedGen.reconstruct(cCollected, prime);
		this.index = 30;
		 
		
		OCalcBWCPhase1Msg msg = new OCalcBWCPhase1Msg(
				contextKey, this.c, "r-key", index);
		phase1AckCounter = agent.networkSize();
		agent.BroadcastMsg(msg);
	}
	
	public void collectPhase1Ack(int otherID) {
		phase1AckCounter--;
		if (phase1AckCounter != 0) {
			return;
		}

		if (isFirstBit()) {
			// When it the first bit, there is no need to calc d and cd, let's move to phase 2
			BroadcastBWCPhase2();
		} else {
			// the formula to calc d[index] is d[index] = d[index+1] + c[index] + d[index+1]*c[index]
			// therefore need to calc d[index+1]*c[index] first
			OSecureMultiplyCtx dCtx = new OSecureMultiplyCtx(
					String.format("%s-%s", contextKey,  bitKey(contextKey+"-cd", index)),
					bitKey(contextKey+"-c", index), bitKey(contextKey+"-d", index+1), bitKey(contextKey+"-cd", index),
					this);
			this.agent.storeContext(dCtx.contextKey(), dCtx);
			dCtx.action();

		}
	}

	public void collectPhase2Ack(int otherID) {
		phase2AckCounter--;
		if (phase2AckCounter != 0) {
			return;
		}

		if (isLastBit()) {
			OSecureMultiplyCtx dCtx = new OSecureMultiplyCtx(
					String.format("%s-f", contextKey),
					contextKey+"-e", contextKey+"-1-ea", contextKey+"-f",
					this);
			this.agent.storeContext(dCtx.contextKey(), dCtx);
			dCtx.action(); 
		} else {
			index--;			
			OCalcBWCPhase1Msg msg = new OCalcBWCPhase1Msg(
					contextKey, this.c,"r-key", index);
			phase1AckCounter = agent.networkSize();
			agent.BroadcastMsg(msg);
		}
	}

	
	private boolean isFirstBit() {
		return (index == 30);
	}
	
	private boolean isLastBit() {
		return (index == 0);
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
	
	static private String bitKey(String key, int index) {
		return String.format("%s(%d)", key, index);
	}
	
	public void multiplyDone(String key) {
		if (key.equals( String.format("%s-%s", contextKey,  bitKey(contextKey+"-cd", index)))) {
			BroadcastBWCPhase2();
			return;
		}
		if (key.equals(String.format("%s-f", contextKey))) {
			OSecureMultiplyCtx dCtx = new OSecureMultiplyCtx(
					String.format("%s-ed0", contextKey),
					contextKey+"-f", bitKey(contextKey+"-d", 0), contextKey+"-ed0",
					this);
			this.agent.storeContext(dCtx.contextKey(), dCtx);
			dCtx.action();			
			return;
		}
		if (key.equals(String.format("%s-ed0", contextKey))) {
			BroadcaseLSBFinale();
			return;
		}
	
		return;
	}
	
	private void BroadcastBWCPhase2() {
		OCalcBWCPhase2Msg msg = new OCalcBWCPhase2Msg(contextKey, this.c, index);
		phase2AckCounter = agent.networkSize();
		agent.BroadcastMsg(msg);		
	}

	private void BroadcaseLSBFinale() {
		OCalcLSBFinaleMsg msg = new OCalcLSBFinaleMsg(contextKey, this.oKey);
		finaleAckCounter = agent.networkSize();
		agent.BroadcastMsg(msg);
	}
	
	public void collectFinalAck(String key) {
		finaleAckCounter--;
		if (finaleAckCounter != 0) {
			return;
		}
		owner.lsbDone(contextKey);

		
	}
}
