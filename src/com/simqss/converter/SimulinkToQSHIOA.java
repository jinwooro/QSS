package com.simqss.converter;

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
import org.conqat.lib.simulink.model.SimulinkLine;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.stateflow.StateflowChart;
import org.conqat.lib.simulink.model.stateflow.StateflowData;
import org.conqat.lib.simulink.model.stateflow.StateflowNodeBase;
import org.conqat.lib.simulink.model.stateflow.StateflowTransition;
import org.conqat.lib.simulink.util.SimulinkBlockRenderer;

import com.simqss.structure.automata.HIOA;
import com.simqss.structure.automata.QSHIOA;
import com.simqss.structure.basic.Formula;
import com.simqss.structure.basic.Location;
import com.simqss.structure.basic.Transition;
import com.simqss.structure.basic.Variable;
import com.simqss.structure.system.Line;
import com.simqss.structure.system.NetworkQSHIOA;
import com.simqss.utils.variableParam;

/**
 * This class converts a slx and mdl files into a network of QSHIOAs. This network of QSHIOAs is stored in an object instance called NetworkQSHIOA. 
 * @author Jin Woo Ro
 *
 */
public class SimulinkToQSHIOA extends SimulinkKeywords {
	// Simulink model components (being extracted)
	private File file = null;
	private SimulinkModel model = null;
	private HashSet<StateflowChart> charts = new HashSet<StateflowChart>();
	private HashSet<SimulinkBlock> blocks = new HashSet<SimulinkBlock>();
	private NetworkQSHIOA sys = null;
	
	/**
	 * Constructor.
	 * @param inputFileName The target file name.
	 * @throws ZipException An exception.
	 * @throws IOException An exception.
	 * @throws SimulinkModelBuildingException An exception.
	 */
	public SimulinkToQSHIOA(String inputFileName) throws ZipException, IOException, SimulinkModelBuildingException {
		file = new File(inputFileName);
		HashSet<String> namesStateflowCharts = new HashSet<String>(); // Set of chart names
		// Distinguish between Simulink Stateflow Charts and Simulink Blocks
		try (SimulinkModelBuilder builder = new SimulinkModelBuilder(file, new SimpleLogger())) {
			model = builder.buildModel();
			if (model.getStateflowMachine() != null) {
				for (StateflowChart chart : model.getStateflowMachine().getCharts()) {
		    		namesStateflowCharts.add(chart.getName());
		    		charts.add(chart);
		    	}
			}
			for (SimulinkBlock block : model.getSubBlocks()) {
	    		if (namesStateflowCharts.contains(block.getName())){
	    			continue;
	    		} else {
	    			blocks.add(block);
	    		}
	    	}
	    } 
		// TODO: extract the simulation time information and store it.
		// System model object instantiation with the name as the file name.
		sys = new NetworkQSHIOA(inputFileName);
		
		// Convert every chart -> QSHIOA 
		for (StateflowChart chart : charts) {
			QSHIOA q = makeQSHIOA(chart);
			sys.addQSHIOA(q);
		}
		
		// Convert each SimulinkLine -> a value assignment (namely just a line)
		for (StateflowChart chart : charts) {
			SimulinkBlock cb =  chart.getStateflowBlock();
			for (SimulinkLine sl : cb.getInLines()) {
				if (sl.getSrcPort() == null) {
					continue;
				}
				// TODO: we also ignore the connections from the simulink blocks
				String srcBlockName = sl.getSrcPort().getBlock().getName();
				if (hasChart(srcBlockName) == false) {
					continue;
				}
				
				Line line = makeLine(sl); 
				sys.addLine(line);
			}
		}
		// TODO: the Simulink block conversion is now disabled
		/*
		for (SimulinkBlock b : blocks) {
			for (SimulinkLine line : b.getInLines()) {
				if (line.getSrcPort() == null) {
					continue;
				}
				connections.add(line);
			}
		}
		*/
	}
	
	/**
	 * @return Returns the network of QSHIOAs.
	 */
	public NetworkQSHIOA getSystem() {
		return sys;
	}
	
	/**
	 * Converts the SimulinkLine into a simpler line for QSHIOA connections.
	 * @param sl A SimulinkLine instance.
	 * @return A QSHIOA line.
	 */
	private Line makeLine(SimulinkLine sl) {
			String srcBlockName = sl.getSrcPort().getBlock().getName();
			String dstBlockName = sl.getDstPort().getBlock().getName();
			String srcVariableName = "";
			String dstVariableName = "";
			if (hasChart(srcBlockName)) {
				srcVariableName = sl.getSrcPort().obtainLabelData().getText();
			} else {
				// if not a chart, then it must be a simulink block
				srcVariableName = "o" + sl.getParameter("SrcPort"); // give a dummy name
			}
			if (hasChart(dstBlockName)) {
				dstVariableName = sl.getDstPort().obtainLabelData().getText();
			} else {
				dstVariableName = "i" + sl.getParameter("DstPort"); // give a dummy name
			}
			Line line = new Line(srcBlockName, srcVariableName, dstBlockName, dstVariableName);
			return line;
	}
	
