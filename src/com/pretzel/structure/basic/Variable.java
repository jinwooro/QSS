package com.pretzel.structure.basic;

import com.pretzel.structure.enums.variableParam;

public class Variable {
	private String name;
	private int id;
	private variableParam.Type type;
	private variableParam.Scope scope;
	private double initialValue;
	
	public Variable(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setID(int id) {
		this.id = id;
	}

	public int getID() {
		return id;
	}
	
	public variableParam.Type getType() {
		return type;
	}
	public void setType(variableParam.Type type) {
		this.type = type;
	}
	public variableParam.Scope getScope() {
		return scope;
	}
	public void setScope(variableParam.Scope scope) {
		this.scope = scope;
	}
	public double getInitialValue() {
		return initialValue;
	}
	public void setInitialValue(double initialValue) {
		this.initialValue = initialValue;
	}

	@Override
	public String toString() {
		return "Variable [name=" + name + ", type=" + type + ", scope=" + scope + ", initialValue=" + initialValue
				+ "]";
	}
}
