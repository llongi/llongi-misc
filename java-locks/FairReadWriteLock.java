public class FairReadWriteLock {
	private boolean activeWriter = false;
	private int activeReaders = 0;
	private int maxReaders = 0;
	private ArrayFIFOQueue queue;

	public FairReadWriteLock(int maxThreads, int maxReaders) {
		this.queue = new ArrayFIFOQueue(maxThreads);
		this.maxReaders = maxReaders;
	}

	synchronized public void rdlock() {
		// Who am I ???
		long tid = Thread.currentThread().getId();

		// Place myself on queue
		queue.enq(tid);

		// While its not my turn, wait
		while (queue.getFirstItem() != tid
				|| activeWriter == true
				|| activeReaders >= maxReaders) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}
		}

		// Its my turn, remove myself from queue
		queue.deq();

		// DEBUG
        if (activeWriter == true
        		|| activeReaders >= maxReaders) {
        	System.out.println("BUG IN READER");
        }

		// I am now an active reader!
		activeReaders++;

		// Signal all, so other readers may continue
		notifyAll();
	}

	synchronized public void wrlock() {
		// Who am I ???
        long tid = Thread.currentThread().getId();

        // Place myself on queue
        queue.enq(tid);

        // While its not my turn, wait
        while (queue.getFirstItem() != tid
        		|| activeWriter == true
      			|| activeReaders > 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}
        }

        // Its my turn, remove myself from queue
        queue.deq();

        // DEBUG
        if (activeWriter == true
        		|| activeReaders > 0) {
        	System.out.println("BUG IN WRITER");
        }

        // I am now an active writer!
        activeWriter = true;
	}

	synchronized public void unlock() throws RuntimeException {
		if (activeWriter) {
			activeWriter = false;
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
