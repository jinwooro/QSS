package com.simqss.writer.python;

import java.io.File;
import java.io.IOException;

import com.simqss.structure.automata.QSHIOA;
import com.simqss.structure.basic.Variable;
import com.simqss.structure.system.Line;
import com.simqss.structure.system.NetworkQSHIOA;
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
		str.append(indentLine("while (time < " + this.simulationTime + "):", indent++));
		
		
		// Instantiation of every QSHIOA (initialization)
		str.append(indentLine("# Initialization of each QSHIOA instance", indent));
		for (QSHIOA q: net.getQSHIOAs()) {
			str.append(indentLine("#" + q.getName() + " = " + q.getName() + "()", indent));
		}
		str.append("\r\n");
		
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
		
		// Input and output conneciton
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
		str.append(indentLine("main()", indent--));
		
		try {
			write(mainFile, str.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	 * @param qshioa
	 * @throws IOException
	 */
	
	/*
	public boolean writeQSHIOA(QSHIOA qshioa) throws IOException {
		// create a new file
		createFile(q.getName() + ".py");
		
		// create the python code 
		String buff = "";
		for (HIOA ha : HIOAs) {
			buff += "class " + ha.getName() + ":\r\n";
			
			// Internal variable initialization
			buff += "\t#Internal continuous variables X_C\r\n";
			for (Entry<String, Double> x : ha.getXC().entrySet()) {
				buff += "\t" + x.getKey() + " = " + x.getValue() + "\r\n";
			}
			buff += "\t#Internal discrete variables X_D\r\n";
			for (Entry<String, Double> x : ha.getXD().entrySet()) {
				buff += "\t" + x.getKey() + " = " + x.getValue() + "\r\n";
			}
			
			write(CHART_FILE, buff);
			buff = "";
		}
		
		return true;
	}
	

	public void writeBlocks(HashSet<SimulinkBlock> blocks) throws IOException {
		HashSet<String> types = new HashSet<>();
		for (SimulinkBlock block : blocks) {
			types.add(block.getType());
			String s = BlockTemplate.getFunctionDef(block);
			write(BLOCK_FILE, s);
		}
		for (String type : types) {
			String func = BlockTemplate.getTemplate(type);
			if (!func.isEmpty()) {
				write(BLOCK_FILE, func);	
			}
		}
		
	}
		//System.out.println("Name: " + block.getName());
		//System.out.println("Type: " + block.getType());
		//System.out.println("Input ports: " + block.getInPorts());
		//System.out.println("Output ports: " + block.getOutPorts());
		//System.out.println("Parameter Value: " + block.getParameter("Value"));
	
	
	public void writeCharts(HashSet<HIOA> HAs) throws IOException {
		String s = "";		
		// ODEs are stored in one class
		s = "class ODEs:\r\n\t#ODE declaration\r\n";
		for (HIOA ha: HAs) {
			// ODE declaration
			// Each ODE has a name syntax: $loc_ode_$var
			for (Location l :  ha.getLocations()) {
				for (String f : l.getf()) {
					String var = l.getLHS(f, true);
					s+= "\t" + l.getName() + "_ode_" + var + " = ODE(env, lvalue=S.sympify('diff(" + var + "))'), "
							+ "rvalue=S.sympify('" + l.getRHS(f) + "'), ttol=10**-2, iterations=1000)\r\n";
				}
			}
		}
		write(ASSET_FILE, s);
		
		// Output variables are created
		s = "\r\nclass Outputs:\r\n\t#Output variables\r\n";
		for (HIOA ha: HAs) {
			Iterator it = ha.getO().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry)it.next();
				s += "\t" + pair.getKey() + " = " + pair.getValue() + "\r\n";
			}
		}
		write(ASSET_FILE, s);
		
		// Each HA is a class in charts.py file
		for (HIOA ha : HAs){
			s = "class " + ha.getName() + ":\r\n";
			

			
			// Location dictionary
			s += "\r\n\tloc = {\r\n\t\t" + ha.getInitLocID() + ": " + ha.getInitLocName() + "\r\n";
			for (Location l : ha.getLocations()) {
				if (l.getID() == ha.getInitLocID()) {
					// skip the initial location
					continue;
				}
				s += "\t\t" + l.getID() + ": " + l.getName() + "\r\n";
			}
			s+= "\t}\r\n";
			
			// Constructor that initiate the cstate variable
			s += "\r\n\tdef __init__(self):\r\n"
					+ "\t\tself.cstate = " + ha.getInitLocID() + " \r\n\r\n";
			
			// Each location is a method
			for (Location l : ha.getLocations()) {
				s += "\tdef " + l.getName() + "(self):\r\n";
				s += "\t\t# Check guard conditions before invariants\r\n"; 
				for (Transition t : ha.getTransitionsBySrc(l.getID())) {
					s += "\t\tif (" + t.getGuardString() + "):\r\n";
					// Write reset assignments
					for (String r : t.getReset()) {
						s += "\t\t\t" + r + "\r\n";
					}
					s += "\t\t\tself.cstate = " + t.getDstId() + "\r\n";
				}
				
				s += "\t\t# Check Invariants\r\n"
					+ "\t\tif (" + l.getInvariantString() + "):\r\n";
				
				// write f and h vectors
				for (String h : l.geth()) {
					
				}
				
			}
			
			
			s += "\r\n\tdef run(self):\r\n" + "\t\tdelta = loc[self.cstate]()\r\n" + "\t\treturn delta\r\n";
			
			s+= "\r\n";
			write(CHART_FILE, s);
		}
	}
	*/
}
