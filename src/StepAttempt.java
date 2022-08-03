
public class StepAttempt extends UserAction {

	private final String answer;
	private final Assessment assessment;
	private final String action;
	
	public StepAttempt(String lesson, String cell, double time, String action, String answer, Assessment assessment) {
		super(lesson, cell, time);
		this.answer = answer;
		this.assessment = assessment;
		this.action = action;
	}
	
	public String getAction() {
		return action;
	}
	
	public String getAnswer() {
		return answer;
	}
	
	public Assessment getAssessment() {
		return assessment;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		String actionString = action.equals("blank") ? "" : action;
		
		builder.append("Step attempt:\r\n" + "action: " + actionString + "\r\nanswer: " + answer + " (" + assessment.toString() + ")\r\n");
		builder.append("Took: " + getTime() + "\r\n");
		builder.append("Cell: " + getCell());
		
		return builder.toString();
	}
}