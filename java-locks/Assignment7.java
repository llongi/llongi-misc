public class Assignment7 {
	static final int MAX_THREADS = 4;
	static final int MAX_READERS = 4;

	public static void main(String[] args) {
		ProtectedObject obj = new ProtectedObject(MAX_THREADS, MAX_READERS);
		Thread[] threads = new Thread[MAX_THREADS];

		long start_time = System.currentTimeMillis();

		for (int i = 0; i < MAX_THREADS; i++) {
			threads[i] = new Thread(new ReaderWriter(obj));
			threads[i].start();
		}

		for (int i = 0; i < MAX_THREADS; i++) {
			try {
				threads[i].join();
			}
			catch (InterruptedException e) {}
		}

		long end_time = System.currentTimeMillis();

		System.out.println("elapsed time: " + (end_time - start_time) + " (in seconds " + ((end_time - start_time) / 1000) + ")");
	}
}
