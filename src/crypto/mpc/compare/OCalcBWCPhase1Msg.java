package crypto.mpc.compare;

import crypto.mpc.interfaces.*;
import crypto.utils.shamir.Shared;

public class OCalcBWCPhase1Msg implements IMessage{
	
	
	private String contextKey;
	private long a;
	private String bKey;
	private int index;
	
	public OCalcBWCPhase1Msg(String contextKey, long a, String bKey, int index) {
		this.contextKey = contextKey;
		this.a = a;
		this.bKey = bKey;
		this.index = index;
		
	}

	@Override
	public void action(IShamirAgent agent, int senderID) {
		long prime = agent.prime();
		
		int aIndex = (int) ((this.a >>this.index) & 1); // Extract the i-th bit of a
		Shared bIndex = agent.shared(bitKey(bKey, index));
		Shared cIndex;
						
		if (aIndex == 0) {
			cIndex = bIndex;
		} else {
			cIndex = new Shared(agent.agentID(), 1, 1).sub(bIndex, prime);
		}
		String cIndexKey = bitKey(contextKey+"-c", index);
		agent.storeShared(cIndexKey, cIndex);
		
		if (isFirstBit()) {
			String dIndexKey = bitKey(contextKey+"-d", index);
			agent.storeShared(dIndexKey, cIndex);
		}
		
		OCalcBWCPhase1AckMsg ack = new OCalcBWCPhase1AckMsg(contextKey);
		agent.SendMsg(senderID, ack);		
	}
		
	static private String bitKey(String key, int index) {
		return String.format("%s(%d)", key, index);
	}

	private boolean isFirstBit() {
		return (index == 30);
	}

	
	public IMessage clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
