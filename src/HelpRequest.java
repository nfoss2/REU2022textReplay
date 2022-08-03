
public class HelpRequest extends UserAction {

	private final double numSteps;
	
	public HelpRequest(String lesson, String cell, double time, double numSteps) {
		super(lesson, cell, time);
		this.numSteps = numSteps;
	}
	
	public double getNumSteps() {
		return numSteps;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("Help request: " + numSteps + " steps\r\n");
		builder.append("Took: " + getTime() + "\r\n");
		builder.append("Cell: " + getCell());
		
		return builder.toString();
	}
}
