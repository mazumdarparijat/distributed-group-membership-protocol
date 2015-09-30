package cs425.mp2;

import java.io.IOException;

/**
 * Created by parijatmazumdar on 29/09/15.
 */
public class Info {
    private static char PARAM_DELIM=' ';
    public enum InfoType {
        JOIN ('J'),
        LEAVE ('L'),
        FAILED ('F'),
        ALIVE ('V');

        public final char prefix;
        InfoType(char c) {
            prefix=c;
        }

        public static InfoType getInfoType(char prefix) throws IOException {
            if (prefix=='J')
                return JOIN;
            else if (prefix=='L')
                return LEAVE;
            else if (prefix=='F')
                return FAILED;
            else if (prefix=='V')
                return ALIVE;
            else
                throw new IOException("Message prefix supplied is not recognized!");
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("");
        sb.append(type.prefix).append(PARAM_DELIM).append(param);
        return sb.toString();
    }

    public static Info fromString(String infoAsString) throws IOException {
        String [] tokens=infoAsString.split(" ");
        assert tokens.length==2 : "Expected size is 2";
        return new Info(Info.InfoType.getInfoType(tokens[0].charAt(0)),tokens[1]);
    }

    public final InfoType type;
    public final String param;
    Info(InfoType type, String param) {
        this.type=type;
        this.param=param;
    }
}
