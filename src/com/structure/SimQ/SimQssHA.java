package com.structure.SimQ;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimQssHA {
	// Name of this HA
	private String name;
	
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
	private HashMap<String, SimQssHATran> transitions = new HashMap<String, SimQssHATran>();
	
	// A set of continuous variables is a structure that includes:
	// 1. Name of the valuation
	// 2. Initial value
	private HashMap<String, Double> contVar  = new HashMap<String, Double>();
	private HashMap<String, Double> inputs  = new HashMap<String, Double>();
	private HashMap<String, Double> outputs  = new HashMap<String, Double>();
	private HashMap<String, Double> discVar  = new HashMap<String, Double>();
	
	private String initLoc;
	
	public SimQssHA(String name){
		this.name = name;
	}
	
	public void addLocation(String node) {
		// A node is a String which includes all information of one "state"
		// This includes the name of the state and all the computation associated with this state
		// TODO: A conversion, String node -> (String loc, String f, String h)
		String locName, f, h;
		
		String[] parts = node.split("\\r?\\n");
		locName = parts[0];
		
		int mode = 0; // A temporary variable that indicates "du" or "en" or "du, en"
		for (String p : parts) {
			if (p.contains("du:")) {
				mode = 1;
				continue;
			}
			else if (p.contains("en:")) {
				mode = 2;
				continue;
			}
			
			if (mode == 1) {
				// here we need to parse the equation as String
				
			}
		}
				
		// If 'loc' is not found in the dictionary, then add it
		if (this.locations.containsKey(locName) == false){
			SimQssHALoc l = new SimQssHALoc(locName);
			this.locations.put(locName, l);
		}
	}
	
	public void addEdge(String src, String dst, String label) {
		SimQssHATran tra = new SimQssHATran(src, dst);
		String tname = src + "->" + dst;
		String G = label;
		String R = label;
		
		Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(label);
		while(m.find()) tra.setGuard(m.group(1));
		m = Pattern.compile("\\{(.*?)\\}").matcher(label);
		while(m.find()) tra.setReset(m.group(1));
		transitions.put(tname, tra);
	}
	
	public void setInitLoc(String loc) {
		this.initLoc = loc;
	}
	
	public void addInvariant(String loc, String inv) {
		// Apply only if the location exists 
		if (this.locations.containsKey(loc) == true) {
			this.locations.get(loc).addInvariant(inv);
		}
	}
	
	// Getters 
	public String getName() {
		return this.name;
	}

	public void addVariable(String name, double value) {
		//this.variables.put(name, value);
	}

	@Override
	public String toString() {
		return "HA [name=" + name + ", locations=" + locations + ", transitions=" + transitions + "\n contVar="
				+ contVar + "\n inputs=" + inputs + "\n outputs=" + outputs + "\n discVar=" + discVar + "\n initLoc="
				+ initLoc + "]";
	}
}
