
public enum Assessment {
	RIGHT,
	WRONG,
	BUG;
	
	static public Assessment getAssessment(String assessmentString) {
		if (assessmentString.equals("RIGHT")) {
			return RIGHT;
		} else if (assessmentString.equals("WRONG")) {
			return WRONG;
		} else if (assessmentString.equals("BUG")) {
			return BUG;
		} else {
			throw new IllegalArgumentException();
		}
	}
}