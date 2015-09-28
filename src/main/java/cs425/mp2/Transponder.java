package cs425.mp2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Transponder extends Thread{
	public byte[] receiveData = new byte[1024];
	public byte[] sendData = new byte[1024]; 
	public DatagramSocket socket;
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
