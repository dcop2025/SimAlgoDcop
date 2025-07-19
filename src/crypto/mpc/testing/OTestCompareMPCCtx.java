package crypto.mpc.testing;

import java.util.Vector;

import crypto.mpc.compare.OSecureCompareCtx;
import crypto.mpc.interfaces.*;
import crypto.utils.shamir.ShamirSharedGen;
import crypto.utils.shamir.Shared;

public class OTestCompareMPCCtx implements 
	IContext,
	OInjectSharedsCtx.IContextOwner, 
	crypto.mpc.compare.OSecureCompareCtx.IContextOwner,
	OReconstructRequsetAckMsg.IReconstructCtx {

	
	public interface IContextOwner {
		IShamirAgent agnet();
		void compareTestOver(String contextKey);
	} 
	
	 enum zstate {
		 	INIT,
		 	PENNDING_ON_INJECT,
		    DONE,
		  }

	 
	private IContextOwner owner;
	private IShamirAgent agent;
	private String contextKey;
	private String aKey;
	private int a;
	private String bKey;
	private int b;
	private String cKey;
	private zstate state;
	 
	private int injectCounter;
	private int requestCounter;
	private Vector<Shared> collected;
	
	public OTestCompareMPCCtx (
			String contextKey,
			IContextOwner owner,
			IShamirAgent agent, 
			String aKey, int a,
			String bKey, int b,
			String cKey) {
		this.contextKey = contextKey;
		this.owner = owner;
		this.agent = agent;
		this.aKey = aKey;
		this.a = a;
		this.bKey = bKey;
		this.b = b;
		this.cKey = cKey;
		this.state = zstate.INIT;
		
	}
	
	public String contextKey() {
		return contextKey;
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
	
	private void init() {
		// create a context for injecting a				
		injectCounter = 0;
		
		OInjectSharedsCtx injectA = new OInjectSharedsCtx(
				String.format("%s-%s", contextKey, aKey),  // context name
				aKey, a, // shared
				this);
		this.agent.storeContext(injectA.contextKey(), injectA);
		injectA.action();
		injectCounter++;
		
		// create a context for injecting b
		OInjectSharedsCtx injectB = new OInjectSharedsCtx(
				String.format("%s-%s", contextKey, bKey),  // context name
				bKey, b, // shared
				this);
		this.agent.storeContext(injectB.contextKey(), injectB);
		injectB.action();
		injectCounter++;
		
		this.state = zstate.PENNDING_ON_INJECT;
		collected = new Vector<Shared>();
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
		// Now we are ready for the do the compare, let's create a context for it
		OSecureCompareCtx ctx = new OSecureCompareCtx(
				String.format("cmp-%s-%s", contextKey, cKey),
				aKey, bKey, cKey,
				this);
		this.agent.storeContext(ctx.contextKey(), ctx);
		ctx.init();		
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
		boolean res = (reconstruct != 0);
		boolean realRes = this.a < this.b;
		if (res != realRes) {
			//Global.log.logln(true, String.format("ERROR!!!! %d %d result is %d", a,b, reconstruct));
			Shared z = agent.shared("ddddd");
			z.index();
		}	
		owner.compareTestOver(contextKey);
	}

	public void compareDone(String key) {
		OReconstructRequsetMsg reconstructMsg = new OReconstructRequsetMsg(this.contextKey, cKey);
		requestCounter = agent.networkSize();
		agent.BroadcastMsg(reconstructMsg);				
	}
}
