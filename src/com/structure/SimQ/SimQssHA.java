package com.structure.SimQ;

import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.conqat.lib.simulink.model.stateflow.StateflowChart;
import org.conqat.lib.simulink.model.stateflow.StateflowData;
import org.conqat.lib.simulink.model.stateflow.StateflowNodeBase;
import org.conqat.lib.simulink.model.stateflow.StateflowTransition;

public class SimQssHA {
	// Name of this HA
	private String name;
	
	// A location data structure includes:
	// 1. Name of the location (String)
	// 2. Invariants of the location (Set of Strings)
	// 3. f of the location (String)
	// 4. h of the location (String)
	private HashSet<SimQssHALoc> locations = new HashSet<SimQssHALoc>();
	
	// A transition data structure includes:
	// 1. Source location (String)
	// 2. Destination location (String)
	// 3. Guard conditions (Set of Strings)
	// 4. Reset functions (Set of Strings)
	private HashSet<SimQssHATran> transitions = new HashSet<SimQssHATran>();
	
	// A set of continuous variables is a structure that includes:
	// 1. Name of the valuation
	// 2. Initial value
	private HashMap<String, Double> X_C  = new HashMap<String, Double>();
	private HashSet<String> X_C_DOT  = new HashSet<String>();
	private HashSet<String> I  = new HashSet<String>();
	private HashMap<String, Double> O  = new HashMap<String, Double>();
	private HashMap<String, Double> X_D  = new HashMap<String, Double>();
	
	private String init_loc;
	private String init_val;
	
	public SimQssHA(String name){
		this.name = name;
	}
	 
	public String getName() {
		return name;
	}

	public HashMap<String,Double> getXC() {
		return X_C;
	}
	
	public HashMap<String,Double> getXD() {
		return X_D;
	}
	
	public HashMap<String,Double> getO() {
		return O;
	}
	
	public HashSet<String> getI() {
		return I;
	}
	
	public HashSet<SimQssHALoc> getLoc(){
		return locations;
	}

	public String toString() {
		return "name=" + name + "\n locations=" + locations + "\n transitions=" + transitions + "\n X_C="
				+ X_C + X_C_DOT + "\n X_D=" + X_D + "\n I=" + I + "\n O=" + O + "\n X_D=" + X_D + "\n init_loc="
				+ init_loc + "\n init_val=" + init_val;
	}

	public void convert(StateflowChart chart) {
		extractData(chart);
		// Extract location and edge information
		for (StateflowNodeBase n : chart.getNodes()) {
			extractLocation(n.getResolvedId().toString());
			for (StateflowTransition t : n.getInTransitions()) {
				if (t.getSrc() == null) {
					setInitLoc(t.getDst().getResolvedId().toString().split("\\\\n|\\\\r|\\r|\\n")[0].replaceAll("/", "_"));
					// TODO: need to resolve the initialization
					
					Matcher m = Pattern.compile("\\{(.*?)\\}").matcher(t.obtainLabelData().getText());
					while(m.find()) init_val = m.group(1);
				}
				else {
					String src = t.getSrc().getResolvedId().toString().split("\\\\n|\\\\r|\\r|\\n")[0].replaceAll("/", "_");
					String dst = t.getDst().getResolvedId().toString().split("\\\\n|\\\\r|\\r|\\n")[0].replaceAll("/", "_");
					addEdge(src, dst, t.obtainLabelData().getText());
				}
			}
		}
	}
	
	private void extractLocation(String node) {
		String s = node.trim().replaceAll("\\s", "");
		String[] parts = s.split("\\\\n|\\\\r|\\r|\\n");
		SimQssHALoc loc = new SimQssHALoc(parts[0].replaceAll("/", "_"));

		// TODO: this string extraction needs improvement
		int mode = 0;
		for (int i = 1; i < parts.length; i++) {
			if (parts[i].contains(":")){
				if (parts[i].contains("du") == true && parts[i].contains("en") == false) {
					mode = 1; // du
					continue;
				} else if (parts[i].contains("en") == true && parts[i].contains("du") == false){
					mode = 2; // en
					continue;
				} else if (parts[i].contains("en") && parts[i].contains("du")) {
					mode = 3; // du, en
					continue;
				}
			} 
			
			if (mode == 1) {
				String LHS = parts[i].split("=")[0];
				if (X_C_DOT.contains(LHS)){
					loc.addODE(parts[i]);
				} else if (O.containsKey(LHS)) {
					loc.addOutputUpdate(parts[i]);
				}
			} else if (mode == 2) {
				loc.addExtraInit(parts[i]);
			} else if (mode == 3) {
				String LHS = parts[i].split("=")[0];
				if (X_C_DOT.contains(LHS)){
					loc.addODE(parts[i]);
				} else if (O.containsKey(LHS)) {
					loc.addOutputUpdate(parts[i]);
				}
				loc.addExtraInit(parts[i]);
			}
		}
		locations.add(loc);
		//System.out.println("Location: " + loc.toString());
	}
	
	private void setInitLoc(String loc) {
		init_loc = loc;
	}
	
	private void addEdge(String src, String dst, String label) {
		SimQssHATran tra = new SimQssHATran(src, dst);
		
		Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(label);
		while(m.find()) tra.setGuard(m.group(1));
		m = Pattern.compile("\\{(.*?)\\}").matcher(label);
		while(m.find()) tra.setReset(m.group(1));
		transitions.add(tra);
		//System.out.println("Transitions: " + tra);
	}
	
	private void extractData(StateflowChart chart) {
		for (StateflowData data : chart.getData()) {
			String scope = data.getParameter("scope");
			double ival = 0.0;
			switch (scope) {
				case Keyword.LOCAL_DATA:
					if (data.getDeclaredParameterNames().contains("props.initialValue")) {
						ival = Double.parseDouble(data.getParameter("props.initialValue"));
					}
					if (data.getDeclaredParameterNames().contains("props.updateMethod")) {
						if (data.getParameter("props.updateMethod").contentEquals(Keyword.CONTINUOUS_DATA)) {
							X_C.put(data.getName(), ival);
							X_C_DOT.add(data.getName()+"_dot");
						}
					} else {
						X_D.put(data.getName(), ival);
					}
					break;
				case Keyword.OUTPUT_DATA:
					if (data.getDeclaredParameterNames().contains("props.initialValue")) {
						ival = Double.parseDouble(data.getParameter("props.initialValue"));
					}
					O.put(data.getName(), ival);
					break;
				case Keyword.INPUT_DATA:
					I.add(data.getName());
					break;
				default:
					System.out.println("Unknown scope variable is detected!!");
					break;
			}
			//System.out.println("Variable parameter list: " + data.getDeclaredParameterNames());
		}
	}
}
