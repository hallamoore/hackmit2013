package translator;

import java.io.PrintWriter;

public class Translator {

	private final PrintWriter out;

	public Translator(PrintWriter out) {
		this.out = out;
	}

	public static String translateCursor(String x, String y) {
		// where to move block!
		return "{'response':{'action':'cursor','x':" + x + ",'y':" + y + "}}";

	}

	public static String translateClick(String prevX, String prevY) {
		// where to paste the block!
		return "{'response':{'action':'click','x':" + prevX + ",'y':" + prevY
				+ "}}";

	}

}
