package cs425.mp2;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class FDIntroducer extends FD{
	public FDIntroducer(int port){
		timestamp=System.currentTimeMillis();
		try {
			self_id=new Pid(InetAddress.getLocalHost().getHostName(),port,0);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
