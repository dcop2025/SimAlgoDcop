package crypto.mpc.testing;

import java.util.Random;

import crypto.mpc.interfaces.*;

import crypto.utils.shamir.ShamirSharedGen;
import crypto.utils.shamir.Shared;
import sinalgo.runtime.Global;

public class OInjectSharedsCtx implements IContext {
		
	public interface IContextOwner {
		IShamirAgent agnet();
		void injectSharedsDone(String key);
	}
	
	 enum zstate {
		 	INIT,
		 	ACK_COLLECT,
		  }

	
	private IContextOwner owner;
	private IShamirAgent agent;
	private String contextKey;
	private zstate state;
	
	private String key;
	private int value;

	private Random random = new Random();
	private int ackCounter;

	
	
	public OInjectSharedsCtx(String contextKey, String key, int value, IContextOwner owner) {
		this.owner = owner;
		this.agent = owner.agnet();
		this.contextKey = contextKey;
		this.key = key;
		this.value = value;
		this.state = zstate.INIT;
	}
	
	public String contextKey() {
		return contextKey;
	}
	
	
	public void action () {
		switch (this.state) {
	    case INIT:
	    	actionInit();	    	
	        break;
	    case ACK_COLLECT:
	    	actionCollect();
	    	break;
	    default:
	    	noAction();
		}
	}
	
	private void actionInit() {
		Global.log.logln(true, "ID:" + agent.agentID() + " " + String.format("init for ctx %s key %s value %s" , contextKey, key, value));
		this.ackCounter = 0;
		long prime = agent.prime();
		int shamirThreshold = agent.shamirThreshold();
		
		

		Shared[] shared = ShamirSharedGen.generate(value, shamirThreshold, agent.networkSize(), prime, random);
		int i = 1;
		for (int targetID : agent.ids()) {
			InjectSecretMsg msg = new InjectSecretMsg(this.contextKey, key, shared[i]);
			
			this.agent.SendMsg(targetID, msg);
			i++;
			this.ackCounter++;		    
		}
/*		
		Vector<IDcopAgent> neighbors = this.agent.Neighborhood();
		Shared[] shared = ShamirSharedGen.generate(value, shamirThreshold, neighbors.size()+1, prime, random);

		// for each neighbor send a shared
		int i = 1;
		for (IDcopAgent other : neighbors) {			
			InjectSecretMsg msg = new InjectSecretMsg(this.contextKey, key, shared[i]);
			
			this.agent.SendMsg(other.agentID(), msg);
			i++;
			this.ackCounter++;
		}
	*/	
		// Also send a shared to yourself		
		InjectSecretMsg msg = new InjectSecretMsg(this.contextKey, key, shared[0]);
		this.agent.SendMsg(this.agent.agentID(), msg);
		this.ackCounter++;
		this.state = zstate.ACK_COLLECT;
	}
	
	public void actionCollect() {
		this.ackCounter--;
		if (ackCounter != 0) {
			return;
		}
		this.owner.injectSharedsDone(contextKey);
	}
	
	
	private void noAction() {
		
	}
}
