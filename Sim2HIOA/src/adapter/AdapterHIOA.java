package adapter;


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

import structure.Formula;
import structure.HIOA;
import structure.Line;
import structure.Location;
import structure.NetworkHIOA;
import structure.Transition;

/**
 * This class converts a slx and mdl files into a network of HIOAs. This network of HIOAs is stored in an object instance called NetworkHIOA. 
 * @author Jin Woo Ro
 *
 */
public class AdapterHIOA extends SimulinkKeywords {
	
	private File file = null;
	private SimulinkModel model = null;
	private NetworkHIOA systemModel = null;
	
	/**
	 * Constructor.
	 * @param inputFileName The target file name.
	 * @throws ZipException An exception.
	 * @throws IOException An exception.
	 * @throws SimulinkModelBuildingException An exception.
	 */
	public AdapterHIOA(String inputFileName) throws ZipException, IOException, SimulinkModelBuildingException {
		
		// open the file and load the model
		File file = new File(inputFileName);
		try (SimulinkModelBuilder builder = new SimulinkModelBuilder(file, new SimpleLogger())) {
			model = builder.buildModel();
	    } 
	
		
		// differentiate blocks and charts
		HashSet<StateflowChart> charts = new HashSet<StateflowChart>();
		HashSet<SimulinkBlock> blocks = new HashSet<SimulinkBlock>();
		HashSet<String> namesStateflowCharts = new HashSet<String>();
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
		
		
		// TODO: extract the simulation setup information and store it.
		
		
		
		// Instantiate a system model object (with the name being the file name)
		NetworkHIOA sys = new NetworkHIOA(inputFileName);
		// QSHIOA conversion 
		for (StateflowChart chart : charts) {
			HIOA q = extractHIOA(chart);
			sys.addHIOA(q);
		}
		// Line conversion
		for (StateflowChart chart : charts) {
			SimulinkBlock cb =  chart.getStateflowBlock();
			for (SimulinkLine sl : cb.getInLines()) {
				if (sl.getSrcPort() == null) {
					continue;
				}
				
				String srcBlockName = sl.getSrcPort().getBlock().getName();
				if (hasChart(srcBlockName) == false) {
					continue;
				}
				
				Line line = makeLine(sl); 
				sys.addLine(line);
			}
		}
		
		// store it in this class scope
		systemModel = sys;
		
	}
	
	/**
	 * @return Returns the network of QSHIOAs.
	 */
	public NetworkHIOA getSystem() {
		return systemModel;
	}

	/**
	 * Converts the SimulinkLine into a simpler line structure for the QSHIOA connections.
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
	 * @param name The name of the charted to be returned.
	 * @return Returns true if the chart is found, otherwise, false.
	 */
	private boolean hasChart(String name) {
		// return true if the requested name exists in the set of charts
		if (model.getStateflowMachine() == null) {
			return false;
		} else {
			for (StateflowChart chart : model.getStateflowMachine().getCharts()) {
				if (chart.getName().equals(name)) {	
					return true;
				}
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
	private HIOA extractHIOA (StateflowChart chart) {
		// HIOA instance name
		HIOA hioa = new HIOA(chart.getName());
		
		// Extract the variables
		for (StateflowData data : chart.getData()) {	
			String variableName = data.getName();	
			double initialValue = 0.0;
			if (data.getDeclaredParameterNames().contains(INITIAL_VALUE)) {
				initialValue = Double.parseDouble(data.getParameter(INITIAL_VALUE));
			}	
			
			// identify the scope of this variable
			String scope = data.getParameter("scope");
			switch (scope) {
				case LOCAL_DATA:
					if (data.getDeclaredParameterNames().contains(VARIABLE_TYPE)) {
						if (data.getParameter(VARIABLE_TYPE).contentEquals(CONTINUOUS_DATA)) {
							// Store this variable as a continuous variable
							hioa.addContinuousVariable(variableName, initialValue);
						}
					} else {
						// store this variable as a discrete variable
						hioa.addDiscreteVariable(variableName, initialValue);
					}
					break;
				case OUTPUT_DATA:
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
		
		
		// Extract the locations
		for (StateflowNodeBase n : chart.getNodes()) {
			String nodeContents = n.getResolvedId().toString();
			String[] lines = nodeContents.split("\\\\n|\\\\r|\\r|\\n");
			
			// set name
			String locationName = lines[0].split("/")[lines[0].split("/").length-1];
			Location l = new Location(locationName);
			
			// set id
			int nodeId = Integer.parseInt(n.getId());
			l.setID(nodeId);
			
			// TODO: need a better algorithm to extract the keywords
			int currentMode = 0;
			for (int i = 1; i < lines.length; i++) {
				// remove ";" and " " characters
				String line = lines[i].replace(";","").replaceAll("\\s","");	
				// identify the type of this equation
				if (line.contains(":")){
					currentMode = getEquationType(line);
					continue;
				}
				// create a formula
				Formula fx = Formula.makeFormula(line);
				if (fx == null) {
					System.out.println("Error: this equation is invalid: " + line);
					System.exit(0);
				}
				
				int varType = hioa.hasVariable(fx.getLHS());
				
				if ((currentMode == 1) || (currentMode == 3)){ // this mode is "du"
					if (varType == 3){ l.addODE(fx); }
					else if (varType == 5) { l.addOutputUpdateAction(fx); }
					else {
						System.out.println("Error: invalid equation: " + line);
						System.exit(0);
					}
				} 
				if ((currentMode == 2) || (currentMode == 3)){ // this mode is "en"
					if ((varType != 3) && (varType != 4)) {
						l.addEntryAction(fx);
					} else {
						System.out.println("Error: entry ations at this location is invalid: " + line);
						System.exit(0);
					}
				}
			}
			hioa.addLocation(l);
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
					Location loc = hioa.getLocation(initialLocationId);
					hioa.setInitialLocation(loc);
				
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
							hioa.addInitialization(fx);
						}
					}
				}
				else {
					int srcLocId = Integer.parseInt(t.getSrc().getId());
					int dstLocId = Integer.parseInt(t.getDst().getId());
					String srcLocName = hioa.getLocation(srcLocId).getName();
					String dstLocName = hioa.getLocation(dstLocId).getName();
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
						int varType = hioa.hasVariable(fx.getLHS()); 
						if (varType == 0) {
							System.out.println("Error: this equation contains a non-existing variable: " + equation);
							System.exit(0);
						}	
						tran.addReset(fx);
					}
					hioa.addTransition(tran);
				}
			}
		}
		
		return hioa;
	}
	
	private int getEquationType(String equation) {
		if (equation.contains("du") == true && equation.contains("en") == false) {
			return 1; // du
		} else if (equation.contains("en") == true && equation.contains("du") == false){
			return 2; // en
		} else if (equation.contains("en") && equation.contains("du")) {
			return 3; // du, en
		}
		return 0;
	}
}