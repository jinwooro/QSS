package com.pretzel.structure.automata;

import java.util.HashMap;
import java.util.HashSet;

import com.pretzel.structure.basic.Formula;
import com.pretzel.structure.basic.Location;
import com.pretzel.structure.basic.Variable;
import com.pretzel.structure.enums.variableParam;

public class QSHIOA extends HIOA {

	protected HashSet<Variable> X_QSS = new HashSet<Variable>();
	protected HashMap<String, HashSet<Formula> > f_qss = new HashMap<String, HashSet<Formula> >();

	public QSHIOA(String name) {
		super(name);
	}	
	
	@Override
	public void addContinuousVariable(String name, double initialValue) {
		super.addContinuousVariable(name, initialValue);
		// Every continuous variable creates a respective QSS variable
		String qss = name + "_qss";
		Variable v_qss = new Variable(qss);
		v_qss.setType(variableParam.Type.DOUBLE);
		v_qss.setScope(variableParam.Scope.LOCAL_CONTINUOUS_QUANTIZED);
		v_qss.setInitialValue(initialValue);
		X_QSS.add(v_qss);
	}
	
	@Override
	public void addLocation(Location l) {
		super.addLocation(l);
		// For every location added, f also generates f_qss
		HashSet<Formula> f_q = new HashSet<Formula>();
		
		for (Formula fx : l.getODEs()) {
			Formula q = new Formula(fx); // create a clone
			for (Variable x : X_C) {
				String xq = x.getName() + "_qss";
				q.substitute(x.getName(), xq);
			}
			f_q.add(q);
		}
		f_qss.put(l.getName(), f_q);
	}

	@Override
	public String toString() {
		return "QSHIOA [name=" + name + "]";
	}
	
	
}
