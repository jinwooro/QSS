package com.simqss.structure;

import com.simqss.adapter.utils.Symbol;

/**
 * This class describes a generic mathematical expression
 * @author Jin Woo Ro
 *
 */
public class Formula {
	private String RHS;
	private String relation;
	private String LHS;
 	
	/**
	 * Constructor. 
	 */
	public Formula() {}
	
	public Formula(String RHS, String relation, String LHS) {
		this.RHS = RHS;
		this.relation = relation;
		this.LHS = LHS;
	}
	
	public Formula(Formula f) {
		this(f.getRHS(), f.getRelation(),f.getLHS());
	}
	
	public Formula(String LHS) {
		this.LHS = LHS;
	}
	
	public void setRHS(String RHS) {
		this.RHS = RHS;
	}
	
	public String getRHS() {
		return RHS;
	}
	
	public void setRelation(String relation) {
		this.relation = relation;
	}
	
	public String getRelation(){
		return relation;
	}
	
	public void setLHS(String LHS) {
		this.LHS = LHS;
	}
	
	public String getLHS() {
		return LHS;
	}
	
	public void substituteLHSvar(String a, String b) {
		LHS = LHS.replaceAll(a, b);
	}

	// extract the relation
	public static String extractRelation(String equation){
		if (equation.contains("==")) { return "=="; }
		else if (equation.contains("<=")) { return "<="; }
		else if (equation.contains(">=")) { return ">="; }
		else if (equation.contains("<")) { return "<"; }
		else if (equation.contains(">")) { return ">"; }
		else if (equation.contains("=")) { return "="; }
		return null;
	}
	
	/**
	 * A helper function that transforms a string equation into a formula.
	 * @param eq The equation.
	 * @return Returns the resulting Formula object.
	 */
	public static Formula makeFormula(String eq) {
		String equation = eq.replaceAll("\\s+",  "");
		
		String relation = Formula.extractRelation(equation);
		// Has no relation is not a valid formula
		if (relation == null) { return null; }
		
		String lvalue = equation.split(relation.toString())[0];
		String rvalue = equation.split(relation.toString())[1];
		
		Formula fx = new Formula(lvalue);
		fx.setRelation(relation);
		fx.setRHS(rvalue);
		
		return fx;
	}
	
	@Override
	public String toString() {
		return LHS + relation + RHS;
	}


}
