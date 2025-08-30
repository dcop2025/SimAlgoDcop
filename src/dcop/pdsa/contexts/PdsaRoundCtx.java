package dcop.pdsa.contexts;

import java.util.Arrays;

import crypto.mpc.add.OSecureAddCtx;
import crypto.mpc.compare.OSecureCompareCtx;
import crypto.mpc.interfaces.IContext;
import crypto.mpc.interfaces.IShamirAgent;
import crypto.mpc.multiply.OSecureMultiplyCtx;
import crypto.mpc.reconstruct.OReconstructCtx;
import crypto.mpc.sub.OSecureKnownSubCtx;
import crypto.mpc.sub.OSecureSubCtx;
import crypto.utils.shamir.Shared;
import crypto.utils.shamir.VectorGenerater;
import dcop.pdsa.OKeyNamer;
import dcop.pdsa.contexts.interfaces.IPdsaAgent;
import dcop.pdsa.contexts.messages.ConstrainsRowSharingMsg;

public class PdsaRoundCtx implements 
			IContext, 
			crypto.mpc.compare.OSecureCompareCtx.IContextOwner,
			crypto.mpc.multiply.OSecureMultiplyCtx.IContextOwner,
			crypto.mpc.sub.OSecureSubCtx.IContextOwner,
			crypto.mpc.sub.OSecureKnownSubCtx.IContextOwner,
			crypto.mpc.add.OSecureAddCtx.IContextOwner,
			crypto.mpc.reconstruct.OReconstructCtx.IContextOwner {
	
//	static final String beta_key = "beta-";
//	static final String gamma_key = "gamma-";
//	static final String delta_key = "delta-";

	public interface IContextOwner {
		IPdsaAgent pdsaAgnet();
		void roundDone(String key);
		void setIndex(int newX);
	}

	private IContextOwner owner;
	private IPdsaAgent me;
	private String contextKey;
	private int round;
	private int waiters;
	private int u;
	private boolean debug;
	
	public PdsaRoundCtx(String contextKey, int round, IContextOwner owner) {
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
	
	public void startNewRound() {
		// Clean seq + storage
		// Check last round
		sendSelectedVectorToEveryone();
	}
	
	public void sendSelectedVectorToEveryone( ) {
		//sendSelectedVectorToTarget(agent.agentID());
		for (int targetID : me.ids()) {
			sendSelectedVectorToTarget(targetID);
		}
		
	}
	
	public void sendSelectedVectorToTarget(int targetID) {
		me.debug(debug, "sending shareds on taget %d", targetID);

		int xIndex = me.xIndex();
		int[][] constraints = me.constraints(targetID);
		if (constraints == null) {
			// set vector to zero
		}
		int[] vector = Arrays.copyOf(constraints[xIndex], constraints[xIndex].length);
		waiters = 0;
		// Create a secret generator
		VectorGenerater vGen = new VectorGenerater(vector, me.shamirThreshold(), me.prime(), me.cryptoRandom());
		for (int otherID : me.ids()) {
			Shared[] shareds = vGen.generate(otherID);
			ConstrainsRowSharingMsg msg = new ConstrainsRowSharingMsg(me.agentID(), targetID, round, shareds, contextKey);
			waiters++;
			me.debug(debug, "sending shareds on taget %d to %d", targetID, otherID);
			me.SendMsg(otherID, msg);			
		}

		Shared[] shareds = vGen.generate(me.agentID());
		ConstrainsRowSharingMsg msg = new ConstrainsRowSharingMsg(me.agentID(), targetID, round, shareds, contextKey);
		waiters++;
		me.debug(debug, "sending shareds on taget %d to %d", targetID, me.agentID());
		me.SendMsg(me.agentID(), msg);
		

	}
	
	public void handleReadyToAssist(int senderId) {		
		waiters--;
		me.debug(debug, String.format("got ready to assist message from %d waiter %d", senderId, waiters));
		if (waiters != 0) {
			return;
		}
		kickStartEvaluateNewValue();		
	}	
		
	private void finishRound( ) {
		owner.roundDone(contextKey);
	}
	
	private void kickStartEvaluateNewValue() {
		boolean skipRound = (me.roundThreshold() < me.algoRandom().nextFloat());
		if (skipRound) {
			// if skipping round, simply start a new round
			me.debug(debug, "skipping round");
			finishRound();
			return;
		}
		// Starting for 1, as 0 was already set as the default min
		this.u = 1;
		evaluateIndex();
	}
	
	private int subReadyCounter = 0;
	private boolean cmpReady = false;
	
	private void evaluateIndex() {
		evaluateIndexPhase1();
	}
	private void evaluateIndexPhase1() { 		
		me.debug(debug, "Evaluating vs index: " + u);
		if (u == me.domainPower()) {
			me.debug(debug, "Done evaluating, reconstructing");
			reconstructK();
			return;
		}
		// Send a compare request msg to everyone
		//String rightKey = wb_i_key + me.agentID() + "-" + u;
		//String leftKey = wi_key + me.agentID();
		String wbKey = OKeyNamer.WbIndex(me.agentID(), u);
		String wKey = OKeyNamer.w(me.agentID());
		String betaKey = OKeyNamer.beta(me.agentID());		
		String kKey = OKeyNamer.k(me.agentID());
		
	
		// start for debug
		Shared wb = me.shared(wbKey);
		if (wb == null) {
			// Log error
			me.debug(true, "ERROR: Didn't find " + wbKey + " in agent " + me.agentID());
		} 
		Shared w = me.shared(wKey);
		if (w == null) {
			// Log error
			me.debug(true, "ERROR: Didn't find " + wbKey + " in agent " + me.agentID());
		} 

		int res = wb.real() < w.real() ? 1 : 0;

		// debug
		me.debug(debug, "broadcasting to compare for agent %d| w %d, wb %d", me.agentID(), w.real(), wb.real());
		Shared k = me.shared(kKey);
		me.debug(debug, "broadcasting to compare for agent %d| k %d, u %d", me.agentID(), k.real(), u);
		// debug
		
		cmpReady = false;
		OSecureCompareCtx compareCtx = new OSecureCompareCtx(String.format("%s-cmp-%d", contextKey, me.agentID()), wbKey, wKey, betaKey, res, this);
		me.storeContext(compareCtx.contextKey(), compareCtx);
		compareCtx.init();
		
		// in parallel calc w_i(u) - w_i
		subReadyCounter = 0;
		OSecureSubCtx subCtx = new OSecureSubCtx(String.format("%s-wsub", contextKey), wbKey, wKey, OKeyNamer.gammaSub(me.agentID()), this);
		me.storeContext(subCtx.contextKey(), subCtx);
		subCtx.action();

		// and calc in parallel we could sub for u - k
		OSecureKnownSubCtx kSubCtx = new OSecureKnownSubCtx(String.format("%s-ksub", contextKey), u, kKey, OKeyNamer.deltaSub(me.agentID()), this);
		me.storeContext(kSubCtx.contextKey(), kSubCtx);
		kSubCtx.action();		
	}


	@Override
	public IShamirAgent agnet() {
		// TODO Auto-generated method stub
		return me;
	}


	@Override
	public void compareDone(String key, int expected) {
		me.debug(debug, "compare done: %d, expected %d", u, expected);
		cmpReady = true;
		evaluateIndexPhase2();
	}

	public void subDone(String contextKey) {
		//me.debug(debug, "sub done: u %d counter %d", u, subReadyCounter);
		subReadyCounter++;
		evaluateIndexPhase2();
	}
	
	private int multiReadyCounter;
	
	public void evaluateIndexPhase2() {
		if (cmpReady == false) {
			return;
		}
		if (subReadyCounter != 2) {
			return;			
		}
		
		me.debug(debug, "starting eve phase 2 u: %d", u);
		
		multiReadyCounter = 0;
		// debug point
		String betaKey = OKeyNamer.beta(me.agentID());
		Shared beta = me.shared(betaKey);
		me.debug(debug, "beta key %s c %d", betaKey, beta.real());
		
		
		OSecureMultiplyCtx multiGamma = new OSecureMultiplyCtx(String.format("%s-gamma", contextKey), betaKey, OKeyNamer.gammaSub(me.agentID()), OKeyNamer.gamma(me.agentID()), this);
		me.storeContext(multiGamma.contextKey(), multiGamma);
		multiGamma.action();
		
		OSecureMultiplyCtx multiDelta = new OSecureMultiplyCtx(String.format("%s-delta", contextKey), betaKey, OKeyNamer.deltaSub(me.agentID()), OKeyNamer.delta(me.agentID()), this);
		me.storeContext(multiDelta.contextKey(), multiDelta);
		multiDelta.action();
	}
	
	public void multiplyDone(String key) {
		multiReadyCounter++;
		evaluateIndexPhase3();
	}
	
	private int addReadyCounter = 0;
	
	public void evaluateIndexPhase3() {
		me.debug(debug, "multi done: u %d counter %d", u, multiReadyCounter);
		if (multiReadyCounter != 2) {
			return;
		}
		multiReadyCounter = 0;		
		
		
		addReadyCounter = 0;
		//String wiKey = wi_key + "-" + me.agentID();		
		//String ki = ki_key + "-" + me.agentID();
		String wiKey = OKeyNamer.w(me.agentID());		
		String ki = OKeyNamer.k(me.agentID());
		
		// debug
		Shared gamma = me.shared(OKeyNamer.gamma(me.agentID()));
		Shared delta = me.shared(OKeyNamer.delta(me.agentID()));
		me.debug(debug, "gamma %d delta %d", gamma.real(), delta.real());
		
		// debug
		
		OSecureAddCtx wAddCtx = new OSecureAddCtx(String.format("%s-addw", contextKey), wiKey, OKeyNamer.gamma(me.agentID()), wiKey, this);
		me.storeContext(wAddCtx.contextKey(), wAddCtx);
		wAddCtx.action();
		
		OSecureAddCtx kAddCtx = new OSecureAddCtx(String.format("%s-addk", contextKey), ki, OKeyNamer.delta(me.agentID()), ki, this);
		me.storeContext(kAddCtx.contextKey(), kAddCtx);
		kAddCtx.action();
			
	}
	
	
	public void addDone(String contextKey) {
		addReadyCounter++;
		evaluateIndexPhase4();
	}
	
	private void evaluateIndexPhase4( ) {
		me.debug(debug, "add done: u %d counter %d", u, addReadyCounter);
		if (addReadyCounter != 2) {
			return;
		}
		//if (u == me.domainPower()) {
		//	reconstructK();
		//	return;
		//}
		u++; 
		evaluateIndex();
	}
	
	private void reconstructK() {
		String ki = OKeyNamer.k(me.agentID());
		OReconstructCtx kReconstructCtx = new OReconstructCtx(String.format("%s-kreconstruct", contextKey),ki, this);
		me.storeContext(kReconstructCtx.contextKey(), kReconstructCtx);
		kReconstructCtx.action();				
	}
	
	public void reconstructDone(String contextKey, long value) {
		me.debug(debug, "the new k is %d", value);
		owner.setIndex((int)value);
		finishRound();
	}
	
}