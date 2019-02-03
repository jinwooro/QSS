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

import org.conqat.lib.simulink.model.stateflow.StateflowBlock;
import org.conqat.lib.simulink.model.stateflow.StateflowChart;
import org.conqat.lib.simulink.model.stateflow.StateflowMachine;
import org.conqat.lib.simulink.model.stateflow.StateflowNodeBase;
import org.conqat.lib.simulink.model.stateflow.StateflowTransition;

import java.util.HashMap;
import java.util.HashSet;

public class SimulinkConvert {

	public static void main(String[] args) throws SimulinkModelBuildingException, ZipException, IOException {
	    File file = new File("resource/example1.slx");
	    try (SimulinkModelBuilder builder = new SimulinkModelBuilder(file, new SimpleLogger())) {
	    	 
	    	SimulinkModel model = builder.buildModel();
	    	HashSet<String> namesStateflowCharts = new HashSet<String>();
	    	SimulinkUtils utils = new SimulinkUtils();
	    	HashMap<String, SimQssHA> HAs = new HashMap<String, SimQssHA>();
	    	
	    	// Iterate over the stateflow blocks first
	    	for (StateflowChart chart : model.getStateflowMachine().getCharts()) {
	    		namesStateflowCharts.add(chart.getName());
	    		SimQssHA ha = new SimQssHA(chart.getName(), "Stateflow");
	    		
	    		// Add the location names
	    		for (StateflowNodeBase b : chart.getNodes()) {
	    			String[] parts = b.getResolvedId().split("\\r?\\n");
	    			ha.addLocation(parts[0]);
	    			// TODO: add f and h
	    			// How to filter the string is the problem
		    	} 
	    		
		    }
	    	
	    	// Extracting information of the Simulink blocks
	    	// Name, type, inputs, outputs
	    	for (SimulinkBlock block : model.getSubBlocks()) {
				System.out.println("Name: " + block.getName());
				System.out.println("Type: " + block.getType());
				System.out.println("Input ports: " + block.getInPorts());
				System.out.println("Output ports: " + block.getOutPorts());
				System.out.println("Parameter Value: " + block.obtainLabelData().getText());
	    	}
	      
	      // Extracting information of the stateflow charts. 
	      
	      for (StateflowChart chart : model.getStateflowMachine().getCharts()) {
	    	  System.out.println("Chart name: " + chart.getName());
	    	  //System.out.println("Nodes: " + chart.getNodes().toString());
	    	  for (StateflowNodeBase b : chart.getNodes()) {
	    		  String[] parts = b.getResolvedId().split("\\r?\\n");
	    		  System.out.println("Nodes: " + parts[0]);
	    	  }
	    		  
	    	  for (StateflowTransition trans : utils.getAllTransitions(chart)) {
	    		  System.out.println("Transition Text:" + trans.obtainLabelData().getText());
	    	  }
	      }
	      	      
	      
	      // Distinguishing the duplicated reading process for stateflow chart
	      // It is read as a simulink block (subsystem) while also detected as a stateflow chart
	      // Thus, it is read twice. Use the name to find the intersection between simulink blocks and stateflow charts.
	      HashSet<String> namesSimulinkBlocks = new HashSet<String>();
	      
	      for (SimulinkBlock block : model.getSubBlocks()) {
	    	  namesSimulinkBlocks.add(block.getName());
	      }
	      for (StateflowChart chart : model.getStateflowMachine().getCharts()) {
	    	  namesStateflowCharts.add(chart.getName());
	    	  namesSimulinkBlocks.remove(chart.getName());
	      }
	      System.out.println("Simulink blocks:"+ namesSimulinkBlocks.toString() + " || Stateflow charts:" + namesStateflowCharts.toString());
	            
	      
	      // render a block or model as PNG image
	      SimulinkBlockRenderer simulinkBlockRenderer = new SimulinkBlockRenderer();
	      BufferedImage image = simulinkBlockRenderer.renderBlock(model);
	      ImageIO.write(image, "PNG", new File(file.getPath() + ".png"));
	    }
	  }
}
