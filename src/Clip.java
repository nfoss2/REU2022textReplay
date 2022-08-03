import java.util.ArrayList;
import java.util.List;


public class Clip {

	private List<UserAction> actions;
	private String student;
	
	private int rightStepCount = 0;
	private int notRightStepCount = 0;
	private int wrongStepCount = 0;
	private int bugStepCount = 0;
	private int helpRequestCount = 0;
	private int attemptCount = 0;
	
	private final int id;
	
	private int numberOfRowsInLogFile = 0;
	
	private boolean isValid = true;
	
	public Clip(int id) {
		actions = new ArrayList<UserAction>();
		this.id = id;
	}
	
	public int getID() {
		return id;
	}
	
	public void setNumberOfRowsInLogFile(int numberOfRows) {
		numberOfRowsInLogFile = numberOfRows;
	}
	
	public int getNumberOfRowsInLogFile() {
		return numberOfRowsInLogFile;
	}
	
	public void addAction(UserAction action) {
		actions.add(action);
		
		if (action instanceof HelpRequest) {
			helpRequestCount++;
		} else if (action instanceof StepAttempt) {
			attemptCount++;
			
			StepAttempt attempt = (StepAttempt) action;
			Assessment assessment = attempt.getAssessment();
			
			if (assessment == Assessment.RIGHT) {
				rightStepCount++;
			} else {
				notRightStepCount++;
				if (assessment == Assessment.WRONG) {
					wrongStepCount++;
				} else if (assessment == Assessment.BUG) {
					bugStepCount++;
				} else {
					throw new IllegalArgumentException();
				}
			}
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	public List<UserAction> getActions() {
		return actions;
	}
	
	public void setStudent(String student) {
		this.student = student;
	}
	
	public String getStudent() {
		return student;
	}
	
	public String getLesson() {
		
		return actions.get(0).getLesson();
	}
	
	public static String getActionCountHeaderString() {
		return "helpRequestCount,attemptCount,rightStepCount,notRightStepCount,wrongStepCount,bugStepCount";
	}
	
	public String getActionCountString() {
		return helpRequestCount + "," + attemptCount + "," + rightStepCount + "," +
	           notRightStepCount + "," + wrongStepCount + "," + bugStepCount;
	}
	
	public String getActionRatioString() {
		int totalActions = actions.size();
		
		return ((float) helpRequestCount / (float) totalActions) + "," + 
			   ((float) attemptCount / (float) totalActions) + "," + 
			   ((float) rightStepCount / (float) totalActions) + "," +
		       ((float) notRightStepCount / (float) totalActions) + "," + 
			   ((float) wrongStepCount / (float) totalActions) + "," + 
		       ((float) bugStepCount / (float) totalActions);
	}
	
	public void invalidate() {
		isValid = false;
	}
	
	public boolean isValid() {
		
		return isValid;
	}
}
