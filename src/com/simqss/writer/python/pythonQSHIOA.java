//package com.simqss.writer.python;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.regex.Pattern;
//
//import com.simqss.adapter.utils.variableParam;
//import com.simqss.structure.Formula;
//import com.simqss.structure.Line;
//import com.simqss.structure.Location;
//import com.simqss.structure.NetworkHIOA;
//import com.simqss.structure.QSHIOA;
//import com.simqss.structure.Transition;
//import com.simqss.structure.Variable;
//import com.simqss.writer.writerBase;
//
///**
// * This class contains the methods for the Python conversion of a QSHIOA.
// * @author Jin Woo Ro
// *
// */
//public class pythonQSHIOA extends writerBase {
//	protected int simulationTime = 60; // the simulation time is by default 60
//	protected static final String STATEFLOW_PATH = "generated/stateflows"; // the QSHIOA converted from the Stateflows 
//	protected static final String SIMBLOCK_PATH = "generated/blocks"; // the QSHIOA converted from the Simulink blocks
//	
//	private int qorder = 2;
//	/**
//	 * Constructor. It inherits the writerBase constructor. However, this also copies the file "resource/python/ode.py" to the "generated" folder.
//	 * @param filename The file name is usually the system name.
//	 * @throws IOException An exception.
//	 */
//	public pythonQSHIOA(NetworkHIOA net) throws IOException {
//		super(net.getName());
//		// TODO: need to polish this part
//		copyFile(new File("resource/python/lib/__init__.py"), new File("generated/lib/__init__.py"));
//		copyFile(new File("resource/python/lib/edge.py"), new File("generated/lib/edge.py"));
//		copyFile(new File("resource/python/lib/equation.py"), new File("generated/lib/equation.py"));
//		copyFile(new File("resource/python/lib/location.py"), new File("generated/lib/location.py"));
//		
//		// write the main file
//		writeMain(net);
//		
//		// write the configuration file
//		
//		// Write a file for each QSHIOA
//		for (QSHIOA q: net.getQSHIOAs()) {
//			writeQSHIOA(q);
//		}
//	}
//	
//	private void writeMain(NetworkHIOA net) {
//		// create the main file
//		String mainFile = ROOT_PATH + "/" + mainName + ".py";
//		createFile(mainFile);
//		StringBuilder str = new StringBuilder();
//		int indent = 0;
//		
//		str.append(indentLine("#!/usr/bin/env python3", indent));
//		str.append(indentLine("# Original file: " + net.getFileName(), indent));
//		str.append("\r\n");
//		// Library calls 
//		for (QSHIOA q: net.getQSHIOAs()) {
//			str.append(indentLine("from " + q.getName() + " import *", indent));
//		}
//		str.append("\r\n");
//	
//		// Main function opening
//		str.append(indentLine("def main():", indent++));
//		str.append(indentLine("time = 0",indent));
//		str.append(indentLine("delta = set()", indent));
//		str.append(indentLine("qorder = " + qorder, indent));
//		str.append("\r\n");
//		
//		// Instantiation of every QSHIOA (initialization)
//		str.append(indentLine("# Initialization of each QSHIOA instance", indent));
//		for (QSHIOA q: net.getQSHIOAs()) {
//			str.append(indentLine("_" + q.getName() + " = " + q.getName() + "(" + qorder + ")", indent));
//		}
//		str.append("\r\n");
//		
//		// While loop starts
//		str.append(indentLine("while (time < " + this.simulationTime + "):", indent++));
//		// I/O exchange
//		str.append(indentLine("# Load inputs (I/O exchange)", indent));
//		for (Line l : net.getLines()) {
//			str.append(indentLine("_" + l.getDstBlockName() + ".I['" + l.getDstPortName() + "'][0] = "
//				+ "_" + l.getSrcBlockName() + ".O['" + l.getSrcPortName() + "'][0]", indent));
//		}
//		str.append("\r\n");
//		
//		// Inter-location transition
//		str.append(indentLine("# Inter-location transition", indent));
//		str.append(indentLine("flag = False", indent));
//		for (QSHIOA q: net.getQSHIOAs()) {
//			str.append(indentLine("flag = flag or _" + q.getName() + ".interLocation()", indent));
//		}
//		str.append(indentLine("if (flag):", indent++));
//		str.append(indentLine("continue # inter-location is executed (disable intra-location)", indent--));
//		str.append("\r\n");
//		
//		// Token exchange
//		str.append(indentLine("# Token exchange", indent));
//		str.append(indentLine("for i in range(1, qorder):", indent++));
//		for (QSHIOA q : net.getQSHIOAs()) {
//			str.append(indentLine("_" + q.getName() + ".setTokens(i)", indent));
//		}
//		for (Line l : net.getLines()) {
//			str.append(indentLine("_" + l.getDstBlockName() + ".I['" + l.getDstPortName() + "'][i] = "
//				+ "_" + l.getSrcBlockName() + ".O['" + l.getSrcPortName() + "'][i]", indent));
//		}
//		str.append("\r\n");
//	
//		// Obtain QSS
//		str.append(indentLine("# Compute QSS approximation and exchange Iq and Oq", --indent));
//		for (QSHIOA q : net.getQSHIOAs()) {
//			str.append(indentLine("_" + q.getName() + ".computeQSS(" + qorder + ")", indent));
//		}
//		for (Line l : net.getLines()) {			
//			str.append(indentLine("_" + l.getDstBlockName() + ".Iq['" + l.getDstPortName() + "'] = "
//				+ "_" + l.getSrcBlockName() + ".Oq['" + l.getSrcPortName() + "']", indent));
//		}		
//		str.append("\r\n");
//		
//		// Obtain delta
//		str.append(indentLine("# get delta", indent));
//		for (QSHIOA q : net.getQSHIOAs()) {
//			str.append(indentLine("delta.add(_" + q.getName() + ".getDelta())", indent));
//		}
//		str.append(indentLine("time_step = min(delta)", indent));
//		str.append("\r\n");
//	
//		// intra location transition
//		str.append(indentLine("# intra-location transition", indent));		
//		for (QSHIOA q: net.getQSHIOAs()) {
//			str.append(indentLine("_" + q.getName() + ".intraLocation(time_step)", indent));
//		}
//		str.append(indentLine("time = time + time_step", indent));
//		str.append("\r\n");
//		
//		// Main function call
//		str.append(indentLine("if __name__ == '__main__':", indent = 0));
//		str.append(indentLine("main()", ++indent));
//		
//		try {
//			write(mainFile, str.toString());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	/**
//	 * Sets the simulation time. If not explicitly set using this method, the simulation time is 60 seconds by default.
//	 * @param simt The maximum simulation time.
//	 */
//	public void setSimulationTime(int simt) {
//		this.simulationTime = simt;
//	}
//	
//	/**
//	 * @return Returns the saved error code.
//	 */
//	public int getErrorCode() {
//		return errorCode;
//	}
//	
//	/**
//	 * Internally used function that writes a python code for each QSHIOA.
//	 * @param qshioa QSAHIOA instance object.
//	 * @throws IOException An exception to be thrown.
//	 */
//	private void writeQSHIOA(QSHIOA qshioa) throws IOException {
//		// create a new file
//		String qfile = ROOT_PATH + "/" + qshioa.getName() + ".py";
//		createFile(qfile);
//		StringBuilder str = new StringBuilder();
//		int indent = 0;
//		
//		// Shebang 
//		str.append(indentLine("#!/usr/bin/env python3", indent));		
//		str.append(indentLine("#Converted block: " + qshioa.getName(), indent));
//		str.append("\r\n");
//		
//		// import calls 
//		str.append(indentLine("import sympy as S", indent));
//		str.append(indentLine("from lib.location import Location", indent));
//		str.append(indentLine("from lib.edge import Edge", indent));
//		str.append("\r\n");
//	
//		str.append(indentLine("# Variable symbols", indent));
//		// symbols
//		for (Variable v : qshioa.getContinuousVariables()) {
//			str.append(indentLine(v.getName() + " = S.sympify('" + v.getName()+ "(t)')", indent));
//		}
//		for (Variable v : qshioa.getInputs()) {
//			str.append(indentLine(v.getName() + " = S.sympify('" + v.getName()+ "(t)')", indent));			
//		}
//		
//		// Locations
//		str.append(indentLine("# Locations", indent));
//		for (Location l : qshioa.getLocations()) {
//			str.append(indentLine(l.getName() + " = Location(id = " + l.getID() + ")", indent));
//			// ODE
//			for (Formula f : l.getODEs()) {
//				str.append(indentLine(l.getName() + ".addODE('" + f.getSubject().getIntegralName() + "', S.sympify(" + getPythonEquation(f.getExpr()) +  "), qorder = " + qorder + ")", indent));
//			}
//			// Output update
//			for (Formula h : l.getOutputUpdateActions()) {
//				str.append(indentLine(l.getName() + ".addOutputUpdate('" + h.getSubject().getIntegralName() + "', S.sympify(" + getPythonEquation(h.getExpr()) + "))", indent));
//			}
//		}
//		
//		// Edges
//		str.append(indentLine("# Edges", indent));
//		int count = 0;
//		for (Transition t: qshioa.getTransitions()) {
//			String name = "edge_" + count;
//			str.append(indentLine(name + " = Edge(src_id = " + t.getSrc().getID() + ", dst_id = " + t.getDst().getID() + ")", indent));
//			// Guards
//			for (Formula f : t.getGuards()) {
//				String lvalue = getPythonEquation(f.getSubject().getName());
//				String rvalue = getPythonEquation(f.getExpr());
//				str.append(indentLine(name + ".addGuard(S.sympify(" + lvalue + "), '" + f.getRelation() + "', S.sympify(" + rvalue + "))", indent));  
//			}
//			// Resets
//			for (Formula f : t.getResets()) {				
//				String expr = getPythonEquation(f.getExpr());
//				str.append(indentLine(name + ".addReset('" + f.getSubject().getName() + "', S.sympify(" + expr + "))" ,indent));
//			}
//			// Connect edge to a location
//			str.append(indentLine(t.getSrc().getName() + ".addOutgoingEdge(" + name + ")", indent));
//		}
//		str.append("\r\n");
//		
//		// location mapping
//		str.append(indentLine("_Locations = {", indent=0));
//		for (Location l : qshioa.getLocations()) {
//			str.append(indentLine(l.getID() + " : " + l.getName() + ",", indent=1));
//		}
//		str.append(indentLine("}", --indent));
//		str.append("\r\n");
//		
//		// write the class definition opening
//		str.append(indentLine("class " + qshioa.getName() + ":", indent=0));
//		
//		// constructor
//		str.append(indentLine("def __init__(self, order=1):", indent=1));
//		str.append(indentLine("self.loc = " + qshioa.getInitialLocation().getID(), ++indent));
//		// input
//		if (qshioa.getInputVarCount() > 0) {
//			str.append(indentLine("self.I = {", indent++));
//			for (Variable i : qshioa.getInputs()) {
//				str.append(indentLine("\"" + i.getName() + "\" : [" + i.getInitialValue() + "],", indent));
//			}
//			str.append(indentLine("}", --indent));
//			str.append(indentLine("self.Iq = {", indent++));
//			for (Variable i : qshioa.getInputs()) {
//				str.append(indentLine("\"" + i.getName() + "\" : (0,0),", indent));
//			}			
//			str.append(indentLine("}", --indent));
//			for (Variable i : qshioa.getInputs()) {
//				str.append(indentLine("self.I['" + i.getName() + "'].extend([0 for i in range(order-1)])", indent));		
//			}
//		} else {
//			str.append(indentLine("self.I = {}", indent));
//			str.append(indentLine("self.Iq = {}", indent));
//		}
//		// output
//		if (qshioa.getOutputVarCount() > 0) {
//			str.append(indentLine("self.O = {", indent++));
//			for (Variable o : qshioa.getOutputs()) {
//				str.append(indentLine("\"" + o.getName() + "\" : [" + o.getInitialValue() + "],", indent));
//			}
//			str.append(indentLine("}", --indent));
//			str.append(indentLine("self.Oq = {", indent++));
//			for (Variable o : qshioa.getOutputs()) {
//				str.append(indentLine("\"" + o.getName() + "\" : (0,0),", indent));
//			}			
//			str.append(indentLine("}", --indent));
//			for (Variable o : qshioa.getOutputs()) {
//				str.append(indentLine("self.O['" + o.getName() + "'].extend([0 for i in range(order-1)])", indent));		
//			}
//		} else {
//			str.append(indentLine("self.O = {}", indent));
//			str.append(indentLine("self.Oq = {}", indent));
//		}
//		// continuous variables
//		if (qshioa.getContVarCount() > 0) {
//			str.append(indentLine("self.X = {", indent++));
//			for (Variable x : qshioa.getContinuousVariables()) {
//				str.append(indentLine("\"" + x.getName() + "\" : [" + x.getInitialValue() + "],", indent));
//			}
//			str.append(indentLine("}", --indent));
//			str.append(indentLine("self.Xq = {", indent++));
//			for (Variable x : qshioa.getContinuousVariables()) {
//				str.append(indentLine("\"" + x.getName() + "\" : (0,0),", indent));
//			}			
//			str.append(indentLine("}", --indent));
//			for (Variable x : qshioa.getContinuousVariables()) {
//				str.append(indentLine("self.X['" + x.getName() + "'].extend([0 for i in range(order-1)])", indent));		
//			}
//		} else {
//			str.append(indentLine("self.X = {}", indent));
//			str.append(indentLine("self.Xq = {}", indent));
//		}
//		str.append("\r\n");
//		
//		// initial value assignment
//		if (qshioa.getInitialization().size() > 0) {	
//			str.append(indentLine("# initial value assignment", indent));
//			for (Formula z : qshioa.getInitialization()) {
//				if (z.getSubject().getScope() == variableParam.Scope.OUTPUT_VARIABLE) {
//					// TODO: the right hand side expression may be not a value but an expression
//					str.append(indentLine("self.O[\"" + z.getSubject().getName() + "\"][0]=" + z.getExpr(), indent));
//				} 
//				else {				
//					str.append(indentLine("self.X[\"" + z.getSubject().getName() + "\"][0]=" + z.getExpr(), indent));
//				}
//			}
//		}
//		// entry action of the location
//		if (qshioa.getInitialLocation().getEntryActions().size() > 0) {
//			str.append(indentLine("# initial location entry actions", indent));
//			for (Formula z : qshioa.getInitialLocation().getEntryActions()) {
//				if (z.getSubject().getScope() == variableParam.Scope.OUTPUT_VARIABLE) {
//					// TODO: the right hand side expression may be not a value but an expression
//					str.append(indentLine("self.O[\"" + z.getSubject().getName() + "\"][0]=" + z.getExpr(), indent));
//				} 
//				else {				
//					str.append(indentLine("self.X[\"" + z.getSubject().getName() + "\"][0]=" + z.getExpr(), indent));
//				}
//			}
//		}
//		str.append(indentLine("_Locations[self.loc].updateOutput(self.X, self.O)", indent));
//		str.append("\r\n");
//		
//		// inter location function
//		str.append(indentLine("def interLocation(self):", indent = 1));
//		str.append(indentLine("flag, self.loc = _Locations[self.loc].anyEdgeEnabled(self.X, self.I) ", ++indent));
//		str.append(indentLine("return flag", indent));
//		str.append("\r\n");
//		
//		// updateTokens function
//		str.append(indentLine("def setTokens(self, rank):", indent = 1));
//		str.append(indentLine("_Locations[self.loc].updateTokens(self.X, self.I, self.O, rank)", ++indent));
//		str.append("\r\n");
//		
//		// computeQSS function
//		str.append(indentLine("def computeQSS(self, qorder):", indent = 1));
//		str.append(indentLine("self.Xq, self.Oq = _Locations[self.loc].getQssEquation(self.X, self.I, self.O, qorder)", ++indent));
//		str.append("\r\n");
//	
//		// get delta function
//		str.append(indentLine("def getDelta(self):", indent = 1));
//		str.append(indentLine("return _Locations[self.loc].getDelta(self.Xq, self.Iq)", ++indent));
//		str.append("\r\n");
//		
//		// intra location function
//		str.append(indentLine("def intraLocation(self, time):", indent = 1));
//		str.append(indentLine("_Locations[self.loc].updateContinuousVariables(self.X, self.Xq, time)", ++indent));
//		str.append(indentLine("_Locations[self.loc].updateOutput(self.X, self.O)", indent));
//		str.append("\r\n");
//		
//		// print funcdtion
//		str.append(indentLine("def printStates(self):", indent = 1));
//		str.append(indentLine("print(self.loc)", ++indent));
//		str.append(indentLine("print(self.I)", indent));
//		str.append(indentLine("print(self.O)", indent));
//		str.append(indentLine("print(self.X)", indent));
//		
//		try {
//			write(qfile, str.toString());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//	}
//	
//	/**
//	 * @param equation Equation written in the Matlab language
//	 * @return The corresponding equation in Python
//	 */
//	public String getPythonEquation(String equation) {
//		String newEquation = equation;
//		newEquation = newEquation.replace("power(", "pow("); // power() is convered into pow()
//		return newEquation;	
//	}
//
//}
