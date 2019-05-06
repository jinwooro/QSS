package com.simqss.structure.basic;

import com.simqss.utils.variableParam;

/**
 * This class describes the structure of a variable.
 * @author Jin Woo Ro
 *
 */
public class Variable {
	private String name;
	private int id;
	private variableParam.Type type;
	private variableParam.Scope scope;
	private double initialValue;
	
	/**
	 * Constructor.
	 * @param name Name of this variable.
	 */
	public Variable(String name) {
		this.name = name;
	}
	
	/**
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets an Id value.
	 * @param id Id of this variable.
	 */
	public void setID(int id) {
		this.id = id;
	}

	/**
	 * @return The id.
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * @return The type.
	 */
	public variableParam.Type getType() {
		return type;
	}
	/** 
	 * Sets the type of this variable.
	 * @param type The type.
	 */
	public void setType(variableParam.Type type) {
		this.type = type;
	}
	/**
	 * @return The scope.
	 */
	public variableParam.Scope getScope() {
		return scope;
	}
	/**
	 * Sets the scope of this variable.
	 * @param scope The scope.
	 */
	public void setScope(variableParam.Scope scope) {
		this.scope = scope;
	}
	/**
	 * @return The initial value.
	 */
	public double getInitialValue() {
		return initialValue;
	}
	/**
	 * Sets the initial value.
	 * @param initialValue The value.
	 */
	public void setInitialValue(double initialValue) {
		this.initialValue = initialValue;
	}

	@Override
	public String toString() {
		return "Variable [name=" + name + ", type=" + type + ", scope=" + scope + ", initialValue=" + initialValue
				+ "]";
	}
}
