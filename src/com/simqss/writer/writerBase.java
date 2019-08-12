package com.simqss.writer;

import java.io.BufferedWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



/**
 * This is the writer base class. It creates the root folder, delete files, and write to a file.
 * @author Jin Woo Ro
 *
 */
public abstract class writerBase {
	protected static final String ROOT_PATH = "generated";
	protected String mainName;
	protected int errorCode = 0;
	
	/**
	 * Constructor. Creates the "generated" folder automatically. If it exists, then this folder is cleaned.
	 * @param filename Name of the main file.
	 * @throws IOException Exception if fail to create the folder or fail to clean the folder.
	 */
	public writerBase (String mainName) throws IOException  {
		this.mainName = mainName;
		File root = new File(ROOT_PATH);
		createFolder(root);
		File lib = new File(root, "lib");
		createFolder(lib);
	}
	
	/**
	 * Creates a new folder. If it already exist, then delete all the files in this folder.
	 * @param name Name of the folder to be created.
	 */
	protected void createFolder(File directory) {
		// check if "generated" folder remains from the previous program
		if (!directory.exists()) {
			// if does not exist, then create the folder
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
		// Wait until all the process completes
		while (!directory.exists());
	}
	
	/**
	 * @return Returns the error code.
	 */
	public int getErrorCode() {
		return errorCode;
	}
	
	/**
	 * A helper function for deleting a file or a folder.
	 * @param file
	 * @throws IOException
	 */
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

	/**
	 * A function that writes to a file.
	 * @param file The file name.
	 * @param contents The string contents to be written.
	 * @throws IOException
	 */
	protected void write(String file, String contents) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
		writer.write(contents);
		writer.close();
	}
	
	/**
	 * It creates a file.
	 * @param name The file name. This name includes the relative path.
	 */
	protected void createFile(String name) {
		File f = new File (name);
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to generate a file " + name);
		}
	}
	
	/**
	 * A helper function that format the indentation of the code.
	 * @param code The code line.
	 * @param indentation The tab count for the indentation.
	 * @return Returns the string including the indentation.
	 */
	protected String indentLine(String code, int indentation) {
		String line = code;
		for (int a = 0; a < indentation; a++) {
			line = "\t" + line;
		}
		line = line + "\r\n"; 
		return line; 
	}
	
	
	/**
	 * Copy a file in the destination path.
	 * @param source The source file (just the file name).
	 * @param dest The new file (just the file name).
	 * @throws IOException
	 */
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
}
