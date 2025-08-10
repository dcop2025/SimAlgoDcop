package crypto.mpc.reconstruct;

import java.util.Vector;

import crypto.mpc.interfaces.*;
import crypto.utils.shamir.ShamirSharedGen;
import crypto.utils.shamir.Shared;

public class OReconstructCtx implements 
	IContext,
	OReconstructRequsetAckMsg.IReconstructCtx {

	
	public interface IContextOwner {
		IShamirAgent agnet();
		void reconstructDone(String contextKey, long value);
	} 
	
	 
	private IContextOwner owner;
	private IShamirAgent me;
	private String contextKey;
	private String key;
	 
	private int reconstructCounter;
	private Vector<Shared> collected;
	
	public OReconstructCtx (
			String contextKey, 
			String key,
			IContextOwner owner) {
		this.contextKey = contextKey;
		this.owner = owner;		
		this.me = owner.agnet();
		this.key = key;		
	}
	
	public String contextKey() {
		return contextKey;
	}

	public void action() {
		init();
	}
	
	private void init() {
		collected = new Vector<Shared>();
		OReconstructRequsetMsg reconstructMsg = new OReconstructRequsetMsg(this.contextKey, key);
		reconstructCounter = me.networkSize();
		me.BroadcastMsg(reconstructMsg);				

	}

	public IShamirAgent agnet() {
		return this.me;
	}
	
	public void collectReconstruct(String key, Shared shared) {
		reconstructCounter--;
		collected.add(shared);
		if (reconstructCounter != 0) {
			return;
		}
		long prime = me.prime();
		
		long reconstruct  = ShamirSharedGen.reconstruct(collected, prime);
		owner.reconstructDone(contextKey, reconstruct);
	}

}
