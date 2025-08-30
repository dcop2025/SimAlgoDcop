package dcop.pdsa.contexts.messages;

import java.lang.annotation.Target;
import java.util.Arrays;

import crypto.mpc.compare.OCalcBWCPhase1AckMsg;
import crypto.mpc.interfaces.IMessage;
import crypto.mpc.interfaces.IShamirAgent;
import crypto.utils.shamir.Shared;
import dcop.pdsa.OKeyNamer;
import dcop.pdsa.contexts.interfaces.IPdsaAgent;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class ConstrainsRowSharingMsg implements IMessage {

//	static final String wb_i_key = "wb";
//	static final String ki_key = "k-";
//	static final String wi_key = "w-";
	
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
		raction((IPdsaAgent)agent , senderID);
	}

	public void raction(IPdsaAgent me, int senderID) {
		boolean debug = (me.agentID() == 1); 
		me.debug(debug, String.format("Got sherad vector on %d from %d [sender %d]", agentB, agentA, senderID));
		
		// Storing shared under the following key wb - agent id - index 
		String baseKey = OKeyNamer.WbBase(agentB); 
		for (int i = 0; i < shareds.length; i++) {
			String key = OKeyNamer.WbBaseWithIndex(baseKey, i);
			Shared shared = me.shared(key);
			if (shared == null) {
				shared = shareds[i];
			} else {
				shared = shared.add(shareds[i], me.prime());				
			}
			me.debug(debug, String.format("storing key %s real %d", key, shared.real()));
			me.storeShared(key, shared);
		}
		boolean done = me.ticker(baseKey, me.networkSize()-1);
		if (!done) {
			return;
		}

		// Once this agent has all shares for agent "b" it can notify agent b, that he is readyF
		notifyReadiness(me, agentB);
		
	}
	
	private void notifyReadiness(IPdsaAgent me, int targetId) {
		boolean debug = (me.agentID() == 1);
		me.debug(debug, String.format("Telling agent %d  I'm ready to assist him", targetId));
		String wbKey = OKeyNamer.WbIndex(targetId, 0);
					
		// Pre load k_i and w_i with the value of the first entry
		// k_i = 0		
		String kKey = OKeyNamer.k(targetId);
		Shared kShared = new Shared(me.agentID(), 0, 0);
		me.storeShared(kKey, kShared);
		me.debug(debug, "k is %s real %d", kKey, kShared.real());		
		// wi = wb_i(0)
		Shared w_i = me.shared(wbKey);
		if (w_i == null) {
			// Log error
			me.debug(true, String.format("ERROR: Didn't find %s for Agent %d in agent %d", w_i, targetId, me.agentID()));			
		}
		String wiKey = OKeyNamer.w(targetId);
		me.debug(debug, "wi is %s real %d", wiKey, w_i.real());
		me.storeShared(wiKey, w_i);

		// Once this agent has all shares for agent "b" it can notify agent b, that he is ready
		me.SendMsg(targetId, new ReadyToEvaluateMsg(contextKey));		
	}

	
	public IMessage clone() {
		// TODO Auto-generated method stub
		return this;
	}

}