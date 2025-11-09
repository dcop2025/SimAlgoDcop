package pmgm.contexts;

import crypto.mpc.interfaces.IContext;
import dcop.pdsa.contexts.PdsaRoundCtx.IContextOwner;
import dcop.pdsa.contexts.interfaces.IPdsaAgent;

public class PmgmRountCtx implements IContext {

	
	private IContextOwner owner;
	private IPdsaAgent me;
	private String contextKey;
	private int round;
	private int waiters;
	private int u;
	private boolean debug;
	
	public PmgmRountCtx(String contextKey, int round, IContextOwner owner) {
		this.owner = owner;
		this.me = owner.pdsaAgnet();
		this.contextKey = contextKey;
		this.round = round;		
		
		debug = (me.agentID() == 1);
		
		me.debug(debug, "context key %s", this.contextKey);
	}

	
	public String contextKey() {
		return contextKey;
	}

}

