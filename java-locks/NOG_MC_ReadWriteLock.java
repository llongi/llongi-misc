// No Order Guaranteed (NOG) Multiple Conditions ReadWriteLock

// This implementation is biased towards either readers or writers

import java.util.concurrent.locks.*;

public class NOG_MC_ReadWriteLock {
	private int intentionedWriters = 0, activeWriter = 0;
	private int intentionedReaders = 0, activeReaders = 0;

	private int maxReaders = 0;
	private char bias = 'W';

	private Lock lock = new ReentrantLock();
	private Condition readers = lock.newCondition();
	private Condition writers = lock.newCondition();

	private static ThreadLocal<Integer> tls_owned_locks = new ThreadLocal<Integer>() {
		protected synchronized Integer initialValue() {
			return new Integer(0);
		}
	};
	// -1 means "this thread owns a write-lock"
	// 0 means "this thread holds no lock"
	// 1 means "this thread owns a read-lock"
	// 2 and higher (max being maxReaders) means
	// "this thread owns as many read-locks"

	public NOG_MC_ReadWriteLock(int maxThreads, int maxReaders, char bias) {
		// Configurable limit on maximum simultaneous readers.
		// A positive number means "limit at that value", 0 or less will
		// simply be interpreted as if passing 1.
		this.maxReaders = maxReaders;
		this.bias = bias;

	}

	public void rdlock() throws RuntimeException {
		lock.lock();
		try {
			// Deadlock detection, check if this thread already holds a write-lock
			if (tls_owned_locks.get() == -1) {
				throw new RuntimeException();
			}

			if (activeWriter == 1 || activeReaders >= maxReaders || (bias == 'W' && intentionedWriters > 0)) {
				intentionedReaders++;

				while (activeWriter == 1 || activeReaders >= maxReaders || (bias == 'W' && intentionedWriters > 0)) {
					try {
						readers.await();
					} catch (InterruptedException e) {
						System.out.println(e.getMessage());
					}
				}

				intentionedReaders--;
			}

			activeReaders++;

			tls_owned_locks.set(tls_owned_locks.get() + 1);
		} finally {
			lock.unlock();
		}
	}

	public void wrlock() throws RuntimeException {
		lock.lock();
		try {
			// Deadlock detection, check if this thread already holds a write- or read-lock
			if (tls_owned_locks.get() != 0) {
				throw new RuntimeException();
			}

			if (activeWriter == 1 || activeReaders > 0 || (bias == 'R' && intentionedReaders > 0)) {
				intentionedWriters++;

				while (activeWriter == 1 || activeReaders > 0 || (bias == 'R' && intentionedReaders > 0)) {
					try {
						writers.await();
					} catch (InterruptedException e) {
						System.out.println(e.getMessage());
					}
				}

				intentionedWriters--;
			}

			activeWriter = 1;

			tls_owned_locks.set(-1);
		} finally {
			lock.unlock();
		}
	}

	public void unlock() throws RuntimeException {
		lock.lock();
		try {
			if (activeWriter == 1) {
				if (tls_owned_locks.get() != -1) {
					throw new RuntimeException();
				}
				tls_owned_locks.set(0);

				activeWriter = 0;

				if (bias == 'W') {
					if (intentionedWriters > 0) {
						writers.signal();
					}
					else if (intentionedReaders > 0) {
						readers.signalAll();
					}
				}
				else {
					if (intentionedReaders > 0) {
						readers.signalAll();
					}
					else if (intentionedWriters > 0) {
						writers.signal();
					}
				}
			} else {
				// Check if there are no locked threads at all
				if (activeReaders == 0) {
					throw new RuntimeException();
				}

				if (tls_owned_locks.get() <= 0) {
					throw new RuntimeException();
				}
				tls_owned_locks.set(tls_owned_locks.get() - 1);

				activeReaders--;

				if (bias == 'W') {
					if (activeReaders == 0
					&& intentionedWriters > 0) {
						writers.signal();
					}
					else if (intentionedReaders > 0) {
						readers.signalAll();
					}
				}
				else {
					if (intentionedReaders > 0) {
						readers.signalAll();
					}
					else if (activeReaders == 0
					&& intentionedWriters > 0) {
						writers.signal();
					}
				}
			}
		} finally {
			lock.unlock();
		}
	}
}
