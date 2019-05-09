package com.simqss.writer.python;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import com.simqss.structure.automata.QSHIOA;
import com.simqss.structure.basic.Formula;
import com.simqss.structure.basic.Location;
import com.simqss.structure.basic.Transition;
import com.simqss.structure.basic.Variable;
import com.simqss.structure.system.Line;
import com.simqss.structure.system.NetworkQSHIOA;
import com.simqss.utils.variableParam;
import com.simqss.writer.writerBase;

/**
 * This class contains the methods for the Python conversion of a QSHIOA.
 * @author Jin Woo Ro
 *
 */
public class pythonQSHIOA extends writerBase {
	protected int simulationTime = 60; // the simulation time is by default 60
	protected static final String STATEFLOW_PATH = "generated/stateflows"; // the QSHIOA converted from the Stateflows 
	protected static final String SIMBLOCK_PATH = "generated/blocks"; // the QSHIOA converted from the Simulink blocks
	
	/**
	 * Constructor. It inherits the writerBase constructor. However, this also copies the file "resource/python/ode.py" to the "generated" folder.
	 * @param filename The file name is usually the system name.
	 * @throws IOException An exception.
	 */
	public pythonQSHIOA(NetworkQSHIOA net) throws IOException {
		super(net.getName());
		copyFile(new File("resource/python/ode.py"), new File("generated/ode.py"));
		
		// the string used to write the codes.
		String temp = "";
		
		// create the main file
		String mainFile = ROOT_PATH + "/" + mainName + ".py";
		makeFile(mainFile);
		StringBuilder str = new StringBuilder();
		int indent = 0;
		// Starting commentation
		str.append(indentLine("#!/usr/bin/env python3", indent));		
		str.append(indentLine("#Converted file: " + net.getFileName(), indent));
		str.append("\r\n");
		// Library calls 
		str.append(indentLine("import sympy as S", indent));
		str.append(indentLine("from ode import ODE", indent));
		str.append("\r\n");
	
		// Global scope I/O variables
		str.append(indentLine("#I/O variables in the global scope", indent));
		for (QSHIOA q : net.getQSHIOAs()) {
			if (q.getInputVarCount() > 0) {			
				temp = q.getName() + "_I = {\r\n";
				for (Variable i : q.getInputs()) {
					temp = temp + "\t\"" + i.getName() + "\" : " + "0.0,\r\n";
				}
				temp = temp.substring(0, temp.length()-3);	
				temp = temp + "\r\n}";
				str.append(indentLine(temp,indent));
				temp = "";
			}
			
			if (q.getOutputVarCount() > 0) {
				temp = q.getName() + "_O = {\r\n";
				for (Variable o : q.getOutputs()) {
					temp = temp + "\t\"" + o.getName() + "\" : " + o.getInitialValue() + ",\r\n";
				}
				temp = temp.substring(0, temp.length()-3);
				temp = temp + "\r\n}";
				str.append(indentLine(temp, indent));
			}
		}
		str.append("\r\n");
		
		// Main function opening
		str.append(indentLine("def main():", indent++));
		str.append(indentLine("time = 0",indent));
		str.append(indentLine("delta = set()", indent));
		str.append("\r\n");
		
		
		// Instantiation of every QSHIOA (initialization)
		str.append(indentLine("# Initialization of each QSHIOA instance", indent));
		for (QSHIOA q: net.getQSHIOAs()) {
			str.append(indentLine("#" + q.getName() + " = " + q.getName() + "()", indent));
		}
		str.append("\r\n");
		
		str.append(indentLine("while (time < " + this.simulationTime + "):", indent++));
		// Loading the input variables
		str.append(indentLine("#Input loading", indent));
		for (QSHIOA q: net.getQSHIOAs()) {
			str.append(indentLine(q.getName() + ".loadInput(" + q.getName() + "_I)", indent));
		}
		str.append("\r\n");
		
		// Executing each QSHIOA
		str.append(indentLine("#Compute the next simulation time", indent));
		for (QSHIOA q: net.getQSHIOAs()) {
			str.append(indentLine("delta.add(" + q.getName() + ".run(time))", indent));
		}
		str.append("\r\n");
		
		// Write to the output variable
		str.append(indentLine("#Update the output variables", indent));
		for (QSHIOA q : net.getQSHIOAs()) {	
			str.append(indentLine(q.getName() + "_O = " + q.getName() + ".getOutput()", indent));
		}
		str.append("\r\n");
		
		// Input and output connection
		str.append(indentLine("#Connection between QSHIOA", indent));
		for (Line l : net.getLines()) {
			temp = l.getDstBlockName() + "[\"" + l.getDstPortName() + "\"] = " + l.getSrcBlockName() + "[\"" + l.getSrcPortName() + "\"]";
			str.append(indentLine(temp, indent));
		}
		str.append("\r\n");
	
		// update the global time
		str.append(indentLine("#Update the global time", indent));
		str.append(indentLine("time = min(delta)", indent));
		str.append(indentLine("delta.clear()", indent--));
		str.append("\r\n");
	
		// final call (for some visualization)
		str.append(indentLine("#For the final computation (e.g., visualization)", indent));		
		for (QSHIOA q: net.getQSHIOAs()) {
			str.append(indentLine(q.getName() + ".finish()", indent));
		}
		
		str.append("\r\n");
		// Main function call
		str.append(indentLine("if __name__ == '__main__':", --indent));
		str.append(indentLine("main()", ++indent));
		
		try {
			write(mainFile, str.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Write a file for each QSHIOA
		for (QSHIOA q: net.getQSHIOAs()) {
			writeQSHIOA(q);
		}
	}
	
	/**
	 * Sets the simulation time. If not explicitly set using this method, the simulation time is 60 seconds by default.
	 * @param simt The maximum simulation time.
	 */
	public void setSimulationTime(int simt) {
		this.simulationTime = simt;
	}
	
	/**
	 * @return Returns the saved error code.
	 */
	public int getErrorCode() {
		return errorCode;
	}
	
	/**
	 * Internally used function that writes a python code for each QSHIOA.
	 * @param qshioa QSAHIOA instance object.
	 * @throws IOException An exception to be thrown.
	 */
	private void writeQSHIOA(QSHIOA qshioa) throws IOException {
		// create a new file
		String qfile = ROOT_PATH + "/" + qshioa.getName() + ".py";
		makeFile(qfile);
		StringBuilder str = new StringBuilder();
		int indent = 0;
		
		// write the shebang comment. 
		str.append(indentLine("#!/usr/bin/env python3", indent));		
		str.append(indentLine("#Converted block: " + qshioa.getName(), indent));
		str.append("\r\n");
		// import calls 
		str.append(indentLine("import sympy as S", indent));
		str.append(indentLine("from ode import ODE", indent));
		str.append("\r\n");
		
		// write ODEs
		for (Location l : qshioa.getLocations()) {
			for (Formula f : l.getODEs()) {
				str.append(indentLine("#" + l.getName() + "_" + f.getSubject().getName() + " = ODE("
						+ "lvalue = '" + f.getSubject().getName() + "', rvalue = '" + f.getExpr() + "', "
						+ "ttol = 0.1" + ")", indent));
			}
		}
		str.append(indentLine("", indent));
		
		// declare the inter-location transitions
		for (Location l : qshioa.getLocations()) {
			str.append(indentLine("def " + l.getName() + "_edges(X, I, O):", indent = 0));
			if (l.getOutgoingTransitions().size() == 0) {
				str.append(indentLine("return " + l.getID(), ++indent));
			}
			else {
				// for each transition from this location,
				indent++;
				for (Variable v : qshioa.getContinuousVariables()) {
					str.append(indentLine(v.getName() + " = X[\"" + v.getName() + "\"]" , indent));
				}
				for (Variable v : qshioa.getInputs()) {
					str.append(indentLine(v.getName() + " = I[\"" + v.getName() + "\"]" , indent));
				}
				for (Transition t : l.getOutgoingTransitions()) {
					// write the guard condition
					String temp = "";
					for (Formula f : t.getGuards()) {
						temp = temp + "and " + "(" + f.toString() + ")";
					}
					temp = getPythonEquation(temp);
					temp = temp.replaceFirst(Pattern.quote("and "), "");
					str.append(indentLine("if " + temp + ":", indent));
					str.append(indentLine("# Exit actions", ++indent));
					str.append(indentLine("# Reset actions", indent));
					// reset function
					for (Formula f : t.getResets()) {
						String expr = getPythonEquation(f.getExpr());
						if (f.getSubject().getScope() == variableParam.Scope.LOCAL_CONTINUOUS) {
							str.append(indentLine("X[\"" + f.getSubject().getName() + "\"] = " + expr , indent));						
						}
						else if (f.getSubject().getScope() == variableParam.Scope.OUTPUT_VARIABLE) {
							str.append(indentLine("O[\"" + f.getSubject().getName() + "\"] = " + expr, indent));						
						}
					}
					str.append(indentLine("# Entry actions", indent));
					// entry action
					for (Formula f : l.getEntryActions()) {
						String expr = getPythonEquation(f.getExpr());						
						str.append(indentLine("X[\"" + f.getSubject().getName() + "\"] = " + expr, indent));						
					}
					str.append(indentLine("return " + t.getDst().getID(), indent));
				}
				str.append(indentLine("return " + l.getID(), --indent));
			}
			str.append("\r\n");
		}
		
		// declare the intra-location transitions
		for (Location l : qshioa.getLocations()) {
			str.append(indentLine("def " + l.getName() + "(time_step, X, I, O):", indent = 0));
			indent++;
			for (Formula f : l.getODEs()) {
				str.append(indentLine("#" + f.getSubject().getIntegralName() + " = " 
						+ l.getName() + "_" + f.getSubject().getName() + ".compute(time_step, X, I)", indent));
			}
			for (Formula f : l.getODEs()) {
				str.append(indentLine("X[\"" + f.getSubject().getIntegralName() + "\"] = " 
						+ f.getSubject().getIntegralName(), indent));
			}
			for (Formula h : l.getOutputUpdateActions()) {
				str.append(indentLine("O[\"" + h.getSubject().getName() + "\"] = " + h.getExpr(), indent));
			}
			str.append("\r\n");
		}
		
		// location mapping
		str.append(indentLine("Locations = {", indent=0));
		for (Location l : qshioa.getLocations()) {
			str.append(indentLine(l.getID() + " : " + l.getName() + ",", indent=1));
		}
		str.append(indentLine("}\r\n", --indent));
		// guard mapping
		str.append(indentLine("Edges = {", indent++));
		for (Location l : qshioa.getLocations()) {
			str.append(indentLine(l.getID() + " : " + l.getName() + "_edges,", indent));
		}	
		str.append(indentLine("}", --indent));
		str.append("\r\n");		
		
		// write the class definition opening
		str.append(indentLine("class " + qshioa.getName() + ":", indent=0));
		
		// constructor
		str.append(indentLine("def __init__(self):", indent=1));
		str.append(indentLine("self.loc = " + qshioa.getInitialLocation().getID(), ++indent));
		// input
		if (qshioa.getInputVarCount() > 0) {
			str.append(indentLine("self.I = {", indent++));
			for (Variable i : qshioa.getInputs()) {
				str.append(indentLine("\"" + i.getName() + "\" : " + i.getInitialValue() + ",", indent));
			}
			str.append(indentLine("}", --indent));
		}
		// output
		if (qshioa.getOutputVarCount() > 0) {
			str.append(indentLine("self.O = {", indent++));
			for (Variable o : qshioa.getOutputs()) {
				str.append(indentLine("\"" + o.getName() + "\" : " + o.getInitialValue() + ",", indent));
			}
			str.append(indentLine("}", --indent));
		}
		// continuous variables
		if (qshioa.getContVarCount() > 0) {
			str.append(indentLine("self.X = {", indent++));
			for (Variable x : qshioa.getContinuousVariables()) {
				str.append(indentLine("\"" + x.getName() + "\" : " + x.getInitialValue() + ",", indent));
			}
			str.append(indentLine("}", --indent));
		}
		for (Formula z : qshioa.getInitialization()) {
			if (z.getSubject().getScope() == variableParam.Scope.OUTPUT_VARIABLE) {
				// TODO: the right hand side expression may need some adjustment to be written in python
				str.append(indentLine("self.O[\"" + z.getSubject().getName() + "\"]=" + z.getExpr(), indent));
			} 
			else {				
				str.append(indentLine("self.X[\"" + z.getSubject().getName() + "\"]=" + z.getExpr(), indent));
			}
		}
		str.append("\r\n");
		
		// Input setter function
		str.append(indentLine("def setInput(self, name, value):", indent = 1));
		str.append(indentLine("self.I[name] = value", indent = 2));
		str.append("\r\n");
		
		// Output getter function
		str.append(indentLine("def getOutput(self, name):", indent = 1));
		str.append(indentLine("return self.O[name]", indent = 2));
		str.append("\r\n");
	
		// delta computation function
		str.append(indentLine("def getDelta(self):", indent = 1));
		str.append(indentLine("new_loc = Edges[self.loc](self.X, self.I, self.O)", ++indent));
		str.append(indentLine("if (self.loc != new_loc):", indent));
		str.append(indentLine("self.loc = new_loc", ++indent));
		str.append(indentLine("return 0 # delta is zero", indent));
		str.append(indentLine("else:", --indent));
		str.append(indentLine("# delta = computeDelta(self.X, self.I, self.loc)", ++indent));
		str.append(indentLine("return delta", indent));
		str.append("\r\n");
	
		// execute function
		str.append(indentLine("def intraTransition(self, time_step):", indent = 1));
		str.append(indentLine("Locations[self.loc](time_step, self.X, self.I, self.O)", ++indent));
		str.append("\r\n");
		
		// print funcdtion
		str.append(indentLine("def print(self):", indent = 1));
		str.append(indentLine("print(self.I)", ++indent));
		str.append(indentLine("print(self.O)", indent));
		str.append(indentLine("print(self.X)", indent));
		
		try {
			write(qfile, str.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @param equation Equation written in the Matlab language
	 * @return The corresponding equation in Python
	 */
	public String getPythonEquation(String equation) {
		String newEquation = equation;
		newEquation = newEquation.replace("power(", "pow("); // power() is convered into pow()
		return newEquation;	
	}

}
