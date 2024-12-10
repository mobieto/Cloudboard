package com.whiteboard.whiteboardapp2;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Constants {
    public static final String WB_ACTION_PREFIX = "wbac:";
    public static final String WB_STATE_PREFIX = "wbst:";

    public static final String HOST_NAME;

    static {
        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostName = "UNKNOWN";
            throw new RuntimeException(e);
        }
        HOST_NAME = hostName;
    }
}
