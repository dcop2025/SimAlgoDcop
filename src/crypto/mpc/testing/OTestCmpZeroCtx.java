package crypto.mpc.testing;

import crypto.mpc.cmpzero.OCmpZeroCtx;
import crypto.mpc.cmpzero.OCmpZeroCtx.IContextOwner;
import crypto.mpc.interfaces.IContext;
import crypto.mpc.interfaces.IShamirAgent;
import crypto.mpc.reconstruct.OReconstructCtx;
import crypto.mpc.reconstruct.OReconstructRequsetAckMsg;
import crypto.utils.shamir.Shared;

public class OTestCmpZeroCtx implements 
		IContext, 
		OInjectSharedsCtx.IContextOwner, 
		crypto.mpc.cmpzero.OCmpZeroCtx.IContextOwner,
		crypto.mpc.reconstruct.OReconstructCtx.IContextOwner {

	private IContextOwner owner;
	private IShamirAgent me;
	
	private String contextKey;
	private String aKey;
	private int a;
	private String cKey;


	public interface IContextOwner {
		IShamirAgent me();
		void cmpTestOver(String contextKey, long value);
	}

	public OTestCmpZeroCtx(String contextKey, int a, IContextOwner owner) {
		this.owner = owner;
		this.me = owner.me();
		
		this.contextKey = contextKey;
		this.a = a;
		this.aKey = String.format("%s-a", contextKey);
		this.cKey = String.format("%s-c", contextKey);
	}
	
	public void action( ) {
		OInjectSharedsCtx injectA = new OInjectSharedsCtx(
				String.format("%s-%s", contextKey, aKey),  // context name
				aKey, a, // shared
				this);
		this.me.storeContext(injectA.contextKey(), injectA);
		injectA.action();

		
	}
	
	public String contextKey() {
		return contextKey;
	}


	@Override
	public void cmpZeroDone(String key) {
		OReconstructCtx rCtx = new OReconstructCtx(String.format("%s-rec", contextKey), cKey, this);
		me.storeContext(rCtx.contextKey(), rCtx);
		rCtx.action();		
		
	}

	@Override
	public IShamirAgent agnet() {
		// TODO Auto-generated method stub
		return me;
	}

	@Override
	public void injectSharedsDone(String key) {
		Shared s = me.shared(aKey);
		OCmpZeroCtx zCtx = new OCmpZeroCtx(String.format("%s-cmpz", contextKey), aKey, cKey, this);
		me.storeContext(zCtx.contextKey(), zCtx);
		zCtx.action();
	}

	@Override
	public void reconstructDone(String contextKey, long value) {
		// TODO Auto-generated method stub
		owner.cmpTestOver(contextKey, value);
		
	}
	
	


}
