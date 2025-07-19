package crypto.mpc.testing;

import java.util.Vector;

import crypto.mpc.interfaces.*;
import crypto.mpc.multiply.OSecureMultiplyCtx;
import crypto.utils.shamir.ShamirSharedGen;
import crypto.utils.shamir.Shared;

public class OTestMultiplyMPCCtx implements 
	IContext, 
	OInjectSharedsCtx.IContextOwner, 
	crypto.mpc.multiply.OSecureMultiplyCtx.IContextOwner,
	OReconstructRequsetAckMsg.IReconstructCtx {

	public interface IContextOwner {
		void multiplyTestOver(String contextKey);
	} 
	
	 enum zstate {
		 	INIT,
		 	PENNDING_ON_INJECT,
		    DONE,
		  }

	private IShamirAgent agent;
	private String key;
	private String aKey;
	private int a;
	private String bKey;
	private int b;
	private String cKey;
	private zstate state;
	 
	private int injectCounter;
	private int requestCounter;
	private Vector<Shared> collected;
	
	public OTestMultiplyMPCCtx(
			String key,
			IShamirAgent agent, 
			String aKey, int a,
			String bKey, int b,
			String cKey) {
		this.key = key;
		this.agent = agent;
		this.aKey = aKey;
		this.a = a;
		this.bKey = bKey;
		this.b = b;
		this.cKey = cKey;
		this.state = zstate.INIT;
		
	}
	
	public String contextKey() {
		return key;
	}

	public void action() {
		switch (this.state) {
	    case INIT:
	    	init();	    	
	        break;
	    case PENNDING_ON_INJECT:
	    	penndingOnInject();
	    default:
	    	noAction();
		}
	}
	
	private void init() {
		// create a context for injecting a
		//Global.log.logln(true, "ID:" + agent.agentID() + " " + String.format("testing multi init"));				
		injectCounter = 0;
		
		OInjectSharedsCtx injectA = new OInjectSharedsCtx(
				String.format("%s-%s", key, aKey),  // context name
				aKey, a, // shared
				this);
		this.agent.storeContext(injectA.contextKey(), injectA);
		injectA.action();
		injectCounter++;
		
		// create a context for injecting b
		OInjectSharedsCtx injectB = new OInjectSharedsCtx(
				String.format("%s-%s", key, bKey),  // context name
				bKey, b, // shared
				this);
		this.agent.storeContext(injectB.contextKey(), injectB);
		injectB.action();
		injectCounter++;
		
		this.state = zstate.PENNDING_ON_INJECT;
		collected = new Vector<Shared>();

	}
	
	private void penndingOnInject() {
		
	}
	
	private void noAction() {
		// TODO: log error
	}

	public IShamirAgent agnet() {
		return this.agent;
	}
	
	public void injectSharedsDone(String key) {
		injectCounter--;
		if (injectCounter != 0) {
			return;
		}
		// Now we are ready for the do the mulitply, let's create a context for it
		OSecureMultiplyCtx ctx = new OSecureMultiplyCtx(
				String.format("multi-%s-%s", key, cKey),
				aKey, bKey, cKey,
				this);
		this.agent.storeContext(ctx.contextKey(), ctx);
		ctx.init();
		
	}
	
	public void multiplyDone(String key) {
		// log done with
		OReconstructRequsetMsg reconstructMsg = new OReconstructRequsetMsg(this.key, cKey);
		requestCounter = agent.networkSize();
		agent.BroadcastMsg(reconstructMsg);		
	}
	
	public void collectReconstruct(String key, Shared shared) {
		requestCounter--;
		collected.add(shared);
		if (requestCounter != 0) {
			return;
		}
		long prime = agent.prime();
		
		long reconstruct  = ShamirSharedGen.reconstruct(collected, prime);
		// TODO Send a message to everyone with the reconstruct value
		//Global.log.logln(true, "ID:" + agent.agentID() + " " + String.format("result is %d", reconstruct));
		
	}
}
