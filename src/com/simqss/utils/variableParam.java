package com.simqss.utils;

/**
 * This class contains enums for describing the variables.
 * @author Jin Woo Ro
 *
 */
public class variableParam {
	public enum Type {
		/**
		 * Double type.
		 */
		DOUBLE,
		/**
		 * Boolean type.
		 */
		BOOL,
		/**
		 * Int type.
		 */
		INT,
		/**
		 * Float type. 
		 */
		FLOAT;
	}
	
	public enum Scope {
		/**
		 * Input.
		 */
		INPUT_VARIABLE,
		/**
		 * Output. 
		 */
		OUTPUT_VARIABLE,
		/**
		 * Input signal. 
		 */
		INPUT_SIGNAL,
		/**
		 * Output signal. 
		 */
		OUTPUT_SIGNAL,
		/**
		 * Local continuous variable. 
		 */
		LOCAL_CONTINUOUS,
		/**
		 * Local discrete variable. 
		 */
		LOCAL_DISCRETE,
		/**
		 * The derivative of the local continuous variables. 
		 */
		LOCAL_CONTINUOUS_DERIVATIVE, 
		/**
		 * The QSS variable of the local continuous variables. 
		 */
		LOCAL_CONTINUOUS_QUANTIZED;
	}
}
