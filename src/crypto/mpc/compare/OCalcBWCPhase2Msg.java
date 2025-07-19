package crypto.mpc.compare;

import crypto.mpc.interfaces.*;
import crypto.utils.OModMath;
import crypto.utils.shamir.Shared;

public class OCalcBWCPhase2Msg implements IMessage{
	
	
	private String contextKey;
	private long a;
	private int index;
	
	public OCalcBWCPhase2Msg(String contextKey, long a, int index) {
		this.contextKey = contextKey;
		this.a = a;
		this.index = index;	
	}

	@Override
	public void action(IShamirAgent agent, int senderID) {
		long prime = agent.prime();
		
		int aIndex = (int) ((this.a >>this.index) & 1); // Extract the i-th bit of a
		Shared cIndex;
		Shared dIndex;
		Shared eIndex;
		Shared e;
		Shared ea;
		
		String cIndexKey = bitKey(contextKey+"-c", index);
		cIndex = agent.shared(cIndexKey);
		
		if (isFirstBit()) {
			// First bit is easy c[index] --> d[index] --> e[index] and ea = e[index]*a[index]
			dIndex = cIndex;
			eIndex = dIndex;
			e = eIndex;
			ea = eIndex.constMultiply(aIndex, prime);			
		} else {
			// and for all the rest
			Shared cdIndex = agent.shared(bitKey(contextKey+"-cd", index));
			Shared dIndexPlus1 = agent.shared(bitKey(contextKey+"-d", index+1));

			// d[index] = d[index+1] + c[index] - c[index]*d[index+1]
			dIndex = dIndexPlus1.add(cIndex, prime).sub(cdIndex, prime);
			
			// e[index] = d[index] - d[index+1]
			eIndex = dIndex.sub(dIndexPlus1, prime);
			e = agent.shared(contextKey+"-e");
			// e = sigma(e[index])
			e = e.add(eIndex, prime);
			// ea = sigma(e[index] * a[index])
			ea = agent.shared(contextKey+"-ea");
			ea = ea.add(eIndex.constMultiply(aIndex, prime), prime);			
		}
		// now store everything
		agent.storeShared(bitKey(contextKey+"-d", index), dIndex);
		agent.storeShared(bitKey(contextKey+"-e", index), eIndex);
		agent.storeShared(contextKey+"-e", e);
		agent.storeShared(contextKey+"-ea", ea);
				
		if (isLastBit()) {			
			//agent.storeShared(contextKey+"-1-ea", new Shared(agent.agentID(), 1, 1).sub(ea, prime));
			agent.storeShared(contextKey+"-1-ea", new Shared(agent.agentID(), OModMath.sub(1, ea.share(), prime) , OModMath.sub(1, ea.real(), prime)));
		}
		 
		OCalcBWCPhase2AckMsg ack = new OCalcBWCPhase2AckMsg(contextKey);
		agent.SendMsg(senderID, ack);		
	}
	
	
	private boolean isFirstBit() {
		return (index == 30);
	}
	
	private boolean isLastBit() {
		return (index == 0);
	}
	
	static private String bitKey(String key, int index) {
		return String.format("%s(%d)", key, index);
	}

	public IMessage clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
