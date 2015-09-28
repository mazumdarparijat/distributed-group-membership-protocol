package cs425.mp2;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class FailureDetector {
    private final long PING_TIME_OUT=10;
    private final long PROTOCOL_TIME=50;

    private final int MAX_NODES=10;
    private final int CONCURRENCY_LEVEL=2;
    private final float LOAD_FACTOR= (float) 0.75;

    private AtomicInteger time=new AtomicInteger(0);
    private CountDownLatch ackReceived=new CountDownLatch(1);

	private Pid introducer_id;
	protected Pid self_id;
    private Set<String> membershipSet;

	public FailureDetector(int port, String intro_address, int intro_port){
        try {
            self_id=new Pid(InetAddress.getLocalHost().getHostName(),port,System.currentTimeMillis());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        introducer_id=new Pid(intro_address,intro_port,0);
        membershipSet= Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>
                (MAX_NODES,LOAD_FACTOR,CONCURRENCY_LEVEL));
	}

	public void runFD() throws SocketException {
        DatagramSocket socket=null;
		try {
			socket = new DatagramSocket(self_id.port);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		Thread receiverThread = new Transponder(socket,self_id.pidStr,membershipSet,time,ackReceived);
		receiverThread.setDaemon(true);
		receiverThread.start();
		Thread senderThread = new PingSender(socket,membershipSet,self_id.pidStr,time,ackReceived,PING_TIME_OUT,PROTOCOL_TIME);
		senderThread.setDaemon(true);
		senderThread.start();
		System.out.println("Press any key followed by enter to leave");
		Scanner in = new Scanner(System.in);
		in.nextLine();
	}
}

