package cs425.mp2;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

// TODO check functionality
public class FDIntroducer extends FailureDetector {
	public FDIntroducer(int port){
        super();

        try {
            this.self_id=new Pid(InetAddress.getLocalHost().getHostName(),port,0);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        System.out.println("self id created : " + self_id.pidStr);
	}

    @Override
    public void startFD() {
        System.out.println("node started");
        NewJoinThread joiner=new NewJoinThread(this.self_id.port);
        joiner.setDaemon(true);
        joiner.start();
        this.runFD();
    }

    private class NewJoinThread extends Thread {
        private final int port;
        public NewJoinThread(int port) {
            this.port=port;
        }

        @Override
        public void run() {
            System.out.println("joiner thread started");
            ServerSocket tcp=null;
            try {
                tcp=new ServerSocket(this.port);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (true) {
                Socket joinRequest = null;
                try {
                    joinRequest=tcp.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Scanner inputReader = null;
                try {
                    inputReader = new Scanner(new InputStreamReader(joinRequest.getInputStream()));
                    inputReader.useDelimiter("\n");
                } catch (IOException e) {
                    System.err.println("[ERROR] Error creating input stream to introducer");
                    return;
                }
                PrintWriter outputWriter = null;
                try {
                    outputWriter = new PrintWriter(new OutputStreamWriter(joinRequest.getOutputStream()));
                } catch (IOException e) {
                    System.err.println("[ERROR] Error creating input stream from socket");
                    return;
                }

                String joinerID=inputReader.next();
                System.out.println("[JOINER THREAD] join requested by : " + joinerID);
                for (String member : membershipSet) {
                    outputWriter.println(member);
                }

                outputWriter.flush();

                try {
                    joinRequest.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                membershipSet.add(joinerID);
                System.out.println(joinerID + " added to membershipSet");
                infoBuffer.put(new Info(Info.InfoType.JOIN, joinerID), time.intValue()
                        + (int) getSpreadTime(membershipSet.size()));
                System.out.println(joinerID + " join added to infoBuffer");
            }
        }
    }
}
