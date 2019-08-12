package com.simqss.main;
import java.io.IOException;
import java.util.zip.ZipException;

import org.conqat.lib.simulink.builder.SimulinkModelBuildingException;

import com.simqss.adapter.AdapterHIOA;
import com.simqss.structure.NetworkHIOA;
import com.simqss.adapter.AdapterJson;

/**
 * This is the main program
 * @author Jin Ro
 *
 */
public class SimQSS {
	
	public static void main(String[] args) throws ZipException, IOException, SimulinkModelBuildingException {
		for (String s : args) {
			System.out.println(args.toString());
		}
		
		// handling the input arguments
		String filename =  args[0];
		//String filename = "resource/collision.mdl";
		
		// Echo the input file name
		System.out.println("Target file : " + filename);
		
		// TODO: some interactive commands can be implemented here
		
		// Convert the simulink model, and receive the converted QSHIOAs as a system
		AdapterHIOA adapter = new AdapterHIOA(filename);
		NetworkHIOA system = adapter.getSystem();
		// Debugging purpose console printout
		System.out.println(system);
		
		//System.out.println(system.toJson());
	
		// Convert to JSON
		AdapterJson jsonAdapter = new AdapterJson();
		jsonAdapter.outputJsonFile(system);
		
		
			
	}
}
