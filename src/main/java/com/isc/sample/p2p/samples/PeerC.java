package com.isc.sample.p2p.samples;

/**
 * Created by mac on 24/11/15.
 */
public class PeerC {
    public static void main(String[] args) throws Exception {
        Peer p = new Peer("C",4545,"213.14.92.21",1234);
        p.sendMessage("10.6.0.79",1234,"asasas");
    }
}
