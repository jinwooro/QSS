import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;

import javax.imageio.ImageIO;

import org.conqat.lib.commons.logging.SimpleLogger;
import org.conqat.lib.simulink.builder.SimulinkModelBuilder;
import org.conqat.lib.simulink.builder.SimulinkModelBuildingException;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.datahandler.ESimulinkBlockType;
import org.conqat.lib.simulink.model.datahandler.ModelDataHandler;
import org.conqat.lib.simulink.model.datahandler.simulink.SimulinkLayoutHandler;
import org.conqat.lib.simulink.util.SimulinkBlockRenderer;
import org.conqat.lib.simulink.util.SimulinkUtils;

import com.structure.SimQ.SimQssHA;
import com.writer.SimQ.HaWriter;

import org.conqat.lib.simulink.model.stateflow.StateflowBlock;
import org.conqat.lib.simulink.model.stateflow.StateflowChart;
import org.conqat.lib.simulink.model.stateflow.StateflowData;
import org.conqat.lib.simulink.model.stateflow.StateflowDeclContainerBase;
import org.conqat.lib.simulink.model.stateflow.StateflowMachine;
import org.conqat.lib.simulink.model.stateflow.StateflowNodeBase;
import org.conqat.lib.simulink.model.stateflow.StateflowTarget;
import org.conqat.lib.simulink.model.stateflow.StateflowTransition;

import java.util.HashMap;
import java.util.HashSet;

public class SimulinkConvert {
	// The program can accept two arguments
	// 1. file name, 2. simulation time
	public static void main(String[] args) throws SimulinkModelBuildingException, ZipException, IOException {
		File file = null;
		double simTime = 0;
		if (args.length == 0) {	
			file = new File("resource/example1.mdl");
			simTime = 10;
		} else if (args.length == 1) {
			file = new File(args[0]);
			simTime = 10;
		} else {
			file = new File(args[0]);
			simTime = Double.parseDouble(args[1]);
		}
		
		System.out.println("Target file : " + file.getName());
		HaWriter hw = new HaWriter(file.getName(), simTime);
		
		// Step 1: Extract information and sort them in HashSets
    	HashSet<String> namesStateflowCharts = new HashSet<String>();
    	HashSet<StateflowChart> charts = new HashSet<StateflowChart>();
    	HashSet<SimulinkBlock> blocks = new HashSet<SimulinkBlock>();
    	// Obtain stateflow charts and simulink blocks
		try (SimulinkModelBuilder builder = new SimulinkModelBuilder(file, new SimpleLogger())) {
	    	SimulinkModel model = builder.buildModel();
	    	// Obtain the list of Stateflow charts
	    	for (StateflowChart chart : model.getStateflowMachine().getCharts()) {
	    		namesStateflowCharts.add(chart.getName());
	    		charts.add(chart);
	    		// Consider using chart.getNodes() to extract the location information
	    	}
    	
	    	// Obtain the list of Simulink blocks
	    	for (SimulinkBlock block : model.getSubBlocks()) {
	    		if (namesStateflowCharts.contains(block.getName())){
	    			continue;
	    		} else {
	    			blocks.add(block);
	    		}
	    	}	      	      
	      // For debugging purpose, produce a png file for the Simulink model
	      drawModel(model, file);
	    }
		
		
		// Step 2: Translate charts and blocks into HAs
    	HashSet<SimQssHA> HAs = new HashSet<SimQssHA>();
		for (StateflowChart chart : charts) {
			SimQssHA ha = new SimQssHA(chart.getName());
			ha.convert(chart);
			HAs.add(ha);
			System.out.println(ha.toString());
		}
		
		// Step 3: Transpose all Simulink blocks into functions
		if (blocks.size() > 0) {
			hw.initBlockFile();
		}
		hw.writeBlocks(blocks);
		System.out.println("Program complted");
    	//hw.addLine(hw.MAIN_FILE, "\r\nif __name__ == '__main__':\r\n\tmain()");
	}
	
	public static void drawModel(SimulinkModel model, File file) throws IOException {
	      // render a block or model as PNG image
	      SimulinkBlockRenderer simulinkBlockRenderer = new SimulinkBlockRenderer();
	      BufferedImage image = simulinkBlockRenderer.renderBlock(model);
	      ImageIO.write(image, "PNG", new File("generated/" + file.getName() + ".png"));
	}
}
