package structure;


import java.util.HashSet;

/**
 * This class defines the structure of a single Hybrid Input Output Automata (HIOA).
 * @author Jin Woo Ro
 *
 */
public class HIOA {
	protected String name;
	protected HashSet<Location> locations = new HashSet<Location>();
	protected HashSet<Transition> transitions = new HashSet<Transition>();
	protected HashSet<Variable> I = new HashSet<Variable>();
	protected HashSet<Variable> O = new HashSet<Variable>();
	protected HashSet<Variable> X_C = new HashSet<Variable>();
	protected HashSet<Variable> X_D = new HashSet<Variable>(); 
	protected HashSet<Variable> X_DOT = new HashSet<Variable>();
	protected Location initialLocation;
	protected HashSet<Formula> initialization = new HashSet<Formula>();
	
	/**
	 * Constructor
	 * @param name Name of this HIOA.
	 */
	public HIOA(String name) {
		this.name = name;
	}
	
	/**
	 * 
	 * @return Returns the name of this HIOA.
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return Returns the set of locations.
	 */
	public HashSet<Location> getLocations() {
		return locations;
	}

	/**
	 * 
	 * @return Returns the set of transitions.
	 */
	public HashSet<Transition> getTransitions() {
		return transitions;
	}
	
	/**
	 * @return Returns the input variables.
	 */
	public HashSet<Variable> getInputs(){
		return I;
	}

	/**
	 * @return Returns the number of input variables.
	 */
	public int getInputVarCount() {
		return I.size();
	}
	
	/**
	 * @return Returns the number of output variables.
	 */
	public int getOutputVarCount() {
		return O.size();
	}
	
	/**
	 * @return Returns the number of continuous variables.
	 */
	public int getContVarCount() {
		return X_C.size();
	}
	
	/**
	 * @return Returns the number of discrete variables.
	 */
	public int getDiscVarCount() {
		return X_D.size();
	}
	
	/**
	 * @return Returns the output variables.
	 */
	public HashSet<Variable> getOutputs(){
		return O;
	}
	
	/**
	 * 
	 * @return Returns the set of continuous variables.
	 */
	public HashSet<Variable> getContinuousVariables() {
		return X_C;
	}
	
	/**
	 * 
	 * @return Returns the set of discrete local variables.
	 */
	public HashSet<Variable> getDiscreteVariables(){
		return X_D;
	}

	/**
	 * 
	 * @return Returns the initial location.
	 */
	public Location getInitialLocation() {
		return initialLocation;
	}

	/**
	 * 
	 * @return Returns the set of equations for the initialization of the variables.
	 */
	public HashSet<Formula> getInitialization() {
		return initialization;
	}
	
	/**
	 * Sets the initial location.
	 * @param l The initial location.
	 */
	public void setInitialLocation(Location l) {
		initialLocation = l;
	}
		
	/**
	 * Adds an equation to the variable initialization.
	 * @param equation A formula.
	 */
	public void addInitialization(Formula equation) {
		initialization.add(equation);
	}

	/**
	 * Adds an input variable.
	 * @param name Variable name.
	 */
	public void addInputVariable(String name) {
		Variable v = new Variable(name);
		I.add(v);
	}
	
	/**
	 * Adds an output variable.
	 * @param name Variable name.
	 * @param initialValue Initial value.
	 */
	public void addOutputVariable(String name, double initialValue) {
		Variable v = new Variable(name);
		v.setInitialValue(initialValue);
		O.add(v);
	}
	
	/**
	 * Adds a continuous variable.
	 * @param name Variable name.
	 * @param initialValue Initial value.
	 */
	public void addContinuousVariable(String name, double initialValue) {
		Variable v = new Variable (name);
		v.setInitialValue(initialValue);
		X_C.add(v);
		
		// Every continuous variable creates a derivative variable
		String derivative = name + "_dot";
		Variable v_dot = new Variable (derivative);
		X_DOT.add(v_dot);
	}
	
	/**
	 * Adds a discrete variable.
	 * @param name Variable name.
	 * @param initialValue Initial value.
	 * @param type Type of this variable.
	 */
	public void addDiscreteVariable(String name, double initialValue) {
		Variable v = new Variable (name);
		v.setInitialValue(initialValue);
		X_D.add(v);
	}
	
	/**
	 * Adds a location.
	 * @param loc A location object.
	 */
	public void addLocation(Location loc) {
		locations.add(loc);
	}
	
	/**
	 * Adds a edge transition.
	 * @param tran A transition object.
	 */
	public void addTransition(Transition tran) {
		transitions.add(tran);
	}
	
	/**
	 * 
	 * @param id id of the location
	 * @return Returns a location based on the id. Returns null if no location has the requested id value.
	 */
	public Location getLocation(int id) {
		for (Location l : locations) {
			if (l.getID() == id) {
				return l;
			}
		}
		return null;
	}
	
	/**
	 *
	 * @param name Name of the location.
	 * @return Returns the location based on the name. Returns null if no location has the requested name.
	 */
	public Location getLocation(String name) {
		for (Location l : locations) {
			if (l.getName().equals(name)){
				return l;
			}
		}
		return null;
	}
	
	/**
	 * A helper function to search a variable using the name. 
	 * @param name Name of the variable.
	 * @return Returns the variable. Otherwise, returns null if the search fails.
	 */
	public int hasVariable(String name) {
		for (Variable var : X_C) {
			if (var.getName().equals(name)) {
				return 1;
			}
		}
		for (Variable var : X_D) {
			if (var.getName().equals(name)) {
				return 2;
			}
		}
		for (Variable var : X_DOT) {
			if (var.getName().equals(name)) {
				return 3;
			}
		}
		for (Variable var : I) {
			if (var.getName().equals(name)) {
				return 4;
			}
		}
		for (Variable var : O) {
			if (var.getName().equals(name)) {
				return 5;
			}
		}
		
		// return zero means this variable does not exist
		return 0;
	}

	@Override
	public String toString() {
		return "HIOA [name=" + name + "]";
	}


	
}
