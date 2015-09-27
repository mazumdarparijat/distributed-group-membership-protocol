package cs425.mp2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

public class Sender extends Thread{
	public DatagramSocket socket;
	public ConcurrentHashMap<Pid,Tuple> Mlist;
	public ConcurrentHashMap<String,Tuple> Jlist;
	public ConcurrentHashMap<String,Tuple> Slist;
	public ConcurrentHashMap<String,Tuple> Flist;
	public ConcurrentHashMap<String,Tuple> Awklist;
	byte[] sendData = new byte[1024];    
	
	public Sender(DatagramSocket socket,ConcurrentHashMap<Pid,Tuple> Mlist,ConcurrentHashMap<String,Tuple> Jlist
			,ConcurrentHashMap<String,Tuple> Slist,ConcurrentHashMap<String,Tuple> Flist
			,ConcurrentHashMap<String,Tuple> AwkList){
		this.socket=socket;
		this.Mlist=Mlist;
		this.Slist=Slist;
		this.Flist=Flist;
		this.Awklist=Awklist;
	}
	void sendMsg(String msg,)
	@Override
	public void run(){
		
		byte[] receiveData = new byte[1024]; 
		while(true){
			 int port=9090;
			 String message="hello";
			 sendData = message.getBytes();
			 DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			 try{
				 DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("localhost"), port);
				 socket.send(sendPacket);
			 }catch(IOException e){
				 e.printStackTrace();
			 }
			 String modifiedSentence = new String(receivePacket.getData());
			 System.out.println("FROM SERVER:" + modifiedSentence);
		}
	}
}
