package com.pretzel.solver.qss;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.text.Highlighter.Highlight;

import org.conqat.lib.simulink.model.SimulinkBlock;

import com.pretzel.structure.HIOA;
import com.pretzel.structure.Location;
import com.pretzel.structure.Transition;



// it is recommended to use only one of this object class
public class HaWriter {
	public static final String PATH = "generated";
	public static final String CHART_FILE = "generated/charts.py";
	public static final String BLOCK_FILE = "generated/blocks.py";
	public static final String ASSET_FILE = "generated/assets.py";
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
			
			// Variable initialization
			s += "\t#Internal continuous variables X_C\r\n";
			for (Entry<String, Double> x : ha.getXC().entrySet()) {
				s += "\t" + x.getKey() + " = " + x.getValue() + "\r\n";
			}
			s += "\t#Internal discrete variables X_D\r\n";
			for (Entry<String, Double> x : ha.getXD().entrySet()) {
				s += "\t" + x.getKey() + " = " + x.getValue() + "\r\n";
			}
			
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
}
