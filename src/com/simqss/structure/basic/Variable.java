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
	 * It returns the integral name of the subject variable if it is a derivative.
	 * For example, 'x_dot' is a derivative of 'x'. Thus, this function returns 'x' instead of 'x_dot'.
	 * Note: if the subject varialbe is not a derivative, this function returns the subject variable name as is.
	 * @return The integral name
	 */
	public String getIntegralName() {
		if (this.scope == variableParam.Scope.LOCAL_CONTINUOUS_DERIVATIVE) {
			String integralName = name.replace("_dot", "");
			return integralName;
		}
		return name;
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
