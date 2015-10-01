package cs425.mp2;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.*;
import java.util.Collections;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FailureDetector {
    private final long PING_TIME_OUT=1000;
    private final long PROTOCOL_TIME=5000;
    private final int MAX_NODES=10;
    private final int CONCURRENCY_LEVEL=2;
    private final float LOAD_FACTOR= (float) 0.75;

    protected AtomicInteger time=new AtomicInteger(0);
    private AtomicBoolean ackReceived=new AtomicBoolean(false);

	private Pid introducer_id;
    protected ConcurrentHashMap<Info,Integer> infoBuffer;
    protected Set<String> membershipSet;
    protected Pid self_id;

    protected FailureDetector() {
        membershipSet= Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>
                (MAX_NODES,LOAD_FACTOR,CONCURRENCY_LEVEL));
        infoBuffer=new ConcurrentHashMap<Info, Integer>(MAX_NODES,LOAD_FACTOR,CONCURRENCY_LEVEL);
    }

	public FailureDetector(int port, String intro_address, int intro_port){
        this();
        try {
            self_id=new Pid(InetAddress.getLocalHost().getHostName(),port,System.currentTimeMillis());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        System.out.println("[MAIN] [INFO] ["+System.currentTimeMillis()+"] : node created with id : "+self_id.pidStr);
        introducer_id=new Pid(intro_address,intro_port,0);
        membershipSet.add(introducer_id.pidStr);
        System.out.println("[MAIN] [MEM_ADD] ["+System.currentTimeMillis()+"] : "+introducer_id.pidStr);
	}

    public void startFD() {
        // get membership list from introducer over TCP
        Socket tcpConnection=null;
        try {
            tcpConnection=new Socket(this.introducer_id.hostname,this.introducer_id.port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scanner inputReader = null;
        try {
            inputReader = new Scanner(new InputStreamReader(tcpConnection.getInputStream()));
            inputReader.useDelimiter("\n");
        } catch (IOException e) {
            System.err.println("[MAIN] [ERROR] Error creating input stream to introducer");
            return;
        }
        PrintWriter outputWriter = null;
        try {
            outputWriter = new PrintWriter(new OutputStreamWriter(tcpConnection.getOutputStream()));
        } catch (IOException e) {
            System.err.println("[MAIN] [ERROR] Error creating input stream from socket");
            return;
        }

        System.out.println("[MAIN] [INFO] ["+System.currentTimeMillis()+"] : tcp connection initiated");
        outputWriter.println(this.self_id.pidStr);
        outputWriter.flush();

        while (inputReader.hasNext()) {
            String newMember=inputReader.next();
            System.out.println("[MAIN] [MEM_ADD] ["+System.currentTimeMillis()+"] : "+newMember);
            membershipSet.add(newMember);
        }

        this.runFD();
    }

	protected void runFD() {
        DatagramSocket socket=null;
		try {
			socket = new DatagramSocket(self_id.port);
		} catch (SocketException e) {
			e.printStackTrace();
		}

        System.out.println("[MAIN] [INFO] [" + System.currentTimeMillis() + "] : udp socket initiated");
		Thread receiverThread = new Transponder(socket,self_id.pidStr,membershipSet,ackReceived,infoBuffer,time);
		receiverThread.setDaemon(true);
		receiverThread.start();
        System.out.println("[MAIN] [INFO] [" + System.currentTimeMillis() + "] : receiver thread started");

        // wait for log N cycles before ping sending
        try {
            Thread.sleep((long) (PROTOCOL_TIME*FailureDetector.getSpreadTime(membershipSet.size())));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Thread senderThread = new PingSender(socket,membershipSet,ackReceived,infoBuffer,
                self_id.pidStr,time,PING_TIME_OUT,PROTOCOL_TIME);
		senderThread.setDaemon(true);
		senderThread.start();
        System.out.println("[MAIN] [INFO] [" + System.currentTimeMillis() + "] : sender thread added");
		System.out.println("Press any key followed by enter to leave");
		Scanner in = new Scanner(System.in);
		in.nextLine();
	}

    public static double getSpreadTime(int numMembers) {
        if (numMembers==0)
            return 0;

        double ret=3.0*Math.log(numMembers)/Math.log(2.0)+1.0;
        return ret;
    }
}

