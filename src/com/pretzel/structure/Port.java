package com.pretzel.structure;

import com.pretzel.structure.enums.variableParam;

public class Port{
	private String blockName;
	private String portName;
	private variableParam.Scope scope;
	private variableParam.Type type = variableParam.Type.DOUBLE;
	
	public Port(String blockName, String portName) {
		this.blockName = blockName;
		this.portName = portName;
	}
	
	public String getBlockName() {
		return this.blockName;
	}
	
	public String getPortName() {
		return this.portName;
	}
	
	public void setThisInputVariable() {
		this.scope = variableParam.Scope.INPUT_VARIABLE;
	}
	
	public void setThisInputSignal() {
		this.scope = variableParam.Scope.INPUT_SIGNAL;
	}
	
	public void setThisOutputVariable() {
		this.scope = variableParam.Scope.OUTPUT_VARIABLE;
	}
	
	public void setThisOutputSignal() {
		this.scope = variableParam.Scope.OUTPUT_SIGNAL;
	}
	
	public boolean isInport() {
		if ((scope == variableParam.Scope.INPUT_SIGNAL) || 
				(scope == variableParam.Scope.INPUT_VARIABLE)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isOutport() {
		if ((scope == variableParam.Scope.OUTPUT_SIGNAL) || 
				(scope == variableParam.Scope.OUTPUT_VARIABLE)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "Port [portName=" + portName + "]";
	}
}
