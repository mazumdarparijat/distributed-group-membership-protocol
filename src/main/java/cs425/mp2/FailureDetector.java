package cs425.mp2;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FailureDetector {
	private Pid introducer_id;
	public Pid self_id;
	public DatagramSocket socket;
	public AtomicInteger protocolTime=new AtomicInteger(0);
	public long timestamp;
	public ConcurrentHashMap<String,Tuple> Mlist = new ConcurrentHashMap<String,Tuple>();
	public ConcurrentHashMap<String,Tuple> Jlist=new ConcurrentHashMap<String,Tuple>();
	public ConcurrentHashMap<String,Tuple> Slist=new ConcurrentHashMap<String,Tuple>();
	public ConcurrentHashMap<String,Tuple> Flist=new ConcurrentHashMap<String,Tuple>();
	public ConcurrentHashMap<String,Tuple> Awklist=new ConcurrentHashMap<String,Tuple>();
	public void addMember(Pid p, Tuple t){
		Mlist.put(p.toString(), t);
	}
	public FailureDetector(int port, String iadd, int iport){
		this();
		timestamp=System.currentTimeMillis();
		try {
			self_id=new Pid(InetAddress.getLocalHost().getHostName(),port,timestamp);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		introducer_id=new Pid(iadd,iport,0);
		addMember(new Pid(iadd,iport,0),new Tuple("ALIVE", 0));
	}
	public FailureDetector(){
	}
	public void runFD() throws SocketException {
		try {
			socket = new DatagramSocket(self_id.port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		Thread receiverThread = new Receiver(socket,Mlist,Jlist,Slist,Flist, Awklist, protocolTime);
		receiverThread.setDaemon(true);
		receiverThread.start();
		Thread senderThread = new Sender(socket,Mlist,Jlist,Slist,Flist, Awklist,protocolTime);
		senderThread.setDaemon(true);
		senderThread.start();
		System.out.println("Press any key + enter to leave");
		Scanner in = new Scanner(System.in);
		in.nextLine();
	}
}

