package com.simqss.structure.basic;

import com.simqss.utils.Symbol;

/**
 * This class describes a mathematical equation.
 * @author Jin Woo Ro
 *
 */
public class Formula {
	private Variable subject;
	private Symbol.Relation relation;
	private String expr;
	
	/**
	 * Constructor. 
	 */
	public Formula() {}
	/**
	 * Constructor.
	 * @param subject The subject variable (the left hand side).
	 * @param relation The mathematical relation.
	 * @param expr The expression (the right hand side).
	 */
	public Formula(Variable subject, Symbol.Relation relation, String expr) {
		this.subject = subject;
		this.relation = relation;
		this.expr = expr;
	}
	
	/**
	 * Constructor that clones another Formula.
	 * @param f Another formula object.
	 */
	public Formula(Formula f) {
		this(f.getSubject(), f.getRelation(),f.getExpr());
	}
	
	/**
	 * Constructor that creates an empty equation.
	 * @param subject The left hand side.
	 */
	public Formula(Variable subject) {
		this.subject = subject;
	}
	
	/**
	 * Sets the subject variable.
	 * @param subject The subject variable. 
	 */
	public void setSubject(Variable subject) {
		this.subject = subject;
	}
	
	/**
	 * @return Returns the subject variable.
	 */
	public Variable getSubject() {
		return subject;
	}
	
	/**
	 * Sets the relation.
	 * @param r Mathematical relation.
	 */
	public void setRelation(Symbol.Relation r) {
		relation = r;
	}
	
	/**
	 * @return Returns the relation.
	 */
	public Symbol.Relation getRelation(){
		return relation;
	}
	
	/**
	 * Sets the expression.
	 * @param expr The expression.
	 */
	public void setExpr(String expr) {
		this.expr = expr;
	}
	
	/**
	 * @return Returns the expression.
	 */
	public String getExpr() {
		return expr;
	}
	
	/**
	 * Substitute the variable 'a' in the expression with 'b'.
	 * @param a the target variable to be replaced.
	 * @param b the substitution variable.
	 */
	public void substitute(String a, String b) {
		// Substitute the variable a in the equation with the variable b, and update the equation  
		expr = expr.replaceAll(a, b);
	}
	
	/**
	 * This is a helper function that detects and returns the relation symbol in the string equation
	 * @param equation The input expression.
	 * @return The relation in the requested expression.
	 */
	public static Symbol.Relation getRelation(String equation){
		if (equation.contains("==")) { return Symbol.Relation.EQUAL_EQUAL; }
		else if (equation.contains("<=")) { return Symbol.Relation.LESS_EQUAL; }
		else if (equation.contains(">=")) { return Symbol.Relation.GREATER_EQUAL; }
		else if (equation.contains("<")) { return Symbol.Relation.LESS; }
		else if (equation.contains(">")) { return Symbol.Relation.GREATER; }
		else if (equation.contains("=")) { return Symbol.Relation.EQUAL; }
		return null;
	}
	
	/**
	 * A helper function that transforms a string equation into a formula.
	 * @param eq The equation.
	 * @return Returns the resulting Formula object.
	 */
	public static Formula makeFormula(String eq) {
		String equation = eq.replaceAll("\\s+",  "");
		
		Symbol.Relation relation = Formula.getRelation(equation);
		if (relation == null) { return null; }
		
		String subjectVariableName = equation.split(relation.toString())[0];
		Variable x = new Variable(subjectVariableName); // creates a dummy Variable instance
		Formula fx = new Formula(x);
		
		fx.setRelation(relation);
		
		String expression = equation.split(relation.toString())[1];
		fx.setExpr(expression);
		return fx;
	}
	
	@Override
	public String toString() {
		return subject.getName() + relation.toString() + expr;
	}

}
