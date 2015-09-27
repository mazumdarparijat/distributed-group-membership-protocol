package cs425.mp2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Sender extends Thread{
	public DatagramSocket socket;
	public ConcurrentHashMap<String,Tuple> Mlist;
	public ConcurrentHashMap<String,Tuple> Jlist;
	public ConcurrentHashMap<String,Tuple> Slist;
	public ConcurrentHashMap<String,Tuple> Flist;
	public ConcurrentHashMap<String,Tuple> Awklist;
	public AtomicInteger protocolTime;
	byte[] sendData = new byte[1024];    
	
	public Sender(DatagramSocket socket,ConcurrentHashMap<String,Tuple> Mlist,ConcurrentHashMap<String,Tuple> Jlist
			,ConcurrentHashMap<String,Tuple> Slist,ConcurrentHashMap<String,Tuple> Flist
			,ConcurrentHashMap<String,Tuple> AwkList, AtomicInteger counter){
		this.socket=socket;
		this.Mlist=Mlist;
		this.Slist=Slist;
		this.Flist=Flist;
		this.Awklist=Awklist;
		protocolTime=counter;
	}
	void sendMsg(String msg,InetAddress add, int port){
		 sendData = msg.getBytes();
		 try{
			 DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, add, port);
			 socket.send(sendPacket);
		 }catch(IOException e){
			 e.printStackTrace();
		 }
	}
	@Override
	public void run(){
		long currtime= System.currentTimeMillis();
		long pingWait = 10;
		while(true){
			int counter = protocolTime.incrementAndGet();
			//get some id
			try {
				sendMsg("Ping", InetAddress.getByName("id"),9090);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Thread.sleep(pingWait);
			//if message not in awklist
			//send ping_requests
			
			Thread.sleep(pingWait*K);
			//do stuff
			while(true){
				if(System.currentTimeMillis()-currtime > 50){
					currtime=System.currentTimeMillis();
					break;
				}
			}
		}
	}
}
