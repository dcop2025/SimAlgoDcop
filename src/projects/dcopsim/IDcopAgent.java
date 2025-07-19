package projects.dcopsim;

import dcop.Problem.ConstraintsMatrix;

public interface IDcopAgent {
	// Set the trigger, so start running the algo
	public void trigger();
	
	// Install a constraints matrix 
	public void installConstraintsMatrix(ConstraintsMatrix Constraints);
	
	public void logState();

	public int assignment();
	
	public int getInternal(String key);
	
	public void connect(IDcopAgent other);
	
	public boolean doneStatus();
	
	public int agentID();
	
	public void triggerCommand(String command);
}
