import java.io.IOException;
import java.util.zip.ZipException;

import org.conqat.lib.simulink.builder.SimulinkModelBuildingException;

import com.simqss.converter.SimulinkToQSHIOA;
import com.simqss.structure.system.NetworkQSHIOA;

public class Main {
	public static void main(String[] args) throws ZipException, IOException, SimulinkModelBuildingException {
		// the file name needs to be an input argument in the future
		String filename = "resource/onlySF1.mdl";
		System.out.println("Target file : " + filename);
		
		// TODO: some interactive commands can be implemented here
		
		// Convert the simulink model, and receive the converted QSHIOAs as a system
		SimulinkToQSHIOA convert = new SimulinkToQSHIOA(filename);
		NetworkQSHIOA network = convert.getSystem();
		// Debugging purpose console printout
		System.out.println(network);
	
		// 
		
		
	}
}
