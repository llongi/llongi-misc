public class ArrayFIFOQueue {
	long[] queue;
	int head = 0;
	int tail = 0;
	final int QUEUE_SIZE;

	public ArrayFIFOQueue(int size) {
		QUEUE_SIZE = size;
		queue = new long[QUEUE_SIZE];
	}

	public void enq(long item) {
		if (tail - head == QUEUE_SIZE) {
			// this should not happen in your implementation
			System.out.println("Queue is full ... debug your code :-)");
			System.exit(0);
		}
		queue[tail % QUEUE_SIZE] = item;
		tail++;
	}

	public long deq() {
		if (tail == head) {
			// this should not happen in your implementation
			System.out.println("Queue is empty -- debug your code :-)");
			System.exit(0);
		}
		long tmp = queue[head % QUEUE_SIZE];
		head++;
		return tmp;
	}

	public long getFirstItem() {
		return queue[head % QUEUE_SIZE];
	}

	public boolean isEmpty() {
		if (head == tail) {
			return true;
		}
		else {
			return false;
		}
	}
}
