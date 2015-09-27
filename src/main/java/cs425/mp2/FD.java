package cs425.mp2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class FD{
	private Pid introducer_id;
	public Pid self_id;
	public DatagramSocket socket;
	public long timestamp;
	public ConcurrentHashMap<Pid,Tuple> Mlist;
	public ConcurrentHashMap<String,Tuple> Jlist;
	public ConcurrentHashMap<String,Tuple> Slist;
	public ConcurrentHashMap<String,Tuple> Flist;
	public ConcurrentHashMap<String,Tuple> Awklist;
	public void addMember(Pid p, Tuple t){
		Mlist.put(p, t);
	}
	public FD(int port, String iadd, int iport){
		timestamp=System.currentTimeMillis();
		try {
			self_id=new Pid(InetAddress.getLocalHost().getHostName(),port,timestamp);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		introducer_id=new Pid(iadd,iport,0);
		addMember(new Pid(iadd,iport,0),new Tuple("ALIVE", 0));
	}
	public FD(){
		
	}
	public void runFD() throws SocketException {
		try {
			socket = new DatagramSocket(self_id.port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		Thread receiverThread = new Receiver(socket,Mlist,Jlist,Slist,Flist, Awklist);
		receiverThread.setDaemon(true);
		receiverThread.start();
		Thread senderThread = new Sender(socket,Mlist,Jlist,Slist,Flist, Awklist);
		senderThread.setDaemon(true);
		senderThread.start();
		System.out.println("Press any key + enter to leave");
		Scanner in = new Scanner(System.in);
		in.nextLine();
	}
}

