package crypto.mpc.barrier;

import crypto.mpc.interfaces.IContext;
import crypto.mpc.interfaces.IMessage;
import crypto.mpc.interfaces.IShamirAgent;
import crypto.utils.shamir.Shared;

public class OBarrierNotifyMsg  implements IMessage{
	
	private String contextKey;

	public OBarrierNotifyMsg(String contextKey) {
		this.contextKey = contextKey;
	} 
	
	@Override
	public void action(IShamirAgent agent, int senderID) {
		// TODO: P2 shmuelg find a better avoid this casting
		IContext dummy = agent.context(contextKey);
		OBarrierCtx ctx = (OBarrierCtx) dummy;
	
		ctx.collectMsg(senderID);
	}

	public IMessage clone() {
		return this;
	}
	
	/*
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}
	 */


}
