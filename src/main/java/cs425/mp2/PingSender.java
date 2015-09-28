package cs425.mp2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PingSender extends Thread{
    private static final int SUBGROUP_K=2;
	private final DatagramSocket socket;
    private final long pingTimeOut;
    private final long protocolTime;
    private final Set<String> memberSet;
    private final String idString;
    private AtomicInteger time;
    private CountDownLatch ackReceived;

    public PingSender(DatagramSocket socket, Set<String> memberSet, String idStr,
                      AtomicInteger time, CountDownLatch ackReceived,
                      long pingTimeOut, long protocolTime) {
        this.socket=socket;
        this.pingTimeOut=pingTimeOut;
        this.protocolTime=protocolTime;
        this.memberSet=memberSet;
        this.idString=idStr;
        this.time=time;
    }

    private void sendPing(String destID,AtomicInteger counterKey) {
        byte [] sendData = Message.MessageBuilder
                .buildPingMessage(String.valueOf(counterKey.get()),idString)
                .getMessage()
                .toByteArray();

        Pid destination = Pid.getPid(destID);
        sendMessage(sendData,destination);
    }

    private void sendPingReq(String relayerID, String destID,AtomicInteger counter) {
        byte [] sendData = Message.MessageBuilder
                .buildPingReqMessage(String.valueOf(counter.get()),idString,destID)
                .getMessage()
                .toByteArray();

        Pid destination = Pid.getPid(relayerID);
        sendMessage(sendData,destination);
    }

	private void sendMessage(byte [] sendData, Pid destination){
		 try{
			 DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                     InetAddress.getByName(destination.hostname),destination.port);
			 socket.send(sendPacket);
		 }catch(IOException e){
			 e.printStackTrace();
		 }
	}

    ListIterator<String> getShuffledMembers() {
        // TODO handle empty memberSet
        List<String> shuffledMembers=new ArrayList<String>(memberSet);
        Collections.shuffle(shuffledMembers);
        ListIterator<String> iterator=shuffledMembers.listIterator();
        return iterator;
    }

	@Override
	public void run(){
        ListIterator<String> shuffledIterator=getShuffledMembers();

		while(true){
            long startTime=System.currentTimeMillis();

            if (!shuffledIterator.hasNext()) {
                shuffledIterator=getShuffledMembers();
                continue;
            }

            String pingMemberID=shuffledIterator.next();

            // skip if shuffledList contains a member which is deleted from memberSet in between
            if (!memberSet.contains(pingMemberID))
                continue;

            time.getAndIncrement();
            ackReceived=new CountDownLatch(1);
            sendPing(pingMemberID, time);

            boolean hasReceived=false;
            try {
                hasReceived=ackReceived.await(pingTimeOut, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (hasReceived)
                continue;


            //if message not in awklist
			//send ping_requests
            ackReceived=new CountDownLatch(1);
            ListIterator<String> shuffledk=getShuffledMembers();
            for (int i=0;i<SUBGROUP_K;i++) {
                if (!shuffledk.hasNext())
                    break;

                String nextMember=shuffledk.next();
                if (!memberSet.contains(nextMember)) {
                    i--;
                    continue;
                }

                sendPingReq(nextMember,pingMemberID,time);
            }

            hasReceived=false;
            try {
                hasReceived=ackReceived.await(startTime+protocolTime-System.currentTimeMillis(),
                        TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!hasReceived) {
                // Todo suspect
            }
		}
	}
}
