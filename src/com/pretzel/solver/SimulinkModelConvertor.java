package com.pretzel.solver;

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
import org.conqat.lib.simulink.model.SimulinkPortBase;
import org.conqat.lib.simulink.model.stateflow.StateflowChart;
import org.conqat.lib.simulink.model.stateflow.StateflowData;
import org.conqat.lib.simulink.model.stateflow.StateflowNodeBase;
import org.conqat.lib.simulink.model.stateflow.StateflowTransition;
import org.conqat.lib.simulink.util.SimulinkBlockRenderer;

import com.pretzel.structure.BlockInterface;
import com.pretzel.structure.Line;
import com.pretzel.structure.Port;
import com.pretzel.structure.automata.HIOA;
import com.pretzel.structure.automata.QSHIOA;
import com.pretzel.structure.NetworkQSHIOA;
import com.pretzel.structure.basic.Formula;
import com.pretzel.structure.basic.Location;
import com.pretzel.structure.basic.Transition;
import com.pretzel.structure.basic.Variable;
import com.pretzel.structure.enums.Symbol;
import com.pretzel.structure.enums.variableParam;

public class SimulinkModelConvertor {
	// Simulink model parameters
	public static final String LOCAL_DATA = "LOCAL_DATA";
	public static final String OUTPUT_DATA = "OUTPUT_DATA";
	public static final String INPUT_DATA = "INPUT_DATA";
	public static final String CONTINUOUS_DATA = "SF_CONTINUOUS_TIME_DATA";
	public static final String INITIAL_VALUE = "props.initialValue";
	public static final String VARIABLE_TYPE = "props.updateMethod";
	
	// Simulink model components (being extracted)
	private File file = null;
	private SimulinkModel model = null;
	private HashSet<StateflowChart> charts = new HashSet<StateflowChart>();
	private HashSet<SimulinkBlock> blocks = new HashSet<SimulinkBlock>();
	
	// Our system as a structure
	private NetworkQSHIOA sys = null;

	
	public SimulinkModelConvertor(String inputFileName) throws ZipException, IOException, SimulinkModelBuildingException {
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
		
		// System model object instantiation with the name as the file name.
		NetworkQSHIOA sys = new NetworkQSHIOA(inputFileName);	
		
		// Convert every chart -> QSHIOA 
		for (StateflowChart chart : charts) {
			QSHIOA q = makeQSHIOA(chart);
			sys.addQSHIOA(q);
		}
		
		// Extract the information of the connections between the components
		createLines();
	
		// for debugging purpose
		System.out.println(sys);
	}
	
	public NetworkQSHIOA getSystem() {
		return sys;
	}
	
	private void createLines() {
		HashSet<SimulinkLine> allLines = new HashSet<SimulinkLine>();
		// it is sufficient to consider only the input lines (because the outgoing lines are
		// essentially input lines of another block.
		for (StateflowChart chart : charts) {
			SimulinkBlock cb =  chart.getStateflowBlock();
			for (SimulinkLine line : cb.getInLines()) {
				allLines.add(line);
			}
		}
		
		for (SimulinkBlock b : blocks) {
			for (SimulinkLine line : b.getInLines()) {
				allLines.add(line);
			}
		}
		
		for (SimulinkLine l : allLines) {
			String srcBlock = l.getSrcPort().getBlock().getName();
			String dstBlock = l.getDstPort().getBlock().getName();
			String srcVar = "";
			String dstVar = "";
			if (isChart(srcBlock)) {
				srcVar = l.getSrcPort().obtainLabelData().getText();
			} else {
				srcVar = "o" + l.getParameter("SrcPort");
			}
			if (isChart(dstBlock)) {
				dstVar = l.getDstPort().obtainLabelData().getText();
			} else {
				dstVar = "i" + l.getParameter("DstPort");
			}
			Port srcPort = sys.getBlockInterfaceByName(srcBlock).getOutPortByName(srcVar);
			Port dstPort = sys.getBlockInterfaceByName(dstBlock).getInPortByName(dstVar);
			Line line = new Line(srcBlock, srcPort, dstBlock, dstPort);
			
			sys.addLine(line);
		}
		
	}
	
