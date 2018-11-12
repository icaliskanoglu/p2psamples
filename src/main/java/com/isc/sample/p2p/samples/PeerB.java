package com.isc.sample.p2p.samples;

/**
 * Created by mac on 24/11/15.
 */
public class PeerB {
    public static void main(String[] args) throws Exception {
        Peer p = new Peer("B",1234,args[0],Integer.parseInt(args[1]));
        p.start();
    }
}
