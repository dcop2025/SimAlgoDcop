package dcop.pdsa.contexts.messages;

import java.lang.annotation.Target;
import java.util.Arrays;

import crypto.mpc.compare.OCalcBWCPhase1AckMsg;
import crypto.mpc.interfaces.IMessage;
import crypto.mpc.interfaces.IShamirAgent;
import crypto.utils.shamir.Shared;
import dcop.pdsa.contexts.interfaces.IPdsaAgent;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class ConstrainsRowSharingMsg implements IMessage {

	static final String wb_i_key = "wb";
	static final String ki_key = "k-";
	static final String wi_key = "w-";
	
	private int agentA;
	private int agentB;
	private int round;
	private Shared[] shareds;
	private String contextKey;
	
	public ConstrainsRowSharingMsg(int aID, int bID, int round, Shared[] shareds, String contextKey) {
		agentA = aID;
		agentB = bID;
		this.round = round;
		this.shareds = Arrays.copyOf(shareds, shareds.length);
		this.contextKey = contextKey;
	}

	
	public void action(IShamirAgent agent, int senderID) {
		
	}

	public void action(IPdsaAgent me, int senderID) {
		//debug(debug, "Got sherad vector on " + msg.agentB + " from " + msg.agentA);
		
		// Storing shared under the following key wb - agent id - index 
		String baseKey = String.format("%s-%d-", wb_i_key, agentB);
		for (int i = 0; i < shareds.length; i++) {
			String key = baseKey + i;
			Shared shared = me.shared(key);
			if (shared == null) {
				shared = shareds[i];
			} else {
				shared = shared.add(shareds[i], me.prime());				
			}
			//debug(debug, "adding key " + key + " value: " + shared.toString());
			me.storeShared(key, shared);
		}
		boolean done = me.ticker(baseKey, me.networkSize());
		if (!done) {
			return;
		}

		// Once this agent has all shares for agent "b" it can notify agent b, that he is ready
		notifyReadiness(me, agentB);
		
	}
	
	private void notifyReadiness(IPdsaAgent me, int targetId) {
		me.debug(false, String.format("Telling agent %d  I'm ready to assist him", targetId));
		String baseKey = wb_i_key + targetId + "-";
		
		// Pre load k_i and w_i with the value of the first entry
		// k_i = 0
		me.storeShared(ki_key + targetId, new Shared(me.agentID(), 0, 0));
		// wi = wb_i(0)
		Shared w_i = me.shared(baseKey + "0");
		if (w_i == null) {
			// Log error
			me.debug(true, "ERROR: Didn't find wi(0) for Agent " + targetId  + " in agent " + me.agentID());
		}
		me.storeShared(wi_key + targetId, w_i);
		

		// Once this agent has all shares for agent "b" it can notify agent b, that he is ready
		me.SendMsg(targetId, new ReadyToEvaluateMsg(contextKey));		
	}

	
	public IMessage clone() {
		// TODO Auto-generated method stub
		return this;
	}

}