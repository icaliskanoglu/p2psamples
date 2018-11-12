package com.isc.sample.p2p.samples;

/**
 * Created by mac on 24/11/15.
 */
public class PeerA {
    public static void main(String[] args) throws Exception {
        Peer p = new Peer("A",1235,args[0],Integer.parseInt(args[1]));
        p.start();
    }
}
