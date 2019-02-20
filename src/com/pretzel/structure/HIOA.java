package com.pretzel.structure;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.conqat.lib.simulink.model.stateflow.StateflowChart;
import org.conqat.lib.simulink.model.stateflow.StateflowData;
import org.conqat.lib.simulink.model.stateflow.StateflowNodeBase;
import org.conqat.lib.simulink.model.stateflow.StateflowTransition;

import com.pretzel.reader.Keyword;
/**
 * This class defines the structure of Hybrid Input Output Automata (HIOA).
 * @author Jin Woo Ro
 *
 */
public class HIOA {
	/**
	 * Name of the HIOA object instance.
	 */
	private String name;
	private HashSet<Location> locations = new HashSet<Location>();
	private HashSet<Transition> transitions = new HashSet<Transition>();
	private HashMap<String, Double> X_C  = new HashMap<String, Double>();
	private HashSet<String> X_C_DOT  = new HashSet<String>();
	private HashSet<String> I  = new HashSet<String>();
	private HashMap<String, Double> O  = new HashMap<String, Double>();
	private HashMap<String, Double> X_D  = new HashMap<String, Double>();
	private int init_loc_id;
	private HashSet<String> init_val = new HashSet<String>();
	
	public HIOA(String name){
		this.name = name;
	}
	 
	public String getName() {
		return name;
	}

	public HashMap<String,Double> getXC() {
		return X_C;
	}
	
	public HashSet<Transition> getTransitionsBySrc(int src_id){
		HashSet<Transition> transitions_subset = new HashSet<Transition>();
		for (Transition t : transitions) {
			if (t.getSrcId() == src_id) {
				transitions_subset.add(t);
			}
		}
		return transitions_subset;
	}
	
	public HashMap<String,Double> getXD() {
		return X_D;
	}
	
	public int getInitLocID() {
		return init_loc_id;
	}
	
	public String getInitLocName() {
		for (Location l : locations) {
			if (l.checkByID(init_loc_id)) {
				return l.getName();
			}
		}
		// Error due to unknown initial location
		System.exit(0);
		return null;
	}
	
	public HashMap<String,Double> getO() {
		return O;
	}
	
	public HashSet<String> getI() {
		return I;
	}
	
	public HashSet<Location> getLocations(){
		return locations;
	}

	public String toString() {
		return "name=" + name + "\n locations=" + locations + "\n transitions=" + transitions + "\n X_C="
				+ X_C + X_C_DOT + "\n X_D=" + X_D + "\n I=" + I + "\n O=" + O + "\n X_D=" + X_D + "\n init_loc="
				+ init_loc_id + "\n init_val=" + init_val;
	}

	public void convert(StateflowChart chart) {
		// extractData extracts variables
		extractData(chart);
		// extract location information
		for (StateflowNodeBase n : chart.getNodes()) {
			int id = Integer.parseInt(n.getId());
			extractLocation(n.getResolvedId().toString(), id);
			// extract transition information
			for (StateflowTransition t : n.getInTransitions()) {
				if (t.getSrc() == null) {
					init_loc_id = Integer.parseInt(t.getDst().getId());
					// Capture the value assignments within {} bracket on the starting transition
					Matcher m = Pattern.compile("\\{(.*?)\\}").matcher(t.obtainLabelData().getText());
					while(m.find()) {
						String members[] = m.group(1).replaceAll("\\\\n|\\\\r|\\r|\\n", "").split(";");
						init_val = new HashSet<String>(Arrays.asList(members)); 
					}
				}
				else {
					int src_id = Integer.parseInt(t.getSrc().getId());
					int dst_id = Integer.parseInt(t.getDst().getId());
					addEdge(src_id, dst_id, t.obtainLabelData().getText());
				}
			}
		}
		System.out.print("Initial location : " + init_loc_id);
	}
	
	private void extractLocation(String node, int id) {
		String s = node.trim().replaceAll("\\s", "");
		String[] parts = s.split("\\\\n|\\\\r|\\r|\\n");
		String locname[] = parts[0].split("/");
		// Set the location name and id
		Location loc = new Location(locname[locname.length-1]);
		loc.setID(id);
		System.out.println("Location: " + loc.getName() + " id is : " + loc.getID());

		// TODO: this string extraction needs improvement in terms of
		// detecting du and en keywords to distinguish f, h, and reset
		int mode = 0;
		// "i" is the line count
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
					loc.addODE(parts[i].replace(";", ""));
				} else if (O.containsKey(LHS)) {
					loc.addOutputUpdate(parts[i]);
				}
			} else if (mode == 2) {
				loc.addExtraInit(parts[i].replaceAll(";",""));
			} else if (mode == 3) {
				String LHS = parts[i].split("=")[0];
				if (X_C_DOT.contains(LHS)){
					loc.addODE(parts[i].replaceAll(";", ""));
				} else if (O.containsKey(LHS)) {
					loc.addOutputUpdate(parts[i]);
				}
				loc.addExtraInit(parts[i]);
			}
		}
		locations.add(loc);
		//System.out.println("Location: " + loc.toString());
	}
	
	private void addEdge(int src, int dst, String label) {
		Transition tran = new Transition(src, dst);
		
		Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(label);
		while(m.find()) tran.setGuard(m.group(1));
		m = Pattern.compile("\\{(.*?)\\}").matcher(label);
		while(m.find()) tran.setReset(m.group(1));
		transitions.add(tran);
		//System.out.println("Transitions: " + tra);
	}
	
	public Location getLocationByID(int id) {
		for (Location h : locations) {
			if (h.checkByID(id) == true) {
				return h;
			}
		}
		return null;
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
					O.put(chart.getName() + "_" + data.getName(), ival);
					break;
				case Keyword.INPUT_DATA:
					I.add(chart.getName() + "_" + data.getName());
					break;
				default:
					System.out.println("Unknown scope variable is detected!!");
					break;
			}
		}
	}
}
