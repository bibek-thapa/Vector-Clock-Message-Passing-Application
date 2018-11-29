package message;

import java.util.Comparator;

import clock.VectorClockComparator;

/**
 * Message comparator class. Use with PriorityQueue.
 */
public class MessageComparator implements Comparator<Message> {

    public int compare(Message lhs, Message rhs) {
        // Write your code here
    	VectorClockComparator comp = new VectorClockComparator();
        return comp.compare(lhs.ts, rhs.ts);
    }

}
