import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.zip.ZipException;

import javax.imageio.ImageIO;

import org.conqat.lib.commons.logging.SimpleLogger;
import org.conqat.lib.simulink.builder.SimulinkModelBuilder;
import org.conqat.lib.simulink.builder.SimulinkModelBuildingException;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.stateflow.StateflowChart;
import org.conqat.lib.simulink.util.SimulinkBlockRenderer;

import com.pretzel.reader.simulink.SimulinkReader;
import com.pretzel.solver.qss.HaWriter;
import com.pretzel.structure.Block;
import com.pretzel.structure.HIOA;

public class Main {
	public static void main(String[] args) throws ZipException, IOException, SimulinkModelBuildingException {
		String filename = "resource/example1.mdl";
		System.out.println("Target file : " + filename);
		SimulinkReader SR = new SimulinkReader(filename);
		
		HashSet<HIOA> HIOAs = SR.extractHIOAs();
		HashSet<Block> Blocks = SR.extractBlocks();
		
		for (HIOA h : HIOAs) {
			System.out.println(h);
		}
		
		/*	
		HaWriter hw = new HaWriter(file.getName(), simTime);
		
		

		// Step 2: Translate each chart into a HA (of type SimQssHA)
    	HashSet<HIOA> HAs = new HashSet<HIOA>();
		for (StateflowChart chart : charts) {
			HIOA ha = new HIOA(chart.getName());
			ha.convert(chart);
			HAs.add(ha);
			System.out.println(ha.toString());
		}
		
		// Step 3: Write all blocks in python
		if (blocks.size() > 0) {
			hw.initBlockFile();
			hw.writeBlocks(blocks);
		}
		
		// Step 4: Write all charts in python
		if (HAs.size() > 0){
			hw.initChartFile();
			//hw.writeCharts(HAs);
		}
		
		// 
		
		System.out.println("Program complted");
    	//hw.addLine(hw.MAIN_FILE, "\r\nif __name__ == '__main__':\r\n\tmain()");
    	
    	 */
	}
}
