package com.pretzel.reader.simulink;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipException;

import javax.imageio.ImageIO;

import org.conqat.lib.commons.logging.SimpleLogger;
import org.conqat.lib.simulink.builder.SimulinkModelBuilder;
import org.conqat.lib.simulink.builder.SimulinkModelBuildingException;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkInPort;
import org.conqat.lib.simulink.model.SimulinkLine;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.SimulinkOutPort;
import org.conqat.lib.simulink.model.SimulinkPortBase;
import org.conqat.lib.simulink.model.stateflow.StateflowChart;
import org.conqat.lib.simulink.model.stateflow.StateflowData;
import org.conqat.lib.simulink.model.stateflow.StateflowNodeBase;
import org.conqat.lib.simulink.model.stateflow.StateflowTransition;
import org.conqat.lib.simulink.util.SimulinkBlockRenderer;

import com.pretzel.structure.Block;
import com.pretzel.structure.HIOA;
import com.pretzel.structure.Location;
import com.pretzel.structure.Transition;

public class SimulinkReader {
	
	public static final String LOCAL_DATA = "LOCAL_DATA";
	public static final String OUTPUT_DATA = "OUTPUT_DATA";
	public static final String INPUT_DATA = "INPUT_DATA";
	public static final String CONTINUOUS_DATA = "SF_CONTINUOUS_TIME_DATA";
	
	private File file = null;
	private SimulinkModel model = null;
	private HashSet<StateflowChart> charts = new HashSet<StateflowChart>();
	private HashSet<SimulinkBlock> blocks = new HashSet<SimulinkBlock>();
	
	public SimulinkReader(String input_file_name) throws ZipException, IOException, SimulinkModelBuildingException {
		file = new File("resource/example1.mdl");
		HashSet<String> namesStateflowCharts = new HashSet<String>();
		try (SimulinkModelBuilder builder = new SimulinkModelBuilder(file, new SimpleLogger())) {
			model = builder.buildModel();
			for (StateflowChart chart : model.getStateflowMachine().getCharts()) {
	    		namesStateflowCharts.add(chart.getName());
	    		charts.add(chart);
	    	}
			for (SimulinkBlock block : model.getSubBlocks()) {
	    		if (namesStateflowCharts.contains(block.getName())){
	    			continue;
	    		} else {
	    			blocks.add(block);
	    		}
	    	}
	    }
	}
	
	public void drawDiagram() throws IOException {
		// render a block or model as PNG image
		BufferedImage image = SimulinkBlockRenderer.renderBlock(model);
		ImageIO.write(image, "PNG", new File(file.getName() + ".png"));
	}
	
