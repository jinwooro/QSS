package com.pretzel.structure;

import java.util.HashMap;

public class Block {
	private String name;
	private String type;
	private HashMap<Integer, String> InPorts = new HashMap<Integer, String>();
	private HashMap<Integer, String> OutPorts = new HashMap<Integer, String>();
	private int samplingRate = -1; // -1 means it is called only by other charts or blocks
	private int initialValue = 0; // 0 by default
	
	public Block(String name, String type) {
		this.name = name;
		this.type = type;
	}
		
	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}
	
	public String getInportByIndex(int index) {
		if (!(InPorts.containsKey(index))) {
			return null;
		}
		return InPorts.get(index); 
	}
	
	public String getOutportByIndex(int index) {
		if (!(OutPorts.containsKey(index))) {
			return null;
		}
		return OutPorts.get(index);
	}
	
	public void addInPort(int port, String name) {
		InPorts.put(port, name);
	}
	
	public void addOutPort(int port, String name) {
		OutPorts.put(port, name);
	}
}
