
public enum Behavior {
	GAMING,
	NOT_GAMING,
	BAD_CLIP;
	
	public String getBehaviorSymbol() {
		if (this == GAMING)
			return "G";
		else if (this == NOT_GAMING)
			return "N";
		else
			return "?";
	}
}
