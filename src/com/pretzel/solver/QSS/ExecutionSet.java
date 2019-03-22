package com.pretzel.solver.QSS;

import java.util.HashSet;

public class ExecutionSet {
	private HashSet<String> names = new HashSet<String>();
	private int referenceTime = 0;
	
	public ExecutionSet(int referenceTime) {
		this.referenceTime = referenceTime;
	}
	
	public void addName(String name) {
		this.names.add(name);
	}
	
	public HashSet<String> getNames(){
		return this.names;
	}
	
	public boolean hasName(String name) {
		if (this.names.contains(name)) {
			return true;
		} else {
			return false;
		}
	} 
	
	public double getReferenceTime() {
		return this.referenceTime;
	}
}
