package tracker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;



import translator.Translator;

public class PsuedoServer {

	public static void main(String[] args) throws IOException {
		final ColorTracker tracker = new ColorTracker();
		String message = "";
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					tracker.run();
				} catch (InterruptedException e) {
					tracker.stop();
					e.printStackTrace();
				}
			}
		});
		thread.start();
		try {
			File file = new File("test.txt");
			if (!file.exists()) {
				file.createNewFile();
			}

			//while (true) {
			for(int i = 0; i < 100; i++){
				FileWriter fw = new FileWriter(file, false);
				if(tracker.click){
					message += Translator.translateClick("" + tracker.xPercent, "" + tracker.yPercent);
				}
				else if (tracker.cursor) {

					
					message += Translator.translateCursor("" + tracker.xPercent, "" + tracker.yPercent);
				}
				System.out.println(message);
				fw.write(message);
				fw.close();
				message = "";
				Thread.sleep(500);

			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			tracker.stop();
		}
	}

}
