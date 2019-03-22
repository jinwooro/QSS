package com.pretzel.structure.variants;

import java.util.HashMap;
import java.util.HashSet;

import com.pretzel.structure.basic.HybridAutomata;
import com.pretzel.structure.basic.Location;
import com.pretzel.structure.basic.Transition;
import com.pretzel.structure.basic.Variable;
import com.pretzel.structure.enums.variableParam;

/**
 * This class defines the structure of Hybrid Input Output Automata (HIOA).
 * @author Jin Woo Ro
 *
 */
public class HIOA extends HybridAutomata {
	private HashSet<Variable> I = new HashSet<Variable>();
	private HashSet<Variable> O = new HashSet<Variable>();
	
	public HIOA(String name) {
		super(name);
	}
	
	public void addInputVariable(String name) {
		Variable v = new Variable(name);
		v.setScope(variableParam.Scope.INPUT_VARIABLE);
		I.add(v);
	}
	
	public void addOutputVariable(String name) {
		Variable v = new Variable(name);
		v.setScope(variableParam.Scope.OUTPUT_VARIABLE);
		O.add(v);
	}
	
	public Variable hasVariable(String name) {
		HashSet<Variable> V = new HashSet<Variable>();
		V.addAll(X_C);
		V.addAll(X_D);
		V.addAll(X_DOT);
		V.addAll(I);
		V.addAll(O);
		for (Variable var : V) {
			if (var.getName().equals(name)) {
				return var;
			}
		}
		return null;
	}
	
}