	/**
	 * @param name Checks if there is a charted with the requested name.
	 * @return Returns true if the chart is found, otherwise, false.
	 */
	private boolean hasChart(String name) {
		// return true if the requested name exists in the set of charts
		for (StateflowChart chart : charts) {
			if (chart.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Generate a png file that visualize the original Simulink model.
	 * @throws IOException
	 */
	public void drawDiagram() throws IOException {
		// render a block or model as PNG image
		BufferedImage image = SimulinkBlockRenderer.renderBlock(model);
		ImageIO.write(image, "PNG", new File(file.getName() + ".png"));
	}

	/**
	 * Converts a Simulink block into a QSHIOA.
	 * @param block The Simulink block instance.
	 * @return Returns the converted QSHIOA instance.
	 */
	private HIOA convertBlock_To_HIOA(SimulinkBlock block) {
		/*
		String BlockType = block.getType();
		HIOA hioa = new HIOA(block.getName());
		
		// This part is the transformation of each Simulink block into HIOA
		if (BlockType.equals("Sum")) {
			// Add I/O variable
			hioa.addInputVariable("i1");
			hioa.addInputVariable("i2");
			hioa.addOutputVariable("o1", 0);
			// Add location and the equation
			Location l = new Location("Sum");
			String signs = block.getParameter("Inputs");
			String expr = signs.charAt(0) + "i1" + signs.charAt(1) + "i2";
			Formula equation = new Formula(hioa.hasVariable("o1"), Symbol.Relation.EQUAL, expr);
			l.addOutputUpdateAction(equation);
			hioa.addLocation(l);
			// Set initialization
			hioa.makeEmptyInitialization("Sum");	
		} else if (BlockType.equals("Constant")) {	
			hioa.addOutputVariable("o1", 0);
			Location l = new Location("Const");
			String expr = block.getParameter("Value");
			Formula equation = new Formula(hioa.hasVariable("o1"), Symbol.Relation.EQUAL, expr);
			l.addOutputUpdateAction(equation);
			hioa.addLocation(l);
			hioa.makeEmptyInitialization("Const");
		} else if (BlockType.equals("Memory")) {
			// TODO: here all the other types of Simulink Blocks needs to be implemented
			
		}
		return hioa;
		*/
		return null;
	}
		
	/**
	 * This function implements the algorithm for translating a Stateflow chart into a QSHIOA
	 * @param chart A chart instance.
	 * @return Returns the converted QSHIOA
	 */
	private QSHIOA makeQSHIOA (StateflowChart chart) {
		// Extract the name
		QSHIOA qshioa = new QSHIOA(chart.getName());
		
		// Extract the variables
		for (StateflowData data : chart.getData()) {
			String scope = data.getParameter("scope");
			String variableName = data.getName();
			double initialValue = 0.0;
			// three variable scopes in Simulink
			switch (scope) {
				case LOCAL_DATA:
					if (data.getDeclaredParameterNames().contains(INITIAL_VALUE)) {
						initialValue = Double.parseDouble(data.getParameter(INITIAL_VALUE));
					}
					if (data.getDeclaredParameterNames().contains(VARIABLE_TYPE)) {
						if (data.getParameter(VARIABLE_TYPE).contentEquals(CONTINUOUS_DATA)) {
							qshioa.addContinuousVariable(variableName, initialValue);
						}
					} else {
						// TODO: here we assume that all the discrete variables are double type
						qshioa.addDiscreteVariable(variableName, initialValue, variableParam.Type.DOUBLE);
					}
					break;
				case OUTPUT_DATA:
					if (data.getDeclaredParameterNames().contains(INITIAL_VALUE)) {
						initialValue = Double.parseDouble(data.getParameter(INITIAL_VALUE));
					}
					// TODO: here shows how to fetch the port index
					qshioa.addOutputVariable(variableName, initialValue);
					break;
				case INPUT_DATA:
					qshioa.addInputVariable(variableName);
					break;
				default:
					System.out.println("Error: unknown scope variable is detected.");
					System.exit(0); // terminate the program
					break;
			}
		}
		
		
		// Extract the locations
		for (StateflowNodeBase n : chart.getNodes()) {
			int nodeId = Integer.parseInt(n.getId());
			String nodeContents = n.getResolvedId().toString();
			String[] lines = nodeContents.split("\\\\n|\\\\r|\\r|\\n");
			// extract the node name
			String locationName = lines[0].split("/")[lines[0].split("/").length-1];
			Location l = new Location(locationName);
			l.setID(nodeId);
			
			// TODO: improve differentiating f and o
			int currentMode = 0;
			for (int i = 1; i < lines.length; i++) {
				String line = lines[i].replace(";","").replaceAll("\\s","");
				if (line.contains(":")){
					if (line.contains("du") == true && line.contains("en") == false) {
						currentMode = 1; // du
						continue;
					} else if (line.contains("en") == true && line.contains("du") == false){
						currentMode = 2; // en
						continue;
					} else if (line.contains("en") && line.contains("du")) {
						currentMode = 3; // du, en
						continue;
					}
				} 
				
				Formula fx = Formula.makeFormula(line);
				// Check if the formula is invalid 
				if (fx == null) {
					System.out.println("Error: this equation is invalid: " + line);
					System.exit(0);
				}
				Variable x = qshioa.hasVariable(fx.getSubject().getName());
				if (x == null) {
					System.out.println("Error: this equation contains non-existing variables: " + line);
					System.exit(0);
				}
			
				if ((currentMode == 1) || (currentMode == 3)){ // this mode includes "du"
					if (x.getScope() == variableParam.Scope.LOCAL_CONTINUOUS_DERIVATIVE) {
						// if the subject variable is a derivative, the equation is an ODE
						l.addODE(fx);
					} else if ((x.getScope() == variableParam.Scope.OUTPUT_VARIABLE) 
							|| (x.getScope() == variableParam.Scope.OUTPUT_SIGNAL)) {
						l.addOutputUpdateAction(fx);
					} else {
						System.out.println("Error: the subject of any equation must be either a derivative or an output variable: " + line);
						System.exit(0);
					}
				} 
				if ((currentMode == 2) || (currentMode == 3)){
					if ((x.getScope() == variableParam.Scope.LOCAL_CONTINUOUS)
							|| (x.getScope() == variableParam.Scope.OUTPUT_VARIABLE)
							|| (x.getScope() == variableParam.Scope.OUTPUT_SIGNAL)) {
						l.addEntryAction(fx);
					} else {
						System.out.println("Error: entry ations at this location is invalid: " + line);
						System.exit(0);
					}
				}
			}
			qshioa.addLocation(l);
		}
		
		// Extract the transitions
		for (StateflowNodeBase n : chart.getNodes()) {
			for (StateflowTransition t : n.getInTransitions()) { 
				String contents = "";
				if (t.obtainLabelData() != null) {
					contents = t.obtainLabelData().getText();
				}
				// A transition that represent the initialization
				if (t.getSrc() == null) {
					int initialLocationId = Integer.parseInt(t.getDst().getId());
					Location loc = qshioa.getLocation(initialLocationId);
					qshioa.setInitialLocation(loc);
				
					if (contents.contains("[")) {
						System.out.println("Error: initialization should never be conditioned: " + contents);
					}
					
					Matcher m = Pattern.compile("\\{(.*?)\\}").matcher(contents);
					String resets[] = null;
					while(m.find()) {
						resets = m.group(1).replaceAll("\\\\n|\\\\r|\\r|\\n", "").split(";");
					}
					if (resets != null) {
						for (String equation: resets) {
							Formula fx = Formula.makeFormula(equation);
							qshioa.addInitialization(fx);
						}
					}
				}
				else {
					int srcLocId = Integer.parseInt(t.getSrc().getId());
					int dstLocId = Integer.parseInt(t.getDst().getId());
					Location srcLocName = qshioa.getLocation(srcLocId);
					Location dstLocName = qshioa.getLocation(dstLocId);
					Transition tran = new Transition(srcLocName, dstLocName);
					
					Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(contents);
					String guards[] = null;
					while(m.find()) {
						guards = m.group(1).replaceAll("\\\\n|\\\\r|\\r|\\n", "").split(";");
					}

					for (String equation: guards) {
						Formula fx = Formula.makeFormula(equation);
						tran.addGuard(fx);
					}
					m = Pattern.compile("\\{(.*?)\\}").matcher(contents);
					String resets[] = {};
					while(m.find()) {
						resets = m.group(1).replaceAll("\\\\n|\\\\r|\\r|\\n", "").split(";");
					}
					for (String equation: resets) {
						Formula fx = Formula.makeFormula(equation);
						tran.addReset(fx);
					}
					qshioa.addTransition(tran);
				}
			}
		}	
		return qshioa;
	}	
}