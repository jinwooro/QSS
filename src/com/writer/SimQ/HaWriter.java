package com.writer.SimQ;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.text.Highlighter.Highlight;

import org.conqat.lib.simulink.model.SimulinkBlock;

import com.structure.SimQ.SimQssHA;
import com.structure.SimQ.SimQssHALoc;

// it is recommended to use only one of this object class
public class HaWriter {
	public static final String PATH = "generated";
	public static final String CHART_FILE = "generated/charts.py";
	public static final String BLOCK_FILE = "generated/blocks.py";
	public static final String MAIN_FILE = "generated/main.py";
	
	public HaWriter (String filename, double simulationTime) throws IOException {
		File directory = new File(PATH);
		
		// check if "generated" folder exists
		if (!directory.exists()) {
			// if does not exist, then create the folder
			System.out.println("folder does not exist");
			try {
				directory.mkdir();
			} catch (SecurityException se) {
				se.printStackTrace();
				System.out.println("Cannot create the folder. Program aborted.");
				System.exit(0);
			}
		} else {
			// if the folder exist, then delete the files in the folder
			try {
				if (directory.list().length != 0) {
					delete(directory);
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Cannot delete the files in the generated folder. Program aborted.");
				System.exit(0);
			}
		}
		
		copyFile(new File("resource/python/ode.py"), new File("generated/ode.py"));
		makeFile("generated/__init__.py");
		makeFile(MAIN_FILE);
	
		write(MAIN_FILE, "#!/usr/bin/env python3\r\n\r\n" 
						+ "# This file is the conversion of " + filename + " to python\r\n" 
						+ "import simpy\r\n" + "import sympy as S\r\n" 
						+ "from ode import ODE\r\n"
						+ "from blocks import *\r\n\r\n"
						+ "def main():\r\n"
						+ "\tenv = simpy.Environment()\r\n"
						+ "\t#Here needs to run each HA\r\n"
						+ "\tenv.run(until=" + simulationTime + ")\r\n");
	}
	
	private void write(String f, String s) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(f, true));
		writer.write(s);
		writer.close();
	}
	
	public String getMainFileName() {
		return MAIN_FILE;
	}
	
	private void makeFile(String s) {
		File f = new File (s);
		try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Failed to generate " + s);
		}
	}
	
	private void copyFile(File source, File dest) throws IOException {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } finally {
	        is.close();
	        os.close();
	    }
	}
	
	private static void delete(File file) throws IOException {
		String files[] = file.list();
		for (String temp : files) {
			File f = new File(file.toString()+"/"+temp);
			if (f.isDirectory()) {
				delete(f); // delete all files
				f.delete(); // delete the folder
			} else {
				f.delete();
			}
		}
    }

	public void initBlockFile() {
		this.makeFile(BLOCK_FILE);
	}
	
	public void initChartFile() {
		this.makeFile(CHART_FILE);
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

	public void writeCharts(HashSet<SimQssHA> HAs) throws IOException {
		// For each HA object
		for (SimQssHA ha : HAs){
			// Open the class definition statement
			String s = "class " + ha.getName() + ":\r\n";
			
			// Local scope variables declaration
			s += "\t#ODE declarations\r\n";
			for (SimQssHALoc l :  ha.getLoc()) {
				for (String f : l.getf()) {
					String var = l.getLHS(f, true);
					s+= "\t" + l.getName() + "_ode_" + var + " = ODE(env, lvalue=S.sympify('diff(" + var + "))'), "
							+ "rvalue=S.sympify('" + l.getRHS(f) + "'), ttol=10**-2, iterations=1000)\r\n";
				}
			}
			
			// TODO: need to write codes for defining the ODE
			// E.g., loc1_ode_x = ODE(env, lvalue=S.sympify('diff(x(t))'), rvalue=S.sympify('v(t)'), ttol=10**-2, iterations=1000)
			
			s += "\tdef __init__(self):\r\n"
					+ "\t\tself.cstate = 0\r\n";
			s += "\t\t#Continuous variables X_C\r\n";
			for (Entry<String, Double> x : ha.getXC().entrySet()) {
				s += "\t\tself." + x.getKey() + " = " + x.getValue() + "\r\n";
			}
			s += "\t\t#Discrete variables X_D\r\n";
			for (Entry<String, Double> x : ha.getXD().entrySet()) {
				s += "\t\tself." + x.getKey() + " = " + x.getValue() + "\r\n";
			}
			s += "\t\t#Output variables O\r\n";
			for (Entry<String, Double> o : ha.getO().entrySet()) {
				s += "\t\tself." + o.getKey() + " = " + o.getValue() + "\r\n";
			}
			s += "\t\t#Input variables I\r\n";
			for (String i : ha.getI()) {
				s += "\t\tself." + i + "=0\r\n";
			}
			
			s+= "\r\n";
			write(CHART_FILE, s);
			
		}
	}
}
