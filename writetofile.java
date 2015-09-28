import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class writetofile {
	public static writetofile obj;
	BufferedWriter writer = null;

	private writetofile() throws IOException {
		File logFile = new File("vmStatistics.log");
		System.out.println(logFile.getCanonicalPath());
		writer = new BufferedWriter(new FileWriter(logFile, true));
	}

	public static writetofile getInstance() throws IOException {
		if (obj == null) {
			obj = new writetofile();
		}
		return obj;
	}

	public void write(String s) {

		try {
			writer.write(s);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				writer.flush();
				// Close the writer regardless of what happens...
				// writer.close();
			} catch (Exception e) {
			}
		}
	}
}

