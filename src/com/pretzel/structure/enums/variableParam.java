package com.pretzel.structure.enums;

public class variableParam {
	public enum Type {
		DOUBLE,
		BOOL,
		INT,
		FLOAT;
	}
	
	public enum Scope {
		INPUT_VARIABLE,
		OUTPUT_VARIABLE,
		INPUT_SIGNAL,
		OUTPUT_SIGNAL,
		LOCAL_CONTINUOUS,
		LOCAL_DISCRETE,
		LOCAL_CONTINUOUS_DERIVATIVE, 
		LOCAL_CONTINUOUS_QUANTIZED;
	}
}
