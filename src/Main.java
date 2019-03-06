import java.io.IOException;
import java.util.HashSet;
import java.util.zip.ZipException;

import org.conqat.lib.simulink.builder.SimulinkModelBuildingException;

import com.pretzel.generator.Kieler.adapterKieler;
import com.pretzel.reader.simulink.SimulinkReader;
import com.pretzel.structure.Block;
import com.pretzel.structure.HIOA;
import com.pretzel.structure.Line;

public class Main {
	public static void main(String[] args) throws ZipException, IOException, SimulinkModelBuildingException {
		
		String filename = "resource/example2.slx";
		System.out.println("Target file : " + filename);
		SimulinkReader SR = new SimulinkReader(filename);
		
		HashSet<HIOA> HIOAs = SR.extractHIOAs(false);
		HashSet<Block> Blocks = SR.extractBlocks();
		HashSet<Line> Lines = SR.extractLines();
		
		for (HIOA h : HIOAs) {
			System.out.println(h);
		}
		
		for (Block b : Blocks) {
			System.out.println(b);
		}
		
		for (Line l : Lines) {
			System.out.println(l);
		}
		
		adapterKieler ak = new adapterKieler(filename);
		ak.generateCode(HIOAs, Blocks, Lines);
	
	}
}
