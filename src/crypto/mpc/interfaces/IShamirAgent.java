package crypto.mpc.interfaces;

public interface IShamirAgent extends IContextMgr, IComm, ISharedStorage {
	long prime();
	int shamirThreshold();
	int agentID();
}
