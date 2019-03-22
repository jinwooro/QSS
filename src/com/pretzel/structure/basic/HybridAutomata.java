package com.pretzel.structure.basic;

import java.util.HashSet;
import com.pretzel.structure.enums.variableParam;

public class HybridAutomata {
	protected String name;
	protected HashSet<Location> locations = new HashSet<Location>();
	protected HashSet<Transition> transitions = new HashSet<Transition>();
	protected HashSet<Variable> X_C = new HashSet<Variable>();
	protected HashSet<Variable> X_D = new HashSet<Variable>(); 
	protected HashSet<Variable> X_DOT = new HashSet<Variable>();
	protected Location initialLocation;
	protected Transition initialTransition;
	
	public HybridAutomata(String name) {
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
	
	public void addContinuousVariable(String name, double initialValue) {
		Variable v = new Variable (name);
		v.setType(variableParam.Type.DOUBLE);
		v.setScope(variableParam.Scope.LOCAL_CONTINUOUS);
		v.setInitialValue(initialValue);
		X_C.add(v);
		
		// Every continuous variable has its derivative as a separate variable
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
	
	public Variable hasVariable(String name) {
		HashSet<Variable> X = new HashSet<Variable>();
		X.addAll(X_C);
		X.addAll(X_D);
		X.addAll(X_DOT);
		for (Variable var : X) {
			if (var.getName().equals(name)) {
				return var;
			}
		}
		return null;
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
	
}
