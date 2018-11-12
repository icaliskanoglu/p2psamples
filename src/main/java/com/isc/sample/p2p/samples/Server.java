package com.isc.sample.p2p.samples;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class Server extends AbstractServer {

    private Map<String, PeerHolder> peerMap = new HashMap<String, Server.PeerHolder>();

    private static int serverPort = 5555;

    public Server(int serverPort) throws SocketException {
        super("Server", serverPort);
    }

    public static void main(String[] args) throws SocketException {
        final Server server = new Server(Integer.parseInt(args[0]));
        server.startServer(new ConversationWorker() {
            public boolean work(String message, String ip, int port) {
                System.out.println(ip+DELIMETER+port+DELIMETER+message);
                String[] args = message.split(DELIMETER);
                if (message.contains(ECHO)) {

                } else if (message.contains(REGISTER)) {
                    server.register(args[0], ip, port);
                    server.sendMessage(ip, port, OK);
                } else if (message.contains(PEER)) {
                    server.peer(args[0], args[2], ip, port);
                } else if (message.contains(PUNCHED)) {
                    server.punched(args[2]);
                }
                return false;
            }
        });
    }

    private void register(String name, String dip, int dport) {
        boolean th = peerMap.containsKey(name);
        PeerHolder ph = new AbstractServer.PeerHolder(name, dip, dport);
        peerMap.put(name, ph);
    }

    private void punched(String dst) {
        PeerHolder phDest = peerMap.get(dst);
        sendMessage(phDest.ip, phDest.port, PUNCHED);
    }

    private void peer(String srcName, String destName, String ip, int port) {

        PeerHolder phDest = peerMap.get(destName);
        PeerHolder phSrc = peerMap.get(srcName);

        System.out.println("PEER SRC " + srcName + DELIMETER + phSrc.getAddress());
        System.out.println("PEER DST " + destName + DELIMETER + phDest.getAddress());

        if (phDest != null) {
            sendMessage(phDest.ip, phDest.port, CONNECT + DELIMETER + phSrc.getAddress());
            sendMessage(phSrc.ip, phSrc.port, CONNECT + DELIMETER + phDest.getAddress());
        } else {
            sendMessage(ip, port, FAIL);
        }

    }
}
