package com.pretzel.structure.variants;

import java.util.HashSet;

import com.pretzel.structure.basic.Formula;
import com.pretzel.structure.basic.HybridAutomata;
import com.pretzel.structure.basic.Location;
import com.pretzel.structure.basic.Variable;
import com.pretzel.structure.enums.variableParam;

public class QSHA extends HybridAutomata {
	private HashSet<Variable> X_Q = new HashSet<Variable>();
	private HashSet<Variable> I = new HashSet<Variable>();
	private HashSet<Variable> O = new HashSet<Variable>();
	
	public class QssLocation extends Location {
		private HashSet<Formula> qssODE = new HashSet<Formula>();

		public QssLocation(String name) {
			super(name);
		}
		
		public void addQssODE(Formula s) {
			qssODE.add(s);
		}
		
		public HashSet<Formula> getQssODEs() {
			return qssODE;
		}
		
	}
	
	public QSHA(String name) {
		super(name);
	}
	
	public void addInputVariable(String name) {
		Variable var = new Variable(name);
		var.setScope(variableParam.Scope.INPUT_VARIABLE);
	}
	
	public void addOutputVariable(String name) {
		Variable var = new Variable(name);
		var.setScope(variableParam.Scope.OUTPUT_VARIABLE);
	}
	
	public void addQuantizedStateVariable(String name) {
		Variable var = new Variable(name);
		var.setScope(variableParam.Scope.LOCAL_CONTINUOUS_QUANTIZED);
	}
}
