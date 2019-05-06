package com.simqss.program;
import java.io.IOException;
import java.util.zip.ZipException;

import org.conqat.lib.simulink.builder.SimulinkModelBuildingException;

import com.simqss.converter.SimulinkToQSHIOA;
import com.simqss.structure.system.NetworkQSHIOA;
import com.simqss.writer.python.pythonQSHIOA;

public class SimQSS {
	
	public static void main(String[] args) throws ZipException, IOException, SimulinkModelBuildingException {
		
		for (String s : args) {
			System.out.println(args.toString());
		}
		
		// the file name needs to be an input argument in the future
		String filename = "resource/onlySF1.mdl";
		System.out.println("Target file : " + filename);
		
		// TODO: some interactive commands can be implemented here
		
		// Convert the simulink model, and receive the converted QSHIOAs as a system
		SimulinkToQSHIOA convert = new SimulinkToQSHIOA(filename);
		NetworkQSHIOA network = convert.getSystem();
		// Debugging purpose console printout
		System.out.println(network);
	
		pythonQSHIOA pq = new pythonQSHIOA(network);
		// TODO: int simTime = convert.getSimulationTime();
		pq.setSimulationTime(30);
		
	}
	
}
