package pmgm.contexts.costaggregation;

import java.util.Arrays;

import crypto.utils.shamir.Shared;
import crypto.utils.shamir.VectorGenerater;
import dcop.pdsa.contexts.messages.ConstrainsRowSharingMsg;
import pmgm.interfaces.IDebug;
import pmgm.interfaces.IDcoper;
import pmgm.interfaces.INetworkAgent;
import pmgm.interfaces.IShamir;
 
public class CostAggregationCtx {

	public CostAggregationCtx(String contextKey, IContextOwner owner) {
		this.contextKey = contextKey;
		this.owner = owner;
		debug = owner.Debugger();
		dcoper = owner.Dcoper();
		network = owner.Networker();
		shamir = owner.shamir();		
	}
	
	public interface IContextOwner {
		void costAggregationDone(String key);
		IDebug Debugger();
		IDcoper Dcoper();		
		INetworkAgent Networker();
		IShamir shamir();
	}
	
	// context
	private String contextKey;
	public String contextKey() {
		return contextKey;
	}

	public void startContxt() {		
		sendSelectedVectorToEveryone();
	}

	
	// debug
	private IDebug debug;
	private boolean bDebug;

	// network
	private INetworkAgent network;

	
	// dcop
	private IDcoper dcoper;

	// shamir
	private IShamir shamir;
	
	// core
	private IContextOwner owner;
	private int round;
	private int waiters;
	
	
	
	private void sendSelectedVectorToEveryone() {
		for (int targetID : network.connections()) {
			sendSelectedVectorToTarget(targetID);
		}
	}
	
	public void sendSelectedVectorToTarget(int targetID) {
		debug.debug(bDebug, "sending shareds on taget %d", targetID);

		int assigment = dcoper.assigment();
		
		int[][] constraints = dcoper.constraints(targetID);
		if (constraints == null) {
			// set vector to zero
		}
		
		int[] vector = Arrays.copyOf(constraints[assigment], constraints[assigment].length);
		waiters = 0;
		// Create a secret generator
		VectorGenerater vGen = new VectorGenerater(vector, shamir.threshold(), shamir.prime(), shamir.cryptoRandom());
		for (int otherID : network.connections()) {
			Shared[] shareds = vGen.generate(otherID);
			ConstrainsRowSharingMsg msg = new ConstrainsRowSharingMsg(dcoper.agentID(), targetID, round, shareds, contextKey);
			waiters++;
			debug.debug(bDebug, "sending shareds on taget %d to %d", targetID, otherID);
			network.SendMsg(otherID, msg);			
		}

		Shared[] shareds = vGen.generate(dcoper.agentID());
		ConstrainsRowSharingMsg msg = new ConstrainsRowSharingMsg(dcoper.agentID(), targetID, round, shareds, contextKey);
		waiters++;
		debug.debug(bDebug, "sending shareds on taget %d to %d", targetID, dcoper.agentID());
		network.SendMsg(dcoper.agentID(), msg);		
	}
	
	public void handleReadyToAssist(int senderId) {		
		waiters--;
		debug.debug(bDebug, String.format("got ready to assist message from %d waiter %d", senderId, waiters));
		if (waiters != 0) {
			return;
		}
		owner.costAggregationDone(contextKey);
	}		
}

