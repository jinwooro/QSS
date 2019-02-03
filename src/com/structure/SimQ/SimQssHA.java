package com.structure.SimQ;

import java.util.HashMap;

public class SimQssHA {
	// Name of this HA
	private String name, type;
	
	// A location data structure includes:
	// 1. Name of the location (String)
	// 2. Invariants of the location (Set of Strings)
	// 3. f of the location (String)
	// 4. h of the location (String)
	private HashMap<String, SimQssHALoc> locations = new HashMap<String, SimQssHALoc>();
	
	// A transition data structure includes:
	// 1. Source location (String)
	// 2. Destination location (String)
	// 3. Guard conditions (Set of Strings)
	// 4. Reset functions (Set of Strings)
	private HashMap<String, SimQssHATrans> transitions = new HashMap<String, SimQssHATrans>();
	
	// A set of continuous variables is a structure that includes:
	// 1. Name of the valuation
	// 2. Initial value
	private HashMap<String, Double> variables  = new HashMap<String, Double>();
	
	public SimQssHA(String name, String type){
		this.name = name;
		this.type = type;
	}
	
	public void addLocation(String loc) {
		// If 'loc' is not found in the dictionary, then add it
		if (this.locations.containsKey(loc) == false){
			SimQssHALoc l = new SimQssHALoc(loc);
			this.locations.put(loc, l);
		}
	}
	
	public boolean addInvariant(String loc, String inv) {
		// Apply only if the location exists 
		if (this.locations.containsKey(loc) == true) {
			this.locations.get(loc).addInvariant(inv);
			return true;
		}
		return false;
	}
	
	// Getters 
	public String getName() {
		return this.name;
	}
}
