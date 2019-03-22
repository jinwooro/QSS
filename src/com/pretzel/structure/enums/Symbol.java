package com.pretzel.structure.enums;

public class Symbol {
	// This is the future implementation of reading the equation
	public enum Operator {
		PLUS,
		MINUS,
		MULTIPLE,
		DIVIDE,
		POWER;
	}
	
	public enum Relation {
		EQUAL,
		EQUAL_EQUAL,
		LESS,
		GREATER,
		LESS_EQUAL,
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

