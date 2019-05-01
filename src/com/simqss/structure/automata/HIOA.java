package com.simqss.structure.automata;

import java.util.HashMap;
import java.util.HashSet;

import com.simqss.structure.basic.Formula;
import com.simqss.structure.basic.Location;
import com.simqss.structure.basic.Transition;
import com.simqss.structure.basic.Variable;
import com.simqss.structure.enums.variableParam;

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
		v.setScope(variableParam.Scope.INPUT_VARIABLE);
		I.add(v);
	}
	
	/**
	 * Adds an output variable.
	 * @param name Variable name.
	 * @param initialValue Initial value.
	 */
	public void addOutputVariable(String name, double initialValue) {
		Variable v = new Variable(name);
		v.setScope(variableParam.Scope.OUTPUT_VARIABLE);
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
		v.setType(variableParam.Type.DOUBLE);
		v.setScope(variableParam.Scope.LOCAL_CONTINUOUS);
		v.setInitialValue(initialValue);
		X_C.add(v);
		
		// Every continuous variable creates a derivative variable
		String derivative = name + "_dot";
		Variable v_dot = new Variable (derivative);
		v_dot.setType(variableParam.Type.DOUBLE);
		v_dot.setScope(variableParam.Scope.LOCAL_CONTINUOUS_DERIVATIVE);
		X_DOT.add(v_dot);
	}
	
	/**
	 * Adds a discrete variable.
	 * @param name Variable name.
	 * @param initialValue Initial value.
	 * @param type Type of this variable.
	 */
	public void addDiscreteVariable(String name, double initialValue, variableParam.Type type) {
		Variable v = new Variable (name);
		v.setType(type);
		v.setScope(variableParam.Scope.LOCAL_DISCRETE);
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
	public Variable hasVariable(String name) {
		HashSet<Variable> V = new HashSet<Variable>();
		V.addAll(X_C);
		V.addAll(X_D);
		V.addAll(X_DOT);
		V.addAll(I);
		V.addAll(O);
		for (Variable var : V) {
			if (var.getName().equals(name)) {
				return var;
			}
		}
		return null;
	}
	
}
