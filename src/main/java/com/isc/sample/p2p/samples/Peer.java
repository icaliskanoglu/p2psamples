package com.isc.sample.p2p.samples;

import net.java.stun4j.StunAddress;
import net.java.stun4j.StunException;
import net.java.stun4j.client.SimpleAddressDetector;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.nat.FutureNAT;
import net.tomp2p.nat.FutureRelayNAT;
import net.tomp2p.nat.NATUtils;
import net.tomp2p.nat.PeerNAT;
import net.tomp2p.natpmp.NatPmpException;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;

import java.net.DatagramSocket;

//bamsibeyrek.com
public class Peer extends AbstractServer {

    private int serverPort = 5555;
    private String serverIp = "127.0.0.1";
    private String destinationPeer = "B";
    private int hiCounter = 1;
    private int publicPort;
    private int peerPort;
    private PeerHolder ph;
    private boolean connectionRequest = false;

    public Peer() {

    }

    public Peer(String peerName, String serverIp, int serverPort) throws Exception {
        this(peerName, getPortNumber(), serverIp, serverPort);
    }

    public Peer(String peerName, int peerPort, String serverIp, int serverPort) throws Exception {
        super(peerName ,peerPort);
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.peerPort = peerPort;
        init();
    }

    private void init() throws Exception {



    }

    private void connectionRequestToServer() {
        connectionRequest = true;
        sendMessage(serverIp, serverPort, PEER + DELIMETER + destinationPeer);
    }


    private void registerToServer() {
        sendMessage(serverIp, serverPort, REGISTER);

    }

    private void openHoleToPeer(String ip, int port) {

        for (int i = 0; i < 5; i++) {
            for (int j = port-100; j < port+100; j++) {
                sendMessage( ip, j, REGISTER);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    private void sayHiToPeer(String ip, int port) {
        sendMessage( ip, port, HI + hiCounter);
        hiCounter++;

    }

    private void startLocalServer() throws Exception {

        startServer(new ConversationWorker() {
                        public boolean work(String message, String ip, int port) {
                            System.out.println(ip+DELIMETER+port+DELIMETER+message);
                            if (message.contains(ECHO)) {

                            } else if (message.contains(CONNECT)) {
                                String[] args = message.split(DELIMETER);
                                String destip = args[2];
                                int destport = Integer.parseInt(args[3]);
                                ph = new AbstractServer.PeerHolder(destinationPeer, destip, destport);
                                if (!connectionRequest)
                                    openHoleToPeer(destip, destport);
                                else sayHiToPeer(ph.ip, ph.port);
                            }  else if (message.contains("Server:OK")) {
                                if (peerName.equals("A")) {

                                    destinationPeer = "B";
                                    connectionRequestToServer();
                                } else {
                                    destinationPeer = "A";
                                }
                            }else if (message.contains(HI) && hiCounter < 10) {
                                sayHiToPeer(ph.ip, ph.port);
                            } else {
                                sendMessage(ip, port, ECHO + message);
                            }
                            return true;
                        }
                    }
        );
        portMap();
    }

    public void start() throws Exception{

        startLocalServer();

        registerToServer();

    }

    public void portMap() throws Exception {
        NATUtils nu = new NATUtils();
        publicPort = getPublicPort(socket);
        nu.mapPMP(peerPort,peerPort,publicPort,publicPort);
    }



    public static int getPublicPort(DatagramSocket datagramSocket) {
        int port = -1;
        try {
            SimpleAddressDetector detector = new SimpleAddressDetector(
                    new StunAddress("stun.l.google.com", 19302));
            detector.start();
            StunAddress mappedAddr = detector.getMappingFor(datagramSocket);

            System.out.println("address is " + mappedAddr);
            port = mappedAddr.getSocketAddress().getPort();
            detector.shutDown();
        } catch (StunException e) {
            e.printStackTrace();
        }
        return port;
    }

    public static void main(String[] args) throws Exception {


    }
}
