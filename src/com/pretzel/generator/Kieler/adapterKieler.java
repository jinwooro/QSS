package com.pretzel.generator.Kieler;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.conqat.lib.simulink.model.SimulinkBlock;

import com.pretzel.generator.adapterBase;
import com.pretzel.structure.Block;
import com.pretzel.structure.HIOA;
import com.pretzel.structure.Line;
import com.pretzel.structure.Location;
import com.pretzel.structure.Transition;

public class adapterKieler extends adapterBase {
	
	public adapterKieler(String filename) throws IOException {
		super(filename);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void generateCode(HashSet<HIOA> HIOAs, HashSet<Block> blocks, HashSet<Line> lines) throws IOException {
		for (HIOA h : HIOAs) {
			String f = h.getName() + ".sctx";
			createFile(f);
			String code = translateChart(h);
			write(f, code);
		}
		
		HashSet<String> types = new HashSet<String>();
		for (Block b : blocks) {
			if (types.contains(b.getType())) {
				continue;
			}
			else {
				String f = b.getType() + ".sctx";
				createFile(f);
				write(f, translateBlock(b.getType()));
				types.add(b.getType());
			}
		}
		
		String main = "main.sctx";
		createFile(main);
		
		StringBuilder code = new StringBuilder();
		StringBuilder refCharts = new StringBuilder();
		StringBuilder refBlocks = new StringBuilder();
		for (HIOA h : HIOAs) {
			code.append("import " + "\"" + h.getName() + "\"\r\n");
			refCharts.append("\tref " + h.getName() + " " + h.getName() + "\r\n");
		}
		for (String t : types) {
			code.append("import " + "\"" + t + "\"\r\n");
			refBlocks.append("\tref " + t + " ");
			String comma = "";
			for (Block b : blocks) {
				if (t.equals(b.getType())) {
					refBlocks.append(comma).append(b.getName());
					comma = ",";
				}
			}
			refBlocks.append("\r\n");
		}
		code.append("\r\nscchart main {\r\n");
		code.append(refCharts.toString());
		code.append(refBlocks.toString());
		code.append("\tdataflow{\r\n"); 
		
		// Create a doctionary
		HashMap<String, HIOA> dictHIOA = new HashMap<String, HIOA>(); 
		for (HIOA h : HIOAs) {
			dictHIOA.put(h.getName(), h);
		}
		
		for (Line l : lines) {
			int SrcPortIndex = l.getSrcPortIndex();
			int DstPortIndex = l.getDstPortIndex();
			String SrcBlockName = l.getSrcBlockName();
			String DstBlockName = l.getDstBlockName();
			String SrcPortName = null;
			String DstPortName = null;
			if (dictHIOA.containsKey(SrcBlockName)) {
				HIOA h = dictHIOA.get(SrcBlockName);
				SrcPortName = SrcBlockName + "." + h.getOutputNameByPort(SrcPortIndex);
			} else {
				SrcPortName = SrcBlockName + ".O" + SrcPortIndex;
			}
			if (dictHIOA.containsKey(DstBlockName)) {
				HIOA h = dictHIOA.get(DstBlockName);
				DstPortName = DstBlockName + "." + h.getInputNameByPort(DstPortIndex);
			} else {
				DstPortName = DstBlockName + ".I" + DstPortIndex;
			}	
			
			code.append("\t\t" + DstPortName + " = " + SrcPortName + "\r\n");
		}
		code.append("\t}\r\n}");
		write(main, code.toString());
	}
	
	@Override
	protected String translateChart(HIOA h) {
		StringBuilder skeleton = new StringBuilder("scchart " + h.getName() + " {\r\n"
									+ "$INP$$OUT$$X_C$$X_D$\r\n$LOC$" + "\r\n}");

		// write all inputs
		int temp = 0;
		String comma = "";
		temp = skeleton.indexOf("$INP$");
		if (h.getI().size() > 0) {
			StringBuilder inputs = new StringBuilder("\tinput float ");
			for (String in : h.getI()) {
				inputs.append(comma).append(in);
				comma = ", ";
			}
			inputs.append("\r\n");
			skeleton.replace(temp, temp+5, inputs.toString());
		} else {
			skeleton.replace(temp, temp+5, "");
		}
			
		// write all outputs
		comma = "";
		temp = skeleton.indexOf("$OUT$");
		if (h.getO().size() > 0) {
			StringBuilder outputs = new StringBuilder("\toutput float ");
			for (Entry<String, Double> o : h.getO().entrySet()) {
				outputs.append(comma).append(o.getKey());
				comma = ", ";
			}
			outputs.append("\r\n");
			skeleton.replace(temp, temp+5, outputs.toString());
		} else {
			skeleton.replace(temp, temp+5, "");
		}
			
		// write all continuous internal variables X_C
		comma = "";
		temp = skeleton.indexOf("$X_C$");
		if (h.getXC().size() > 0) {
			StringBuilder xc = new StringBuilder("\tfloat ");
			for (Entry<String, Double> x : h.getXC().entrySet()) {
				xc.append(comma).append(x.getKey() + "," + x.getKey() + "_dot");
				comma = ", ";
			}
			xc.append("\r\n");
			skeleton.replace(temp, temp+5, xc.toString());
		} else {
			skeleton.replace(temp, temp+5, "");
		}
			
		// write X_D
		comma = "";
		temp = skeleton.indexOf("$X_D$");
		if (h.getXD().size() > 0) {
			StringBuilder xd = new StringBuilder("\tfloat ");
			for (Entry<String, Double> x : h.getXD().entrySet()) {
				xd.append(comma).append(x.getKey());
				comma = ", ";
			}
			xd.append("\r\n");
			skeleton.replace(temp, temp+5, xd.toString());
		} else {
			skeleton.replace(temp, temp+5, "");
		}
			
		// Locations
		comma = "";
		temp = skeleton.indexOf("$LOC$");
		StringBuilder loc = new StringBuilder("");
		for (Location l : h.getLocations()) {
			loc.append("\t");
			if (l.getName().equals(h.getInitialLocation().getName())) {
				loc.append("initial ");
			}
			loc.append("state " + l.getName() + " {\r\n");
			loc.append("\t\tduring do ");
			comma = "";
			for (String fx : l.getf()) {
				loc.append(comma).append(fx);
				comma = ";";
			}
			loc.append("\r\n\t\tduring do ");
			comma = "";
			for (String hx : l.geth()) {
				loc.append(comma).append(hx);
				comma = ";";
			}
			loc.append("\r\n\t}\r\n");
			
			int id = l.getID();
			for (Transition t : h.getTransitionsBySrc(id)) {
				int dstID = t.getDstId();
				String dst_name = h.getLocationByID(dstID).getName();
				loc.append("\t");
				if (t.getGuard().isEmpty() == false) {
					loc.append("if ");
					comma = "";
					for (String g : t.getGuard()) {
						loc.append(comma).append(g);
						comma = "&&";
					}
				}
				
				if (t.getReset().isEmpty() == false) {
					loc.append(" do ");
					comma = "";
					for (String r : t.getReset()) {
						loc.append(comma).append(r);
						comma = ";";
					}
				}
				loc.append(" go to " + dst_name + "\r\n");
			}
		}
		skeleton.replace(temp,  temp+5, loc.toString());
			
		return skeleton.toString();
	}

	
	protected String translateBlock(String type) {
		String template = null;
		if (type.equals("Constant")) {
			template = "scchart Constant { \r\n"
					+ "\toutput float O1 \r\n"
					+ "}\r\n";
		} else if (type.equals("Scope")) {
			template = "scchart Scope { \r\n"
					+ "\tinput float I1 \r\n"
					+ "}\r\n";
		} else if (type.equals("Sum")) {
			template = "scchart Scope { \r\n"
					+ "\tinput float I1, I2 \r\n"
					+ "\tinput float O1 \r\n"
					+ "}\r\n";
		}
		return template;
	}

	@Override
	protected String translateDataflow(Line l) {
		return null;
	}


}
