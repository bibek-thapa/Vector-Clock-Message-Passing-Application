package clock;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class VectorClock implements Clock {

	// suggested data structure ...
	public Map<String, Integer> clock = new LinkedHashMap<String, Integer>();

	public void update(Clock other) {

		VectorClock temp = (VectorClock) other;

		Set<String> keys = temp.clock.keySet();

		for (String key : keys) {
			if (!clock.containsKey(key) || temp.clock.get(key) > clock.get(key)) {
				clock.put(key, other.getTime(Integer.parseInt(key)));
			}

		}

	}

	public void setClock(Clock other) {

		Set<String> keys = this.clock.keySet();
		for (String key : keys) {

			clock.put(key, other.getTime(Integer.parseInt(key)));
		}
	}

	public void tick(Integer pid) {
		clock.put(Integer.toString(pid), clock.get(Integer.toString(pid)) + 1);
	}

	public boolean happenedBefore(Clock other) {
	
		VectorClock temp = (VectorClock) other;
    	if(temp.clock.size() < this.clock.size()) {
    		return false;
    	}
    	for(String key: temp.clock.keySet()) {
    		if (this.clock.containsKey(key)) {
    			if (this.clock.get(key) > temp.clock.get(key)) {
        			return false;
        		}
    		}
    	}
        return true;
	}

	public String toString() {

		int i = 0;
		String temp = "{";
		String temp1 = "";
		String temp2 = "}";
		String result;
		Set<String> keys = this.clock.keySet();
		for (String key : keys) {
			if (i < (this.clock.size() - 1)) {
				temp1 = temp1 + "\"" + key + "\":" + clock.get(key) + ",";

			}

			else {
				temp1 = temp1 + "\"" + key + "\":" + clock.get(key);
			}
			i = i + 1;
		}
		result = temp + temp1 + temp2;
		return result;
	}

	public void setClockFromString(String clock) {
		Map<String, Integer> tempClock = new LinkedHashMap<String, Integer>(this.clock);
		this.clock.clear();

		if (!clock.contentEquals(("{}"))) {
			clock = clock.replace("{", "");
			clock = clock.replace("}", "");
			clock = clock.replaceAll("\"", "");
			String[] tokens = clock.split(",");

			try {
				for (int i = 0; i < tokens.length; i++) {
					String[] tokens1 = tokens[i].split(":");
					this.clock.put(tokens1[0], Integer.parseInt(tokens1[1]));
				}
			} catch (Exception e) {
				this.clock.clear();
				this.clock = tempClock;
			}
		}
	}

	public int getTime(int p) {

		return clock.get(Integer.toString(p));
	}

	public void addProcess(int p, int c) {

		clock.put(Integer.toString(p), c);

	}

	public Map<String, Integer> getClock() {
		return clock;
	}

	public void setClock(Map<String, Integer> clock) {
		this.clock = clock;
	}
}
