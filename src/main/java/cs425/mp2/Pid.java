package cs425.mp2;

public class Pid {
	String address;
	int port;
	long timestamp;
	public Pid(String address, int port, long timestamp){
		this.port=port;
		this.timestamp=timestamp;
		this.address=address;
	}
}
