package pmgm.contexts.findmin;

import crypto.mpc.add.OSecureAddCtx;
import crypto.mpc.compare.OSecureCompareCtx;
import crypto.mpc.interfaces.IContext;
import crypto.mpc.interfaces.IContextMgr;
import crypto.mpc.interfaces.IShamirAgent;
import crypto.mpc.multiply.OSecureMultiplyCtx;
import crypto.mpc.sub.OSecureKnownSubCtx;
import crypto.mpc.sub.OSecureSubCtx;
import crypto.utils.shamir.Generater;
import crypto.utils.shamir.Shared;
import dcop.pdsa.contexts.messages.ConstrainsRowSharingMsg;
import pmgm.interfaces.IDcoper;
import pmgm.interfaces.IDebug;
import pmgm.interfaces.INetworkAgent;
import pmgm.interfaces.IShamir;
import pmgm.contexts.messages.*;
import pmgm.contexts.messages.CopySharedMsg.*;
import pmgm.contexts.messages.InjectSharedMsg.*;


// FindMinCtx takes a array of shareds as input and generates 2 secrets
//   1. the value of the min
//   2. the index of the min
public class FindMinCtx implements IContext,
		ICopySharedCtxOwner,
		IInjectSharedCtxOwner,
		crypto.mpc.compare.OSecureCompareCtx.IContextOwner,
		crypto.mpc.sub.OSecureSubCtx.IContextOwner,
		crypto.mpc.sub.OSecureKnownSubCtx.IContextOwner,
		crypto.mpc.multiply.OSecureMultiplyCtx.IContextOwner,
		crypto.mpc.add.OSecureAddCtx.IContextOwner {

	// the context owner should provided agent services and a callback function when the output is ready
	public interface IContextOwner {
		void findMinDone(String key);
		IDebug Debugger();
		IDcoper Dcoper();		
		INetworkAgent Networker();
		IShamir shamir();
		IContextMgr ContextMgr();
	}
	
	public FindMinCtx(
			String contextKey,
			String baseKey,
			int firstIndex,
			int lastIndex,
			String outputIndexKey,
			String outputValueKey,
			IContextOwner owner) {
		this.contextKey = contextKey;
		this.baseKey = baseKey;
		this.currIndex = firstIndex;
		this.lastIndex = lastIndex;
		this.outputIndexKey = outputIndexKey;
		this.outputValueKey = outputValueKey;
		this.owner = owner;

		debug = owner.Debugger();
		dcoper = owner.Dcoper();
		network = owner.Networker();
		shamir = owner.shamir();
		contextMgr = owner.ContextMgr();
	}
	
	// context
	private String contextKey;
	public String contextKey() {
		return contextKey;
	}

	public void startContxt() {
		prep();
	}
	
	// debug
	private IDebug debug;
	private boolean bDebug;

	// network
	private INetworkAgent network;
	
	// dcop
	private IDcoper dcoper;

	// shamir
	private IShamir shamir;
		
	// context mgr
	private IContextMgr contextMgr;
	
	// core
	private IContextOwner owner;
	private String baseKey;
	private String outputIndexKey;
	private String outputValueKey;	
	private int lastIndex;	
	private int currIndex;

	private int copyWaiters;
	private int injectWaiters;
	private boolean compareDone;
	private int subCounter;
	private int multiCounter;
	private int addCounter;
	
		
	private void prep() {
		// Starting with some prep work,
				
			 
		// 2. Set the output value as the first entry, by boardcasting a message to copy the first entry as the output
		CopySharedMsg copyMsg = new CopySharedMsg(contextKey, outputValueKey, currValueKey());
		copyWaiters = network.networkSize();
		network.BroadcastMsg(copyMsg);
		
		// 3.Set the shared secret with the index (1)
		injectWaiters = 0;
		Generater indexSharedGen = new Generater(currIndex, shamir.threshold(), shamir.prime(), shamir.cryptoRandom());
		for (int otherID : network.connections()) {
			Shared shared = indexSharedGen.generate(otherID);
			InjectSharedMsg injectMsg = new InjectSharedMsg(contextKey, outputIndexKey, shared);
			injectWaiters++;
			network.SendMsg(otherID, injectMsg);						
		}
		Shared shared = indexSharedGen.generate(dcoper.agentID());
		InjectSharedMsg injectMsg = new InjectSharedMsg(contextKey, outputIndexKey, shared);
		injectWaiters++;
		network.SendMsg(dcoper.agentID(), injectMsg);								
	}
	
	@Override
	public void InjectSharedAck(int agentID) {
		injectWaiters--;
		checkPrepDone();
	}

	@Override
	public void CopySharedAck(int agentID) {
		copyWaiters--;
		checkPrepDone();		
	}

	private void checkPrepDone() {
		if (injectWaiters == 0) {
			return;
		}
		
		if (copyWaiters == 0) {
			return;
		}
		
		// Prep work done
		startIteration();
	}
	
	
	
	private void startIteration() {
		// if reach the last index (including the last index) done looping
		if (currIndex == lastIndex) {
			doneLooping();
		}
		// inc the iterator to the next index
		currIndex++;
		
		// 3 things that needs to be done
		//   1. compare the current value (by index) vs the current min value
		//   2. calc array[index] - curr min
		//   3. calc index - curr min index
		
		String betaKey = betaKey();
		String currValKey = currValueKey();
				

		compareDone = false;
		OSecureCompareCtx compareCtx = new OSecureCompareCtx(String.format("%s-cmp-%d", contextKey, dcoper.agentID()), currValKey, outputValueKey, betaKey, -1, this);
		contextMgr.storeContext(compareCtx.contextKey(), compareCtx);
		compareCtx.init();
		

		subCounter = 2;
		OSecureSubCtx subValueCtx = new OSecureSubCtx(String.format("%s-value-sub", contextKey), currValKey, outputValueKey, subGammaKey(), this);
		contextMgr.storeContext(subValueCtx.contextKey(), subValueCtx);
		subValueCtx.action();

		OSecureKnownSubCtx subIndexCtx = new OSecureKnownSubCtx(String.format("%s-index-sub", contextKey), currIndex, outputIndexKey, subDeltaKey(), this);
		contextMgr.storeContext(subIndexCtx.contextKey(), subIndexCtx);
		subIndexCtx.action();		

	}
	
	private void doneLooping( ) {
		// TODO do this
		owner.findMinDone(contextKey);
	}

	@Override
	public IShamirAgent agnet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void compareDone(String key, int expected) {
		// TODO Auto-generated method stub
		compareDone = true;
		startPhase2();
	}
	
	public void subDone(String key) {
		subCounter--;
		startPhase2();
	}
	
	private void startPhase2() {
		if (compareDone == false) {
			return;
		}
		
		if (subCounter != 0) {
			return;	
		}
		
		// multiply beta with the subs
		
		String betaKey = betaKey();
				
		multiCounter = 2;
		OSecureMultiplyCtx multiGamma = new OSecureMultiplyCtx(String.format("%s-gamma", contextKey), betaKey, subGammaKey(), gammaKey(), this);
		contextMgr.storeContext(multiGamma.contextKey(), multiGamma);
		multiGamma.action();
		
		OSecureMultiplyCtx multiDelta = new OSecureMultiplyCtx(String.format("%s-delta", contextKey), betaKey, subDeltaKey(), deltaKey(), this);
		contextMgr.storeContext(multiDelta.contextKey(), multiDelta);
		multiDelta.action();

	}

	@Override
	public void multiplyDone(String key) {
		multiCounter--;
		startPhase3();
	}
	
	private void startPhase3() {
		if (multiCounter != 0) {
			return;
		}
		
		// wbKey == currValKey
		// wKey = outputValueKey

		
		// debug
/*
		Shared gamma = dcoper.shared(OKeyNamer.gamma(dcoper.agentID()));
		Shared delta = dcoper.shared(OKeyNamer.delta(dcoper.agentID()));
		debug.debug(bDebug, "gamma %d delta %d", gamma.real(), delta.real());
*/
		addCounter = 2;
		OSecureAddCtx addValueCtx = new OSecureAddCtx(String.format("%s-add-value", contextKey), outputValueKey, gammaKey(), outputValueKey, this);
		contextMgr.storeContext(addValueCtx.contextKey(), addValueCtx);
		addValueCtx.action();
		
		OSecureAddCtx kAddCtx = new OSecureAddCtx(String.format("%s-add-index", contextKey), outputIndexKey, deltaKey(), outputIndexKey, this);
		contextMgr.storeContext(kAddCtx.contextKey(), kAddCtx);
		kAddCtx.action();

	}

	@Override
	public void addDone(String key) {
		// TODO Auto-generated method stub
		addCounter--;
		loopDone();		
	}
	
	private void loopDone() {
		if (addCounter != 0) {
			return;
		}
		startIteration();		
	}

	// Generate keys for different shared
	
	private String betaKey() {
		return String.format("%s-beta-%d", contextKey, dcoper.agentID());
	}
	
	private String subGammaKey() {
		return String.format("%s-sub-gamma-%d", contextKey, dcoper.agentID());
	}
	
	private String gammaKey() {
		return String.format("%s-gamma-%d", contextKey, dcoper.agentID());
	}

	private String deltaKey() {
		return String.format("%s-delta-%d", contextKey, dcoper.agentID());
	}

	private String subDeltaKey() {
		return String.format("%s-sub-delta-%d", contextKey, dcoper.agentID());
	}

	private String currValueKey() {
		return String.format("%s-%d", baseKey, currIndex);
	}

}
