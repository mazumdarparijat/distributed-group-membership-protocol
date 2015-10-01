package cs425.mp2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Transponder extends Thread{
    private static final int MAX_BYTE_LENGTH =1024;
	private final DatagramSocket socket;
    private final String idString;
    private final Set<String> membershipSet;
    private final ConcurrentHashMap<Info,Integer> infoMap;
    private AtomicInteger time;
    private AtomicBoolean ackReceived;

    public Transponder(DatagramSocket socket, String idStr, Set<String> membershipSet,
                       AtomicBoolean ackReceived, ConcurrentHashMap<Info,Integer> infoMap,
                       AtomicInteger time) {
        this.socket=socket;
        this.idString=idStr;
        this.membershipSet=membershipSet;
        this.infoMap=infoMap;
        this.time=time;
        this.ackReceived=ackReceived;
    }

    private void sendDatagramPacket(byte [] sendBytes, InetAddress address, int port) {
        DatagramPacket sendPacket = new DatagramPacket(sendBytes, sendBytes.length,
                address,port);

        try {
            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processInfoPackets(Message m) {
        for (Info i : m.getInfoList()){
            if (!i.param.equals(idString)) {
                if (i.type == Info.InfoType.JOIN) {
                    if (!this.membershipSet.contains(i.param)) {
                        this.infoMap.put(i, this.time.intValue() + (int) FailureDetector
                                .getSpreadTime(this.membershipSet.size()));
                        this.membershipSet.add(i.param);
                        System.out.println("[RECEIVER] [MEM_ADD] ["+System.currentTimeMillis()+"] : "+i.param);

                    }
                }
                else if (i.type == Info.InfoType.FAILED || i.type == Info.InfoType.LEAVE) {
                    if (this.membershipSet.contains(i.param)) {
                        this.infoMap.put(i, this.time.intValue() + (int) FailureDetector
                                .getSpreadTime(this.membershipSet.size()));
                        this.membershipSet.remove(i.param);
                        System.out.println("[RECEIVER] [MEM_REMOVE] [" + System.currentTimeMillis() + "] " +
                                ": Failure received : " + i.param);
                    }
                }

            }
        }
    }

	public void handleMsg(DatagramPacket receivePacket){
        // if ping - send ack if in membership list and write dissemination to dissemination buffer
        // if ack
        System.out.println("[RECEIVER] [INFO] ["+System.currentTimeMillis()+"] Message Received : "
                +new String(receivePacket.getData(),0,receivePacket.getLength()));

        Message m = Message.extractMessage(receivePacket.getData(),receivePacket.getLength());
        if (m.type==MessageType.PING) {
            this.processInfoPackets(m);
            byte[] sendBytes;
            if (membershipSet.contains(m.getMessageSenderID())) {
                sendBytes = Message.MessageBuilder
                        .buildAckMessage(m.getMessageKey())
                        .addInfoFromList(this.infoMap.keySet())
                        .getMessage()
                        .toByteArray();
            } else {
                sendBytes = Message.MessageBuilder
                        .buildMissingNoticeMessage()
                        .getMessage()
                        .toByteArray();
            }

            System.out.println("[RECEIVER] [INFO] ["+System.currentTimeMillis()+"] message sent : "
                    +new String(sendBytes,0,sendBytes.length));
            sendDatagramPacket(sendBytes, receivePacket.getAddress(), receivePacket.getPort());

        } else if (m.type==MessageType.ACK) {
            this.processInfoPackets(m);
            if (Integer.parseInt(m.getMessageKey())==time.intValue()) {
                ackReceived.set(true);
                synchronized (ackReceived) {
                    ackReceived.notify();
                }
            }

        } else if (m.type == MessageType.PING_REQUEST) {
            if (m.getReqDestination().equals(idString)) {
                byte [] sendBytes = Message.MessageBuilder
                        .buildAckReqMessage(m.getMessageKey(),m.getMessageSenderID())
                        .getMessage()
                        .toByteArray();
                System.out.println("[RECEIVER] [INFO] ["+System.currentTimeMillis()+"] message sent : "
                        +new String(sendBytes,0,sendBytes.length));
                sendDatagramPacket(sendBytes,receivePacket.getAddress(),receivePacket.getPort());
            } else {
                byte [] sendBytes = m.toByteArray();
                Pid destPid=Pid.getPid(m.getReqDestination());
                try {
                    System.out.println("[RECEIVER] [INFO] ["+System.currentTimeMillis()+"] message sent : "
                            +new String(sendBytes,0,sendBytes.length));
                    sendDatagramPacket(sendBytes, InetAddress.getByName(destPid.hostname), destPid.port);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        } else if (m.type == MessageType.ACK_REQUEST) {
            if (m.getMessageSenderID().equals(idString)) {
                if (Integer.parseInt(m.getMessageKey())==time.intValue()) {
                    ackReceived.set(true);
                    synchronized (ackReceived) {
                        ackReceived.notify();
                    }
                }
            } else {
                Pid destPid=Pid.getPid(m.getMessageSenderID());
                try {
                    System.out.println("[RECEIVER] [INFO] ["+System.currentTimeMillis()+"] message sent : "
                            +new String(m.toByteArray(),0,m.toByteArray().length));
                    sendDatagramPacket(m.toByteArray(), InetAddress.getByName(destPid.hostname), destPid.port);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        } else {
                throw new IllegalArgumentException("Message type not recognized");
        }
	}
	@Override
	public void run(){
		while(true){
            byte [] receiveData = new byte[MAX_BYTE_LENGTH];
			DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
            System.out.println("[RECEIVER] [INFO] ["+System.currentTimeMillis()+"] Waiting to receive next packet");
            try {
                socket.receive(receivedPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            handleMsg(receivedPacket);
		} 
	}
}
