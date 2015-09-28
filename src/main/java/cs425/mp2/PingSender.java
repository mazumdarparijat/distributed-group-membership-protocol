package cs425.mp2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PingSender extends Thread{
	private DatagramSocket socket;
	public AtomicInteger protocolTime;


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
			try {
				Thread.sleep(pingWait);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//if message not in awklist
			//send ping_requests

			try {
				Thread.sleep(pingWait);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