	private boolean isChart(String name) {
		// return true if the requested name exists in the set of charts
		for (StateflowChart chart : charts) {
			if (chart.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public void drawDiagram() throws IOException {
		// render a block or model as PNG image
		BufferedImage image = SimulinkBlockRenderer.renderBlock(model);
		ImageIO.write(image, "PNG", new File(file.getName() + ".png"));
	}

	private HIOA convertBlock_To_HIOA(SimulinkBlock block) {
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
	}
		
	// This function implements the algorithm for translating a Stateflow chart into a QSHIOA
	private QSHIOA makeQSHIOA (StateflowChart chart) {
		// Extract the name
		QSHIOA hioa = new QSHIOA(chart.getName());
		
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
							hioa.addContinuousVariable(variableName, initialValue);
						}
					} else {
						// TODO: here we assume that all the discrete variables are double type
						hioa.addDiscreteVariable(variableName, initialValue, variableParam.Type.DOUBLE);
					}
					break;
				case OUTPUT_DATA:
					if (data.getDeclaredParameterNames().contains(INITIAL_VALUE)) {
						initialValue = Double.parseDouble(data.getParameter(INITIAL_VALUE));
					}
					// TODO: here shows how to fetch the port index
					hioa.addOutputVariable(variableName, initialValue);
					break;
				case INPUT_DATA:
					hioa.addInputVariable(variableName);
					break;
				default:
					System.out.println("Error: unknown scope variable is detected.");
					System.exit(0); // terminate the program
					break;
			}
		}
		
		
		// extracting the locations
		for (StateflowNodeBase n : chart.getNodes()) {
			int nodeId = Integer.parseInt(n.getId());
			String nodeContents = n.getResolvedId().toString();
			String[] lines = nodeContents.split("\\\\n|\\\\r|\\r|\\n");
			
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
				if (fx == null) {
					System.out.println("Error: equation cannot be understood: " + line);
					System.exit(0);
				}
				// Check if the subject variable exists in the HIOA instance
				Variable x = hioa.hasVariable(fx.getSubject().getName());
				if (x == null) {
					System.out.println("Error: equation contains non-existing variable: " + line);
					System.exit(0);
				}
				fx.setSubject(x);
			
				if ((currentMode == 1) || (currentMode == 3)){ // this mode includes "du"
					if (x.getScope() == variableParam.Scope.LOCAL_CONTINUOUS_DERIVATIVE) {
						l.addODE(fx);
					} else if ((x.getScope() == variableParam.Scope.OUTPUT_VARIABLE) 
							|| (x.getScope() == variableParam.Scope.OUTPUT_SIGNAL)) {
						l.addOutputUpdateAction(fx);
					} else {
						System.out.println("Error: the subject variable in the equations in a location " 
										+ "is strictly restricted to either the continuous local variable "
										+ "or the output variable: " + line);
						System.exit(0);
					}
				} 
				if ((currentMode == 2) || (currentMode == 3)){
					if ((x.getScope() == variableParam.Scope.LOCAL_CONTINUOUS)
							|| (x.getScope() == variableParam.Scope.OUTPUT_VARIABLE)
							|| (x.getScope() == variableParam.Scope.OUTPUT_SIGNAL)) {
						l.addEntryAction(fx);
					} else {
						System.out.println("Error: entry ations at a location should have the subject varible "
											+ "either local continuous or output: " + line);
						System.exit(0);
					}
				}
			}
			hioa.addLocation(l);
		}
		
		// extracting the transitions
		for (StateflowNodeBase n : chart.getNodes()) {
			for (StateflowTransition t : n.getInTransitions()) {
				if (t.getSrc() == null) {
					int initialLocationId = Integer.parseInt(t.getDst().getId());
					Location loc = hioa.getLocation(initialLocationId);
					hioa.setInitialLocation(loc);
					Transition initialization = new Transition(null,loc);
					
					// TODO: currently we assume that the initialization transition does not include [] brackets
					if (t.obtainLabelData() != null){
						Matcher m = Pattern.compile("\\{(.*?)\\}").matcher(t.obtainLabelData().getText());
						String resets[] = null;
						while(m.find()) {
							resets = m.group(1).replaceAll("\\\\n|\\\\r|\\r|\\n", "").split(";");
						}
						for (String equation: resets) {
							Formula fx = Formula.makeFormula(equation);
							initialization.addReset(fx);
						}
					}
					hioa.setInitialization(initialization);
				}
				else {
					int srcLocId = Integer.parseInt(t.getSrc().getId());
					int dstLocId = Integer.parseInt(t.getDst().getId());
					Location srcLoc = hioa.getLocation(srcLocId);
					Location dstLoc = hioa.getLocation(dstLocId);
					
					Transition tran = new Transition(srcLoc, dstLoc);
					
					if (t.obtainLabelData() != null) {
						String label = t.obtainLabelData().getText();
						Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(label);
						String guards[] = null;
						while(m.find()) {
							guards = m.group(1).replaceAll("\\\\n|\\\\r|\\r|\\n", "").split(";");
						}
						
						for (String equation: guards) {
							Formula fx = Formula.makeFormula(equation);
							tran.addGuard(fx);
						}
						m = Pattern.compile("\\{(.*?)\\}").matcher(label);
						String resets[] = {};
						while(m.find()) {
							resets = m.group(1).replaceAll("\\\\n|\\\\r|\\r|\\n", "").split(";");
						}
						
						for (String equation: resets) {
							Formula fx = Formula.makeFormula(equation);
							tran.addReset(fx);
						}
					}
					hioa.addTransition(tran);
				}
			}
		}
		
		return hioa;
	}	
}
	