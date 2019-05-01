package com.simqss.writer;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkPortBase;

// This class provides templates for the simulink block implmentation in python
public class BlockTemplate {
	public static final String Sum = "def sum_block(a, b):\r\n"
									+ "\treturn a+b\r\n\r\n" ;
	public static final String Constant = "def constant_block(a):\r\n"
									+ "\treturn a\r\n\r\n";
	
	public static String getTemplate(String type) {
		switch (type) {
			case "Sum":
				return Sum;
			case "Constant":
				return Constant;
			case "Scope":
				return "";
			default:
				System.out.println("Error: unknown Simulink block (" + type + ").\r\nProgram aborted.");
				System.exit(0);
				return "";
		}
	}
	
	public static String getFunctionDef(SimulinkBlock block) {
		String type = block.getType();
		String s="";
		switch (type) {
			case "Constant":
				s = "def " + block.getName() + "_1():\r\n";
				s += "\treturn constant_block(" + block.getParameter("Value") + ")\r\n\r\n";
				break;
			case "Sum":
				s = "def " + block.getName() + "_1():\r\n";
				String i1 = getPortName(block.getInPort("1").getLine().getSrcPort());
				String i2 = getPortName(block.getInPort("2").getLine().getSrcPort());
				s += "\treturn sum_block(" + i1  + "," + i2 + ")\r\n\r\n";
				break;
			default:
				break;
		}
		return s;
	}
	
	private static String getPortName(SimulinkPortBase line) {
		String src = line.getBlock().getName();
		String num = line.getIndex();
		return src + "_" + num + "()";
	}
}
