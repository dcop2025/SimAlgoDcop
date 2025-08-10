package crypto.mpc.barrier;

import crypto.mpc.interfaces.IContext;

public class OBarrierCtx implements IContext {

	public interface IContextOwner {
		void barrier(String key);
	}
	
	private IContextOwner owner;
	private String contextKey;
	private int limit;		
	private int counter;

	public OBarrierCtx(String contextKey, int limit, IContextOwner owner) {
		this.contextKey = contextKey;
		this.owner = owner;
		this.limit = limit;
	}
	
	public void collectMsg(int otherID) {
		counter++;
		if (counter != limit) {
			return;
		}
		done();
	}
		
	public void done() {
		owner.barrier(contextKey);
	}

	@Override
	public String contextKey() {
		// TODO Auto-generated method stub
		return contextKey;
	}
}
