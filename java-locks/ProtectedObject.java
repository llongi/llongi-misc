class ProtectedObject {
	NOG_MC_ReadWriteLock m;

	public ProtectedObject(int max_threads, int max_readers) {
		m = new NOG_MC_ReadWriteLock(max_threads, max_readers, 'W');
	}

	public void read(int d) {
		m.rdlock();

		try {
			Thread.sleep(d);
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
		}

		m.unlock();
	}

	public void write(int d) {
		m.wrlock();

		try {
			Thread.sleep(d);
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
		}

		m.unlock();
	}
}
