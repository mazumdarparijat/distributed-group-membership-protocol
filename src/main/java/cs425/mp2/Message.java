package cs425.mp2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by parijatmazumdar on 27/09/15.
 */
public class Message {
    private final char PARAM_DELIM=' ';
    private final char INFO_DELIM=';';
    public final MessageType type;
    private final String [] messageParams;
    private List<Info> infoAttached;

    private enum ParamsFields {
        messageKey (0),
        senderID (1),
        destinationID (2);

        public final int index;
        ParamsFields(int index) {
            this.index=index;
        }
    }

    private Message (MessageType type, String [] params) {
        this.type=type;
        messageParams=params;
        infoAttached=new ArrayList<Info>();
    }

    public byte [] toByteArray() {
        StringBuilder builder=new StringBuilder();
        builder.append(type.getMessagePrefix());
        for (String param : messageParams)
            builder.append(PARAM_DELIM).append(param);

        for (Info i : infoAttached) {
            builder.append(INFO_DELIM).append(i.toString());
        }

        byte [] ret = builder.toString().getBytes();
        assert ret.length<1024 : "FATAL ERROR ! byte overflow";
        return ret;
    }

    public static Message extractMessage(byte[] messageBytes) {
        System.out.println("Message received : " + new String(messageBytes));
        String[] tokens=new String(messageBytes).split(";");
        String[] mStr=tokens[0].split(" ");
        MessageType type = null;
        try {
            type=MessageType.getMessageType(mStr[0].charAt(0));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        String [] params = new String[mStr.length-1];
        for (int i=1;i<params.length;i++)
            params[i-1]=mStr[i];

        Message ret=new Message(type,params);
        for (int i=1;i<tokens.length;i++) {
            try {
                ret.infoAttached.add(Info.fromString(tokens[1]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    public String getMessageSenderID() {
        assert messageParams.length>ParamsFields.senderID.index : "This type of message " +
                "does not have senderID as param";
        return messageParams[ParamsFields.senderID.index];
    }

    public String getMessageKey() {
        assert messageParams.length>ParamsFields.messageKey.index : "This type of message " +
                "does not have senderID as param";
        return messageParams[ParamsFields.messageKey.index];
    }

    public String getReqDestination() {
        assert messageParams.length>ParamsFields.destinationID.index : "This type of message " +
                "does not have senderID as param";
        return messageParams[ParamsFields.destinationID.index];
    }

    public List<Info> getInfoList() {
        return this.infoAttached;
    }

    public static class MessageBuilder {
        private Message m;
        public MessageBuilder(MessageType type, String [] args) {
            m=new Message(type,args);
        }
        public static MessageBuilder buildPingMessage(String messageKey,String senderID) {
            String [] args=new String[2];
            args[ParamsFields.messageKey.index]=messageKey;
            args[ParamsFields.senderID.index]=senderID;

            MessageBuilder newInstance=new MessageBuilder(MessageType.PING,args);
            return newInstance;
        }

        public static MessageBuilder buildPingReqMessage(String messageKey,String senderID,String destinationID) {
            String [] args=new String[3];
            args[ParamsFields.messageKey.index]=messageKey;
            args[ParamsFields.senderID.index]=senderID;
            args[ParamsFields.destinationID.index]=destinationID;

            MessageBuilder newInstance=new MessageBuilder(MessageType.PING_REQUEST,args);
            return newInstance;
        }

        public static MessageBuilder buildAckReqMessage(String messageKey,String senderID) {
            String [] args=new String[2];
            args[ParamsFields.messageKey.index]=messageKey;
            args[ParamsFields.senderID.index]=senderID;

            MessageBuilder newInstance=new MessageBuilder(MessageType.ACK_REQUEST,args);
            return newInstance;
        }

        public static MessageBuilder buildAckMessage(String ackID) {
            String [] args=new String[1];
            args[ParamsFields.messageKey.index]=ackID;

            MessageBuilder newInstance=new MessageBuilder(MessageType.ACK,args);
            return newInstance;
        }

        public static MessageBuilder buildMissingNoticeMessage() {
            String [] args=new String[0];

            MessageBuilder newInstance=new MessageBuilder(MessageType.MISSING_NOTICE,args);
            return newInstance;
        }

        public MessageBuilder addInfoFromList(Collection<Info> infos) {
            for (Info i : infos) {
                this.m.infoAttached.add(i);
            }

            return this;
        }

        public MessageBuilder addJoinInfo(String joinerID) {
            this.m.infoAttached.add(new Info(Info.InfoType.JOIN, joinerID));
            return this;
        }

        public MessageBuilder addLeaveInfo(String leaverID) {
            this.m.infoAttached.add(new Info(Info.InfoType.JOIN, leaverID));
            return this;
        }

        public final Message getMessage() {
            return m;
        }
    }
}
