package com.simqss.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;

import com.simqss.structure.automata.HIOA;
import com.simqss.structure.system.Line;


public abstract class writerBase {
	// all the generated files will be located in the "generated" folder
	protected static final String PATH = "generated";
	protected String filename;
	
	public writerBase (String filename) throws IOException {
		File directory = new File(PATH);
		this.filename = filename;
		
		// check if "generated" folder remains from the previous writing
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
	}

	protected static void delete(File file) throws IOException {
		String files[] = file.list();
		for (String temp : files) {
			File f = new File(file.toString()+"/"+temp);
			if (f.isDirectory()) {
				delete(f); // recursion for deleting each file in this folder
				f.delete(); // after the recursion, delete this folder
			} else {
				f.delete(); // if this is just a file, then just delete this file
			}
		}
    }

	protected void write(String f, String s) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(PATH + "/" + f, true));
		writer.write(s);
		writer.close();
	}
	
	protected void createFile(String s) {
		File f = new File (PATH + "/" + s);
		try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Failed to generate a file " + s);
		}
	}
	
	
	protected void copyFile(File source, File dest) throws IOException {
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
	
	// these methods are for the implementation
	protected abstract String translateChart(HIOA h);
	protected abstract String translateBlock(String type);
	protected abstract String translateDataflow(Line l);
	//public abstract void generateCode(HashSet<HIOA> HIOAs, HashSet<Block> blocks, HashSet<Line> lines) throws IOException;
	
}
