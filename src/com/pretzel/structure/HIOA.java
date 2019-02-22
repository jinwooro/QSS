package com.pretzel.structure;

import java.util.HashMap;
import java.util.HashSet;

/**
 * This class defines the structure of Hybrid Input Output Automata (HIOA).
 * @author Jin Woo Ro
 *
 */
public class HIOA {
	public enum VarType {CONTINUOUS, DISCRETE, INPUT, OUTPUT, DERIVATIVE, NONE} 
	private String name;
	private HashSet<Location> locations = new HashSet<Location>();
	private HashSet<Transition> transitions = new HashSet<Transition>();
	private HashMap<String, Double> X_C  = new HashMap<String, Double>();
	private HashSet<String> X_C_DOT  = new HashSet<String>();
	private HashMap<String, Double> X_D  = new HashMap<String, Double>();
	private HashSet<String> I  = new HashSet<String>();
	private HashMap<String, Double> O  = new HashMap<String, Double>();
	private HashMap<Integer, String> I_ports = new HashMap<Integer, String>();
	private HashMap<Integer, String> O_ports = new HashMap<Integer, String>();
	private Location init_loc;
	private Transition initialization;
	
	public HIOA(String name){
		this.name = name;
	}
	
	public String getInputNameByPort(int num) {
		// TODO:
		return "";
	}
	
	public int getInputPortByName(String name) {
		// TODO:
		return 0;
	}
	
	public String getOutputNameByPort(int num) {
		// TODO
		return "";
	}
	 
	public int getOutputPortByName(String name) {
		// TODO
		return 0;
	}
	
	public String getName() {
		return name;
	}
	
	public HashSet<Location> getLocations(){
		return locations;
	}
	
	/**
	 * Return the collection of continuous variables as a HashMap,
	 * where the key is the variable name in String 
	 * and the value is the initial value in Double.
	 * @return HashMap
	 */
	public HashMap<String,Double> getXC() {
		return X_C;
	}

	public HashMap<String,Double> getXD() {
		return X_D;
	}
	
	public HashSet<String> getI() {
		return I;
	}
	
	public HashMap<String,Double> getO() {
		return O;
	}
	
	public Location getLocationByID(int id) {
		for (Location h : locations) {
			if (h.checkByID(id) == true) {
				return h;
			}
		}
		return null;
	}

	public void addLocation(Location l) {
		locations.add(l);
	}
	
	public void addTransition(Transition t) {
		transitions.add(t);
	}
	
	public void setInitialization(Transition t) {
		initialization = t;
	}
	
	public Transition getInitialTrnasition() {
		return initialization;
	}
	
	public VarType hasVariable(String name) {
		if (X_C.containsKey(name)) 
			return VarType.CONTINUOUS;
		else if (X_D.containsKey(name))
			return VarType.DISCRETE;
		else if (O.containsKey(name))
			return VarType.OUTPUT;
		else if (I.contains(name))
			return VarType.INPUT;
		else if (X_C_DOT.contains(name))
			return VarType.DERIVATIVE;
		else
			return VarType.NONE;
	}
	
	public void addVariable(String name, double value, VarType type, int port) {
		switch (type) {
			case CONTINUOUS:
				X_C.put(name, value);
				X_C_DOT.add(name+"_dot"); // automatically adds the derivative form
				break;
			case DISCRETE:
				X_D.put(name, value);
				break;
			case INPUT:
				I.add(name);
				break;
			case OUTPUT:
				O.put(name, value);
				break;
			default:
				System.out.println("Error: variable " + name + " cannot be added");
				break;
		}
	}
	
	public void setInitialLocation(Location l) {
		init_loc = l;
	}
	
	public HashSet<Transition> getTransitionsBySrc(int src_loc_id){
		HashSet<Transition> transitions_subset = new HashSet<Transition>();
		for (Transition t : transitions) {
			if (t.getSrcId() == src_loc_id) {
				transitions_subset.add(t);
			}
		}
		return transitions_subset;
	}
		
	public Location getInitialLocation() {
		return init_loc;
	}
		
	public String toString() {
		return "name=" + name + "\n locations=" + locations + "\n transitions=" + transitions + "\n X_C="
				+ X_C + "\n X_C_DOT ="+ X_C_DOT + "\n X_D=" + X_D + "\n I=" + I + "\n O=" + O + "\n X_D=" + X_D + "\n init_loc="
				+ init_loc + "\n init_val=" + initialization;
	}
}
