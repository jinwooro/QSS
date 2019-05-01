package com.simqss.structure.enums;

/**
 * This class contains enums for the mathematical symbols.
 * @author Jin Woo Ro
 *
 */
public class Symbol {
	// This is the future implementation of reading the equation
	/**
	 * Mathematical operators.
	 * @author Jin Woo Ro
	 *
	 */
	public enum Operator {
		/**
		 * '+' addition operator.
		 */
		PLUS,
		/**
		 * '-' subtraction operator. 
		 */
		MINUS,
		/**
		 * '*' multiplication operator.
		 */
		MULTIPLE,
		/**
		 * '/' division operator.
		 */
		DIVIDE,
		/**
		 * '**' power operator.
		 */
		POWER;
	}
	
	/**
	 * Mathematical relations.
	 * @author Jin Woo Ro
	 *
	 */
	public enum Relation {
		/**
		 * '=' equal operator (value assignment). 
		 */
		EQUAL,
		/**
		 * '==' equal comparison operator. 
		 */
		EQUAL_EQUAL,
		/**
		 * '{@literal <}' less than comparison operator. 
		 */
		LESS,
		/**
		 * '{@literal >}' greater than comparison operator.
		 */
		GREATER,
		/**
		 * '{@literal <=}' less than or equal to comparison operator.
		 */
		LESS_EQUAL,
		/**
		 * '{@literal >=}' greater than or equal to comparison operator. 
		 */
		GREATER_EQUAL;
		
		@Override
		public String toString() {
			switch(this) {
				case EQUAL: return "=";
				case EQUAL_EQUAL: return "==";
				case LESS: return "<";
				case GREATER: return ">";
				case LESS_EQUAL: return "<=";
				case GREATER_EQUAL: return ">=";
				default: throw new IllegalArgumentException();
			}
		}
	}
}

