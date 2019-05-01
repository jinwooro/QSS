package com.pretzel.structure.automata;

import java.util.HashMap;
import java.util.HashSet;

import com.pretzel.structure.basic.Location;
import com.pretzel.structure.basic.Transition;
import com.pretzel.structure.basic.Variable;
import com.pretzel.structure.enums.variableParam;

/**
 * This class defines the structure of Hybrid Input Output Automata (HIOA).
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
	protected Transition initialTransition;
	
	public HIOA(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public HashSet<Location> getLocations() {
		return locations;
	}

	public HashSet<Transition> getTransitions() {
		return transitions;
	}

	public HashSet<Variable> getContinuousVariables() {
		return X_C;
	}
	
	public HashSet<Variable> getDiscreteVariables(){
		return X_D;
	}

	public Location getInitialLocation() {
		return initialLocation;
	}

	public Transition getInitialization() {
		return initialTransition;
	}
	
	public void setInitialLocation(Location l) {
		initialLocation = l;
	}
	
	// initialization is represented as a transition (arrow) which has no source node
	public void setInitialization(Transition t) {
		initialTransition = t;
	}

	public void makeEmptyInitialization(String initLocation) {
		initialLocation = getLocation(initLocation);
		initialTransition = new Transition(null, initialLocation);
	}
	
	public void addInputVariable(String name) {
		Variable v = new Variable(name);
		v.setScope(variableParam.Scope.INPUT_VARIABLE);
		I.add(v);
	}
	
	public void addOutputVariable(String name, double initialValue) {
		Variable v = new Variable(name);
		v.setScope(variableParam.Scope.OUTPUT_VARIABLE);
		v.setInitialValue(initialValue);
		O.add(v);
	}
	
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
	
	public void addDiscreteVariable(String name, double initialValue, variableParam.Type type) {
		Variable v = new Variable (name);
		v.setType(type);
		v.setScope(variableParam.Scope.LOCAL_DISCRETE);
		v.setInitialValue(initialValue);
		X_D.add(v);
	}
	
	public void addLocation(Location l) {
		locations.add(l);
	}
	
	public void addTransition(Transition t) {
		transitions.add(t);
	}
	
	public Location getLocation(int id) {
		for (Location l : locations) {
			if (l.getID() == id) {
				return l;
			}
		}
		return null;
	}
	
	public Location getLocation(String name) {
		for (Location l : locations) {
			if (l.getName().equals(name)){
				return l;
			}
		}
		return null;
	}
	
	// Helper functions
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
