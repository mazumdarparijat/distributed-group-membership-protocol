package cs425.mp2;

import java.io.IOException;

/**
 * Created by parijatmazumdar on 27/09/15.
 */
public enum MessageType {
    PING ('P'),
    PING_REQUEST ('Q'),
    ACK ('A'),
    ACK_REQUEST ('B'),
    MISSING_NOTICE ('M');

    private final char messagePrefix;
    MessageType(char p) {
        messagePrefix =p;
    }

    public char getMessagePrefix() {
        return messagePrefix;
    }

    public static MessageType getMessageType(char prefix) throws IOException {
        if (prefix=='P')
            return PING;
        else if (prefix=='Q')
            return PING_REQUEST;
        else if (prefix=='A')
            return ACK;
        else if (prefix=='B')
            return ACK_REQUEST;
        else if (prefix=='M')
            return MISSING_NOTICE;
        else
            throw new IOException("Message prefix supplied is not recognized!");
    }
}
