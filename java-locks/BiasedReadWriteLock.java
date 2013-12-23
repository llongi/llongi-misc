public class BiasedReadWriteLock {
	private boolean activeWriter = false;
	private int activeReaders = 0;
	private int maxReaders = 0;
	private char bias = 'R';
	private ArrayFIFOQueue readersQueue;
	private ArrayFIFOQueue writersQueue;

	public BiasedReadWriteLock(int maxThreads, int maxReaders, char bias) {
		this.readersQueue = new ArrayFIFOQueue(maxThreads);
		this.writersQueue = new ArrayFIFOQueue(maxThreads);
		this.maxReaders = maxReaders;
		this.bias = bias;
	}

	synchronized public void rdlock() {
		// Who am I ???
		long tid = Thread.currentThread().getId();

		// Place myself on queue
		readersQueue.enq(tid);

		// While its not my turn, wait
		while (readersQueue.getFirstItem() != tid
				|| activeWriter == true
				|| activeReaders >= maxReaders
				|| (bias == 'W' && writersQueue.isEmpty() == false)) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}
		}

		// Its my turn, remove myself from queue
		readersQueue.deq();

		// DEBUG
        if (activeWriter == true
        		|| activeReaders >= maxReaders
        		|| (bias == 'W' && writersQueue.isEmpty() == false)) {
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
        writersQueue.enq(tid);

        // While its not my turn, wait
        while (writersQueue.getFirstItem() != tid
        		|| activeWriter == true
        		|| activeReaders > 0
        		|| (bias == 'R' && readersQueue.isEmpty() == false)) {
        	try {
				wait();
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}
        }

        // Its my turn, remove myself from queue
        writersQueue.deq();

        // DEBUG
        if (activeWriter == true
        		|| activeReaders > 0
        		|| (bias == 'R' && readersQueue.isEmpty() == false)) {
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
