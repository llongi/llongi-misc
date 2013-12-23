import java.util.Random;

public class ReaderWriter implements Runnable {
	ProtectedObject obj;

	ReaderWriter(ProtectedObject o) {
		obj = o;
	}

	public void run() {
		Random rvalue = new Random();

		for (int i = 0; i < 100; i++) {
			if (rvalue.nextInt(100) < 80) {
				// read
				obj.read(50);
			} else {
				// write
				obj.write(50);
			}
		}
	}
}
