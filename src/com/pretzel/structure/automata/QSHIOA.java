package com.pretzel.structure.automata;

import java.util.HashSet;

import com.pretzel.structure.basic.Formula;
import com.pretzel.structure.basic.Location;
import com.pretzel.structure.basic.Variable;
import com.pretzel.structure.enums.variableParam;

public class QSHIOA extends HIOA {

	protected HashSet<Variable> X_QSS = new HashSet<Variable>();
	protected HashSet<QssLocation> qLocations = new HashSet<QssLocation>();
		
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
	
	public QSHIOA(String name) {
		super(name);
	}	
	
	@Override
	public void addContinuousVariable(String name, double initialValue) {
		super.addContinuousVariable(name, initialValue);
		// Every continuous variable creates a qss variable
		String qss = name + "_qss";
		Variable v_qss = new Variable(qss);
		v_qss.setType(variableParam.Type.DOUBLE);
		v_qss.setScope(variableParam.Scope.LOCAL_CONTINUOUS_QUANTIZED);
		v_qss.setInitialValue(initialValue);
		X_QSS.add(v_qss);
	}

	@Override
	public String toString() {
		return "QSHIOA [name=" + name + "]";
	}
	
	
}
