import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


public class ENAWriter {

	private FileWriter writer;
	
	private int currentLine = 1;
	private int currentAction = 1;
	private int currentClip = -1;
	private boolean currentClipIsGaming = false;
	
	public ENAWriter(String fileName) throws IOException {
		
		writer = new FileWriter(fileName + ".csv");
		
		// TODO: should we also include the type of the action in here?
		
		// Write the header
		writer.write("line,clip,action,");
		
		for (InterpretationConstituent constituent : InterpretationConstituent.values()) {
			
			writer.write(InterpretationConstituent.getTextValue(constituent) + ",");
		}
		
		writer.write("isGaming\r\n");
	}
	
	public void startNewClip(int clipNumber, boolean isGaming) {
		
		currentAction = 1;
		currentClip = clipNumber;
		currentClipIsGaming = isGaming;
	}
	
	public void writeNewLine(List<Integer> constituents) throws IOException {
		
		writer.write(currentLine + "," + currentClip + "," + currentAction + ",");
		
		for (Integer constituent : constituents) {
			
			writer.write(constituent + ",");
		}
		
		writer.write(currentClipIsGaming ? "1" : "0");
		writer.write("\r\n");
		
		currentLine++;
		currentAction++;
	}
	
	public void closeFile() throws IOException {
		
		writer.close();
	}
}
