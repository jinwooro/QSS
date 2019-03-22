import java.io.IOException;
import java.util.zip.ZipException;

import org.conqat.lib.simulink.builder.SimulinkModelBuildingException;

import com.pretzel.solver.SimulinkModelConvertor;
import com.pretzel.structure.SystemModel;

public class Main {
	public static void main(String[] args) throws ZipException, IOException, SimulinkModelBuildingException {
		String filename = "resource/delays1.mdl";

		System.out.println("Target file : " + filename);
		SimulinkModelConvertor convertor = new SimulinkModelConvertor(filename);
		SystemModel model = convertor.getSystem();
		
		
		
	}
}