	public HashSet<HIOA> extractHIOAs(){
		HashSet<HIOA> HIOAs = new HashSet<HIOA>();
		
		for (StateflowChart chart : charts) {
			// extract the name
			HIOA hioa = new HIOA(chart.getName());
			
			// extract the variables
			for (StateflowData data : chart.getData()) {
				String scope = data.getParameter("scope");
				double ival = 0.0;
				switch (scope) {
					case LOCAL_DATA:
						if (data.getDeclaredParameterNames().contains("props.initialValue")) {
							ival = Double.parseDouble(data.getParameter("props.initialValue"));
						}
						if (data.getDeclaredParameterNames().contains("props.updateMethod")) {
							if (data.getParameter("props.updateMethod").contentEquals(CONTINUOUS_DATA)) {
								hioa.addVariable(data.getName(), ival, HIOA.VarType.CONTINUOUS, 0); 
							}
						} else {
							hioa.addVariable(data.getName(), ival, HIOA.VarType.DISCRETE, 0);
						}
						break;
					case OUTPUT_DATA:
						if (data.getDeclaredParameterNames().contains("props.initialValue")) {
							ival = Double.parseDouble(data.getParameter("props.initialValue"));
						}
						int o_port = 0;
						for (SimulinkBlock b : chart.getStateflowBlock().getSubBlocks()) {
							if (data.getName().equals(b.getName())) {
								o_port = Integer.parseInt(b.getParameter("Port"));
							}
						}
						hioa.addVariable(data.getName(), ival, HIOA.VarType.OUTPUT, o_port);
						break;
					case INPUT_DATA:
						int i_port = 0;
						for (SimulinkBlock b : chart.getStateflowBlock().getSubBlocks()) {
							if (data.getName().equals(b.getName())) {
								i_port = Integer.parseInt(b.getParameter("Port"));
							}
						}
						hioa.addVariable(data.getName(), ival, HIOA.VarType.INPUT, i_port);
						break;
					default:
						System.out.println("Unknown scope variable is detected!!");
						break;
				}
			}
			
			// extract the transitions
			int init_id = 0;
			for (StateflowNodeBase n : chart.getNodes()) {
				for (StateflowTransition t : n.getInTransitions()) {
					String resets[] = null;
					String guards[] = null;
					if (t.getSrc() == null) {
						init_id = Integer.parseInt(t.getDst().getId());
						Transition tran = new Transition(-1, init_id);
						if (t.obtainLabelData() != null){
							Matcher m = Pattern.compile("\\{(.*?)\\}").matcher(t.obtainLabelData().getText());
							while(m.find())
								resets = m.group(1).replaceAll("\\\\n|\\\\r|\\r|\\n", "").split(";");
							tran.setReset(resets);
						}
						hioa.setInitialization(tran);
					}
					else {
						int src_id = Integer.parseInt(t.getSrc().getId());
						int dst_id = Integer.parseInt(t.getDst().getId());
						Transition tran = new Transition(src_id, dst_id);
						if (t.obtainLabelData() != null) {
							String label = t.obtainLabelData().getText();
							Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(label);
							while(m.find())
								guards = m.group(1).replaceAll("\\\\n|\\\\r|\\r|\\n", "").split(";");
							tran.setGuard(guards);
							m = Pattern.compile("\\{(.*?)\\}").matcher(label);
							while(m.find()) 
								resets = m.group(1).replaceAll("\\\\n|\\\\r|\\r|\\n", "").split(";");
							tran.setReset(resets);
						}
						hioa.addTransition(tran);
					}
				}
			}
			
			// extract the locations
			for (StateflowNodeBase n : chart.getNodes()) {
				int id = Integer.parseInt(n.getId());
				String contain = n.getResolvedId().toString();
				String[] lines = contain.split("\\\\n|\\\\r|\\r|\\n");
				
				Location l = new Location(lines[0]);
				l.setID(id);
				
				// TODO: improve identifying f and o
				int mode = 0;
				for (int i = 1; i < lines.length; i++) {
					String line = lines[i].replace(";","").replaceAll("\\s","");
					if (line.contains(":")){
						if (line.contains("du") == true && line.contains("en") == false) {
							mode = 1; // du
							continue;
						} else if (line.contains("en") == true && line.contains("du") == false){
							mode = 2; // en
							continue;
						} else if (line.contains("en") && line.contains("du")) {
							mode = 3; // du, en
							continue;
						}
					} 
					
					String LHS = line.split("=")[0];
					HIOA.VarType type = hioa.hasVariable(LHS);
					if ((mode == 1) || (mode == 3)){
						switch (type) {
							case DERIVATIVE:
								l.addODE(line);
								break;
							case OUTPUT:
								l.addOutputUpdate(line);
								break;
							default:
								break;
						}
					} 
					if ((mode == 2) || (mode == 3)){
						l.addExtraInit(line);
					}
				}
				hioa.addLocation(l);
			}
			
			Location init = hioa.getLocationByID(init_id);
			hioa.setInitialLocation(init);
			HIOAs.add(hioa);
		}
		
		return HIOAs;
	}
	
	public HashSet<Block> extractBlocks(){
		HashSet<Block> FBlock = new HashSet<Block>();
		
		for (SimulinkBlock b : blocks) {
			Block block = new Block(b.getName(), b.getType());
			FBlock.add(block);
		}
		
		return FBlock;
	}
}
	