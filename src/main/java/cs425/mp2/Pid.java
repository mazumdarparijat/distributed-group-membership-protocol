package cs425.mp2;

public class Pid {
	String hostname;
	int port;
	long timestamp;
	public Pid(String hostname, int port, long timestamp){
		this.port=port;
		this.timestamp=timestamp;
		this.hostname=hostname;
	}
}
