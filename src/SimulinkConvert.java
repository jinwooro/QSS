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

	SimulinkUtils utils = new SimulinkUtils();
	private static final String defaultFilePath = "resource/example1.mdl";

	public static SimQssHA getHA(StateflowChart chart) {
		// Create a new HA instance with the chart name
		SimQssHA ha = new SimQssHA(chart.getName());
		
		
		// Extract the variable names and the initial values
		for (StateflowData data : chart.getData()) {
			String scope = data.getParameter("scope");
			switch (scope) {
				case "INPUT_DATA":
					ha.addVariable(data.getName(), 0);
					break;
				case "OUTPUT_DATA":
					break;
				case "LOCAL_DATA":
					break;
				default:
					System.out.println("Unknown scope variable is detected!!");
			}
				
			
			System.out.println("Variable parameter list: " + data.getDeclaredParameterNames());
		}
				
		// Extract location and edge information
		for (StateflowNodeBase n : chart.getNodes()) {
			ha.addLocation(n.getResolvedId().toString());
			// Extract transitions associated with each location
			for (StateflowTransition t : n.getInTransitions()) {
				if (t.getSrc() == null) {
					ha.setInitLoc(t.getDst().getResolvedId().toString().split("\\r?\\n")[0]);
				}
				else {
					String src = t.getSrc().getResolvedId().toString().split("\\r?\\n")[0];
					String dst = t.getDst().getResolvedId().toString().split("\\r?\\n")[0];
					ha.addEdge(src, dst, t.obtainLabelData().getText());
				}
			}
		}
		
		
		return ha;
	}
	
	public static void main(String[] args) throws SimulinkModelBuildingException, ZipException, IOException {
		File file = null;
		if (args.length == 0) {	file = new File("resource/example1.slx");
		} else {file = new File(args[0]);}
		System.out.println("Target file : " + file.getName());
		
		try (SimulinkModelBuilder builder = new SimulinkModelBuilder(file, new SimpleLogger())) {
	    	
	    	SimulinkModel model = builder.buildModel();
	    	HashSet<String> namesStateflowCharts = new HashSet<String>();
	    	HashMap<String, SimQssHA> HAs = new HashMap<String, SimQssHA>();
	    	HaWriter hw = new HaWriter(file.getName());

	    	
	    	// Iterate over the stateflow blocks first
	    	for (StateflowChart chart : model.getStateflowMachine().getCharts()) {
	    		namesStateflowCharts.add(chart.getName());
	    		
				System.out.println("***************HA generated: " + getHA(chart) + "\n");	
				
	    		
	    		// Add the location names
	    		for (StateflowNodeBase b : chart.getNodes()) {
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
				System.out.println("Parameter Value: " + block.getParameter("Value"));
	    	}
	      
			for (StateflowChart chart : model.getStateflowMachine().getCharts()) {
				System.out.println("Chart name: " + chart.getName());

				for (StateflowNodeBase b : chart.getNodes()) {
					String[] parts = b.getResolvedId().split("\\r?\\n");
					System.out.println("Full String: " + b.getResolvedId().toString());
					
					System.out.println("Nodes: " + parts[0]);
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
