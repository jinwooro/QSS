package com.structure.SimQ;

public class SimQVariable {
	private	double value;
	private String name;
		
	SimQVariable(String name, double initVal){
		this.name = name;
		this.value = initVal;
	}
	
	public String getName() {
		return this.name;
	}
	
	public double getInit() {
		return this.value;
	}
}
