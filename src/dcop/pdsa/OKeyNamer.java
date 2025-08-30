package dcop.pdsa;

public class OKeyNamer {

	static final String wb_i_key = "wb";
	static final String ki_key = "k";
	static final String wi_key = "w";
	static final String beta_key = "beta";
	static final String gamma_key = "gamma";
	static final String delta_key = "delta";


	public static String WbBase(int owner) {
		return String.format("%s-%d", wb_i_key, owner);
	}
	
	public static String WbBaseWithIndex(String base, int index) {
		return String.format("%s-%d", base, index);
	}
	
	public static String WbIndex(int owner, int index) {
		return String.format("%s-%d-%d", wb_i_key, owner, index);
	}
	
	public static String k(int owner) {
		return String.format("%s-%d",ki_key, owner);
	}
	
	public static String w(int owner) {
		return String.format("%s-%d",wi_key, owner);
	}

	public static String beta(int owner) {
		return String.format("%s-%d", beta_key, owner);
	}
	
	public static String gammaSub(int owner) {
		return String.format("%s-sub-%d", gamma_key, owner);
	}
	
	public static String gamma(int owner) {
		return String.format("%s-%d", gamma_key, owner);
	}

	public static String deltaSub(int owner) {
		return String.format("%s-sub-%d", delta_key, owner);
	}

	public static String delta(int owner) {
		return String.format("%s-%d", delta_key, owner);
	}

}
