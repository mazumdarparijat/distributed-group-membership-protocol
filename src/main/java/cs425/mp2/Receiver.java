package cs425.mp2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Receiver extends Thread{
	public byte[] receiveData = new byte[1024];
	public byte[] sendData = new byte[1024]; 
	public DatagramSocket socket;
	public ConcurrentHashMap<String,Tuple> Mlist;
	public ConcurrentHashMap<String,Tuple> Jlist;
	public ConcurrentHashMap<String,Tuple> Slist;
	public ConcurrentHashMap<String,Tuple> Flist;
	public ConcurrentHashMap<String,Tuple> Awklist;
	public AtomicInteger protocolTime;
	public Receiver(DatagramSocket socket,ConcurrentHashMap<String,Tuple> Mlist,ConcurrentHashMap<String,Tuple> Jlist
			,ConcurrentHashMap<String,Tuple> Slist,ConcurrentHashMap<String,Tuple> Flist
			,ConcurrentHashMap<String,Tuple> AwkList, AtomicInteger counter){
		this.socket=socket;
		this.Mlist=Mlist;
		this.Slist=Slist;
		this.Flist=Flist;
		this.Awklist=Awklist;
		protocolTime=counter;
	}
	public void handleMsg(DatagramPacket receivePacket){
		String sentence = new String( receivePacket.getData());
		System.out.println("RECEIVED: " + sentence);                   
		InetAddress IPAddress = receivePacket.getAddress();                   
		int port = receivePacket.getPort();                   
		String capitalizedSentence = sentence.toUpperCase();                   
		sendData = capitalizedSentence.getBytes();                  
		DatagramPacket sendPacket =                  
				new DatagramPacket(sendData, sendData.length, IPAddress, port);                   
		try {
			socket.send(sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void run(){            
		while(true){                   
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);                   
			try {
				socket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			handleMsg(receivePacket);
		} 
	}
}
