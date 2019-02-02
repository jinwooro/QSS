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
import org.conqat.lib.simulink.util.SimulinkBlockRenderer;


public class SimulinkConvert {

	public static void main(String[] args) throws SimulinkModelBuildingException, ZipException, IOException {
	    File file = new File("resource/example1.slx");
	    try (SimulinkModelBuilder builder = new SimulinkModelBuilder(file,
	        new SimpleLogger())) {
	      SimulinkModel model = builder.buildModel();

	      // list all blocks in the model
	      for (SimulinkBlock block : model.getSubBlocks()) {
	        System.out.println("Block: " + block.getName());
	      }

	      // render a block or model as PNG image
	      SimulinkBlockRenderer simulinkBlockRenderer = new SimulinkBlockRenderer();
	      BufferedImage image = simulinkBlockRenderer.renderBlock(model);
	      ImageIO.write(image, "PNG", new File(file.getPath() + ".png"));
	    }
	  }
}
