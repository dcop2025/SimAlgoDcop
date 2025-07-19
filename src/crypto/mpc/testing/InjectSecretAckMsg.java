package crypto.mpc.testing;

import crypto.mpc.interfaces.*;

public class InjectSecretAckMsg implements IMessage {
	public String contextKey;
			
			
	public InjectSecretAckMsg(String contextKey) {
		this.contextKey = contextKey;
	}
			
	@Override
	public IMessage clone() {
		return this;
	}
			
	@Override
	public void action(IShamirAgent agent, int senderID) {
		// TODO shmuelg P1 fix this ugly cast to a better interface/pattern
		IContext dummy = agent.context(contextKey);
		OInjectSharedsCtx ctx = (OInjectSharedsCtx) dummy;
		ctx.actionCollect();
	}			
}
