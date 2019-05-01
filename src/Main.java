import java.io.IOException;
import java.util.zip.ZipException;

import org.conqat.lib.simulink.builder.SimulinkModelBuildingException;

import com.pretzel.solver.SimulinkModelConvertor;
import com.pretzel.structure.NetworkQSHIOA;

public class Main {
	public static void main(String[] args) throws ZipException, IOException, SimulinkModelBuildingException {
		// the file name needs to be an input argument in the future
		String filename = "resource/example1.mdl";
		System.out.println("Target file : " + filename);
		
		// Convert the simulink model, and receive the converted QSHIOAs as a system
		SimulinkModelConvertor convertor = new SimulinkModelConvertor(filename);
		// TODO: need to check  the version of the Matlab?
		NetworkQSHIOA model = convertor.getSystem();
		
		
		
	}
}
