package com.writer.SimQ;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.assertj.core.util.Files;

import net.bytebuddy.asm.Advice.This;

// it is recommended to use only one of this object class
public class HaWriter {
	private static final String path = "generated";
	private static final String blocks = "generated/blocks.py";
	private static final String mainFile = "generated/main.py";
	
	public HaWriter (String filename) throws IOException {
		File directory = new File(path);
		
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
		
		this.copyFile(new File("resource/python/ode.py"), new File("generated/ode.py"));
		this.makeFile("generated/__init__.py");
		this.makeFile(blocks);
		this.makeFile(mainFile);
	
		this.addLine(mainFile, "#!/usr/bin/env python3\r\n\r\n" 
						+ "# This file is the conversion of " + filename + " to python\r\n" 
						+ "import simpy\r\n" + "import sympy as S\r\n" 
						+ "from ode import ODE\r\n"
						+ "from blocks import *\r\n\r\n");
	}
	
	public void addLine(String f, String s) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(f, true));
		writer.write(s);
		writer.close();
	}
	
	public String getMainFileName() {
		return mainFile;
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
}
