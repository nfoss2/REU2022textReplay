
public abstract class UserAction {

	private final double time;
	private final String lesson;
	private final String cell;
	
	public UserAction(String lesson, String cell, double time) {
		this.time = time;
		this.lesson = lesson;
		this.cell = cell;
	}
	
	public double getTime() {
		return time;
	}
	
	public String getLesson() {
		return lesson;
	}
	
	public String getCell() {
		return cell;
	}
	
	@Override
	public abstract String toString();
}
