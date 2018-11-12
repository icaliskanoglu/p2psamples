package com.isc.sample.p2p.samples;


import java.io.IOException;
import java.net.*;
import java.util.Random;

/**
 * Created by mac on 23/11/15.
 */
public class AbstractServer {
    protected static int packetSize = 2048;
    protected static String REGISTER = "REGISTER";
    protected static String CONNECT = "CONNECT";
    protected static String PUNCHED = "PUNCHED";
    protected static String PEER = "PEER";
    protected static String OK = "OK";
    protected static String FAIL = "FAIL";
    protected static String ECHO = "ECHO : ";

    protected static String HI = "HI : ";
    protected static String DELIMETER = ":";

    protected DatagramSocket socket;
    protected String peerName = "Server";
private int serverPort = -1;
    static final int range = ( 65535 - 49152 );
    public AbstractServer(){

    }
    public AbstractServer(String peerName ,int serverPort) {
        this(peerName);
        this.serverPort = serverPort;
    }

    public AbstractServer(String peerName) {
        this.peerName = peerName;
    }

    public void sendMessage(final String ip, final int serverPort, final String message) {
        sendMessage(socket,ip,serverPort,message);
    }
    public void sendMessage(DatagramSocket socket,final String ip, final int serverPort, final String message) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    byte[] buf;
                    String s = peerName +DELIMETER+message;
                    buf = s.getBytes();

                    DatagramPacket packet = new DatagramPacket(buf, buf.length, new InetSocketAddress(ip, serverPort));
                    socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void startServer(DatagramSocket socket,final ConversationWorker worker) {
        System.out.println(peerName + " server port " + socket.getLocalPort());
        new Thread(new Runnable() {
            public void run() {
                try {
                    DatagramPacket packet;
                    byte[] buf = new byte[packetSize];

                    while (socket.isBound()) {
                        packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        String received = new String(buf, 0, packet.getLength());

                        worker.work(received, packet.getAddress().getHostAddress(),packet.getPort());

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void startServer(final ConversationWorker worker) throws SocketException {
        if (serverPort != -1){
            socket = new DatagramSocket(serverPort);
        }
        else {
            socket = new DatagramSocket();
        }
        startServer(socket,worker);
    }

    interface ConversationWorker {
        boolean work(String message, String ip, int port);
    }


    class PeerHolder {
        String ip;
        int port;
        String peerName;
        public Thread th;
        public PeerHolder(String peerName,String ip, int port) {
            this.ip = ip;
            this.port = port;
            this.peerName = peerName;

        }

        public String getAddress()
        {
            return ip+DELIMETER+port ;
        }
    }

    public static synchronized Integer getPortNumber()
    {
        Random candidateInt = new Random();//
        int cadidatePort = (candidateInt.nextInt(49152) + range);
        if((cadidatePort < 49152) || (cadidatePort > 65535))
        {
            do
            {
                cadidatePort = (candidateInt.nextInt(49152) + range);
            }
            while((cadidatePort < 49152) || (cadidatePort > 65535));
            return new Integer(cadidatePort);
        }
        else
        {
            return new Integer(cadidatePort);
        }

    }
}
