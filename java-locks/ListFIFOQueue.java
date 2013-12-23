public class ListFIFOQueue {
	public class QueueNode {
		long item = -1;
		QueueNode next = null;

		public QueueNode(long item) {
			this.item = item;
		}
	}

	QueueNode head = null;
	QueueNode tail = null;

	public void enq(long item) {
		QueueNode node = new QueueNode(item);

		if (head == null) {
			head = node;
		}
		else {
			tail.next = node;
		}

		tail = node;
	}

	public long deq() {
		if (head == null) {
			return -1;
		}

		QueueNode node = head;

		head = node.next;

		if (node.next == null) {
			tail = null;
		}

		return node.item;

	}

	public long getFirstItem() {
		if (head == null) {
			return -1;
		}

		return head.item;
	}

	public boolean isEmpty() {
		if (head == null) {
			return true;
		}

		return false;
	}
}
