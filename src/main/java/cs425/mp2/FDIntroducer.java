package cs425.mp2;

import java.net.InetAddress;
import java.net.UnknownHostException;

// TODO check functionality
public class FDIntroducer extends FailureDetector {
	public FDIntroducer(int port){
        super(port,"",0);
		try {
			this.self_id=new Pid(InetAddress.getLocalHost().getHostName(),port,0);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
