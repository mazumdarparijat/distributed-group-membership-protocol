package cs425.mp2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

public class Receiver extends Thread{
	public DatagramSocket socket;
	public ConcurrentHashMap<Pid,Tuple> Mlist;
	public ConcurrentHashMap<String,Tuple> Jlist;
	public ConcurrentHashMap<String,Tuple> Slist;
	public ConcurrentHashMap<String,Tuple> Flist;
	public ConcurrentHashMap<String,Tuple> Awklist;
	public Receiver(DatagramSocket socket,ConcurrentHashMap<Pid,Tuple> Mlist,ConcurrentHashMap<String,Tuple> Jlist
			,ConcurrentHashMap<String,Tuple> Slist,ConcurrentHashMap<String,Tuple> Flist
			,ConcurrentHashMap<String,Tuple> AwkList){
		this.socket=socket;
		this.Mlist=Mlist;
		this.Slist=Slist;
		this.Flist=Flist;
		this.Awklist=Awklist;
	}
	@Override
	public void run(){
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];             
		while(true){                   
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);                   
			try {
				socket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
			}                   
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
	}
}
