// Implementation of a perfectly fair and ordered RWLock: it processes all requests exactly as it gets them.
// Ordering is preserved using a FIFO Queue, and several condition variables are used to only wake threads
// when there really is something they can do (one condition variable == one predicate).

import java.util.concurrent.locks.*;

public class MultiCond_FairReadWriteLock {
	private boolean activeWriter  = false;
	private int     activeReaders = 0;
	private int     maxReaders    = 0;
	private ArrayFIFOQueue queue;
	private Lock lock = new ReentrantLock();
	private boolean waitingOnQueue = false;
	private Condition      onQueue = lock.newCondition();
	private boolean waitingOnWriterFalse = false;
	private Condition      onWriterFalse = lock.newCondition();
	private boolean waitingOnReadersDown = false;
	private Condition      onReadersDown = lock.newCondition();
	private boolean waitingOnReadersZero = false;
	private Condition      onReadersZero = lock.newCondition();

	public MultiCond_FairReadWriteLock(int maxThreads, int maxReaders) {
		this.queue = new ArrayFIFOQueue(maxThreads);
		this.maxReaders = maxReaders;
	}

	public void rdlock() {
		lock.lock();
		try {
			// Who am I ???
			long tid = Thread.currentThread().getId();

			// Place myself on queue
			queue.enq(tid);

			// While its not my turn, wait
			while (queue.getFirstItem() != tid) {
				waitingOnQueue = true;
				try {
					onQueue.await();
				} catch (InterruptedException e) {
					System.out.println(e.getMessage());
				}
			}

			// Its my turn, but there is a writer active, wait
			while (activeWriter == true) {
				waitingOnWriterFalse = true;
				try {
					onWriterFalse.await();
				} catch (InterruptedException e) {
					System.out.println(e.getMessage());
				}
			}

			// Its my turn, but there are too many readers, wait
			while (activeReaders >= maxReaders) {
				waitingOnReadersDown = true;
				try {
					onReadersDown.await();
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
			if (waitingOnQueue) {
				waitingOnQueue = false;
				onQueue.signalAll();
			}
		}
		finally {
			lock.unlock();
		}
	}

	public void wrlock() {
		lock.lock();
		try {
			// Who am I ???
			long tid = Thread.currentThread().getId();

			// Place myself on queue
			queue.enq(tid);

			// While its not my turn, wait
			while (queue.getFirstItem() != tid) {
				waitingOnQueue = true;
				try {
					onQueue.await();
				} catch (InterruptedException e) {
					System.out.println(e.getMessage());
				}
			}

			// Its my turn, but there is a writer active, wait
			while (activeWriter == true) {
				waitingOnWriterFalse = true;
				try {
					onWriterFalse.await();
				} catch (InterruptedException e) {
					System.out.println(e.getMessage());
				}
			}

			// Its my turn, but there are still active readers, wait
			while (activeReaders > 0) {
				waitingOnReadersZero = true;
				try {
					onReadersZero.await();
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
		finally {
			lock.unlock();
		}
	}

	public void unlock() throws RuntimeException {
		lock.lock();
		try {
			if (activeWriter) {
				activeWriter = false;

				if (waitingOnWriterFalse) {
					waitingOnWriterFalse = false;
					onWriterFalse.signal();
				}
				else if (waitingOnQueue) {
					waitingOnQueue = false;
					onQueue.signalAll();
				}
			}
			else {
				// Check if there are no locked threads at all
				if (activeReaders == 0) {
					throw new RuntimeException();
				}

				activeReaders--;

				if (waitingOnReadersDown) {
					waitingOnReadersDown = false;
					onReadersDown.signal();
				}
				else if (waitingOnReadersZero
						&& activeReaders == 0) {
					waitingOnReadersZero = false;
					onReadersZero.signal();
				}
				else if (waitingOnQueue) {
					waitingOnQueue = false;
					onQueue.signalAll();
				}
			}
		}
		finally {
			lock.unlock();
		}
	}
}
