package com.pretzel.structure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

public class Block {
	private String name;
	private String type;
	private HashSet<String> InPortNames = new HashSet<String>();
	private HashSet<String> OutPortNames = new HashSet<String>();
	private int samplingRate = -1; // -1 means it is called only by other charts or blocks
	private int initialValue = 0; // 0 by default
	
	public Block(String name, String type) {
		this.name = name;
		this.type = type;
		
		// this injects predefined input ouput port spec
		if (type.equals("Constant")) {
			OutPortNames.add(name + "_" + 1);
		}
		else if (type.equals("Sum")) {
			InPortNames.add(name + "_" + 1);
			InPortNames.add(name + "_" + 2);
			OutPortNames.add(name + "_" + 1);
		}
		else if (type.equals("Scope")) {
			InPortNames.add(name + "_" + 1);
		}
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public int getInPortCount() {
		return InPortNames.size();
	}
	
	public int getOutPortCount() {
		return OutPortNames.size();
	}
	
	public HashSet<String> getInputNames(){
		return InPortNames;
	}
	
	public HashSet<String> getOutputNames(){
		return OutPortNames;
	}
	
	
	@Override
	public String toString() {
		return "Block [name=" + name + ", type=" + type + "]";
	}

	
}
