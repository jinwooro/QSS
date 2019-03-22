package com.pretzel.structure.basic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pretzel.structure.enums.Symbol;

public class Formula {
	private Variable subject;
	private Symbol.Relation relation;
	private String expr;
	
	public Formula() {}
	public Formula(Variable subject, Symbol.Relation relation, String expr) {
		this.subject = subject;
		this.relation = relation;
		this.expr = expr;
	}
	public Formula(Variable subject) {
		this.subject = subject;
	}
	
	public void setSubject(Variable subject) {
		this.subject = subject;
	}
	
	public Variable getSubject() {
		return subject;
	}
	
	public void setRelation(Symbol.Relation r) {
		relation = r;
	}
	
	public Symbol.Relation getRelation(){
		return relation;
	}
	
	public void setExpr(String expr) {
		this.expr = expr;
	}
	
	public String getExpr() {
		return expr;
	}
	
	// This is a helper function that detects and returns the relation symbol in the string equation
	public static Symbol.Relation getRelation(String equation){
		if (equation.contains("==")) { return Symbol.Relation.EQUAL_EQUAL; }
		else if (equation.contains("<=")) { return Symbol.Relation.LESS_EQUAL; }
		else if (equation.contains(">=")) { return Symbol.Relation.GREATER_EQUAL; }
		else if (equation.contains("<")) { return Symbol.Relation.LESS; }
		else if (equation.contains(">")) { return Symbol.Relation.GREATER; }
		else if (equation.contains("=")) { return Symbol.Relation.EQUAL; }
		return null;
	}
	
	// A helper function that transforms a string equation into a formula
	// TODO: detection of multiple relation symbol is not implemented yet
	public static Formula makeFormula(String stringEquation) {
		Symbol.Relation relation = Formula.getRelation(stringEquation);
		if (relation == null) { return null; }
		
		String subjectVariableName = stringEquation.split(relation.toString())[0];
		Variable x = new Variable(subjectVariableName); // creates a dummy Variable instance
		Formula fx = new Formula(x);
		
		fx.setRelation(relation);
		
		String expression = stringEquation.split(relation.toString())[1];
		fx.setExpr(expression);
		return fx;
	}
	
	@Override
	public String toString() {
		return subject.getName() + relation.toString() + expr;
	}

}
