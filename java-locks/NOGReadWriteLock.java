// No Order Guaranteed (NOG) ReadWriteLock

// Note that in my implementation it is not enforced what kind of lock type
// will wake up after a notifyAll() call. This means that if, for example, we
// have a writer executing and there is a request pending for another writer
// and a reader, when the actual writer finishes it will signal all threads and
// it's not enforced if the waiting reader or the waiting writer will get to
// execute first.

public class NOGReadWriteLock {
	private boolean intentionedWriter = false, activeWriter = false;
	private int intentionedReaders = 0, activeReaders = 0;
	private int maxReaders = 0;

	public NOGReadWriteLock(int maxThreads, int maxReaders) {
		this.maxReaders = maxReaders;

	}

	synchronized public void rdlock() {
		while (intentionedWriter) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}
		}

		intentionedReaders++;

		while (activeReaders >= maxReaders) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}
		}

		// DEBUG
		if (activeWriter == true
				|| (intentionedWriter == true && intentionedReaders == 0)
				|| activeReaders >= maxReaders) {
			System.out.println("BUG IN READER");
		}

		activeReaders++;
		intentionedReaders--;
	}

	synchronized public void wrlock() {
		while (intentionedWriter) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}
		}

		intentionedWriter = true;

		while (intentionedReaders > 0 || activeReaders > 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}
		}

		// DEBUG
		if (activeWriter == true
				|| intentionedWriter == false
				|| activeReaders > 0
				|| intentionedReaders > 0) {
			System.out.println("BUG IN WRITER");
		}

		activeWriter = true;
	}

	synchronized public void unlock() throws RuntimeException {
		if (activeWriter) {
			activeWriter = false;
			intentionedWriter = false;
			notifyAll();
		}
		else {
			// Check if there are no locked threads at all
			if (activeReaders == 0) {
				throw new RuntimeException();
			}

			activeReaders--;
			notifyAll();
		}
	}
}
