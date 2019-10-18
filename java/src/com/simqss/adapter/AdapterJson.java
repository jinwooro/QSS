package com.simqss.adapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.simqss.structure.NetworkHIOA;

import java.io.FileWriter;


public class AdapterJson {
	protected static final File outputFolder = new File("generated");
	
	/**
	 * Constructor. 
	 */
	public AdapterJson() {}
	
	/**
	 * Output the JSON file that represents the system. 
	 */
	public void outputJsonFile(NetworkHIOA system) {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.disableHtmlEscaping().create();
		String jsonResult = gson.toJson(system);
		
		// delete all the files in the output folder if this folder exists
		if (outputFolder.exists()) {
			try {
				if (outputFolder.list().length != 0) delete(outputFolder);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Cannot delete the files in the generated folder. Program aborted.");
				System.exit(0);
			}
		}
		
		// create a new output folder 
		createFolder(outputFolder);
		
		// generate the json file
        try (FileWriter file = new FileWriter("generated/" + system.getName() + ".json")) {
            file.write(jsonResult);
            file.flush();
            
            // copy the python script file to the generated folder
            //copyFile(new File("resource/python/qshioaConvertor.py"), new File("generated/qshioaConvertor.py"));
           
            /* This part is executing the python script automatically
            // execute the python script to convert HIOA -> QSHIOA
            String cmd = "python generated/qshioaConvertor.py " + system.getName() + ".json";
			Process p = Runtime.getRuntime().exec(cmd);
			
			BufferedReader normalMsg = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader errorMsg = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			
			System.out.println("Python program interaction:");
			String temp = null;
			while ((temp = normalMsg.readLine()) != null){
				System.out.println(temp);
			}
			
			while ((temp = errorMsg.readLine()) != null) {
				System.out.println(temp);
			}
			*/
        } catch (IOException e) {
            e.printStackTrace();
        }
          
	}
	
	/**
	 * Output the JSON file that represents the system and create the python file that can simulate the system. 
	 */
	public void outputRunnable() {
		File lib = new File(outputFolder, "lib");
		createFolder(lib);
	}
	
	/**
	 * A helper function that creates a folder.
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
		} 
		// Wait until all the process completes
		while (!directory.exists());
	}
	
	/**
	 * A helper function that deletes a file. 
	 * If a folder name is the input, then it deletes all the files in this folder.
	 * @param file
	 * @throws IOException
	 */
	protected static void delete(File file) throws IOException {
		String files[] = file.list();
		for (String temp : files) {
			File f = new File(file.toString()+"/"+temp);
			if (f.isDirectory()) {
				delete(f); 
				f.delete(); 
			} else {
				f.delete(); 
			}
		}
    }
	
	/**
	 * A helper function that copies a file or files.
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
