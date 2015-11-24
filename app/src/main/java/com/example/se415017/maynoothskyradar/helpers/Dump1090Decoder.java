package com.example.se415017.maynoothskyradar.helpers;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by se415017 on 09/11/2015.
 */
public class Dump1090Decoder {
    //TODO: sort out sockets
    private Socket socket;
    private int port = 9999; // default
    private String drBrownsServer = "192.168.1.1"; // default
    OutputStream out = null;
    InputStream in = null;
    byte[] buffer = new byte[128]; // adsMonitor.py uses 128-byte buffer
    /**
     * This constructs a decoder.
     * @param serverURL (the URL of the server I want to stream from), serverPort (the port I want to use)
     */
    public Dump1090Decoder(String serverURL, int serverPort){
        this.port = serverPort;
        this.drBrownsServer = serverURL;
        try {
            socket = new Socket(drBrownsServer, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] receive() throws IOException {
        //TODO: find some way of constantly streaming data
        in = socket.getInputStream();
        buffer = in.;
    }

    /**
     * This method takes in the Mode-S code and converts it to the aircraft's callsign
     * @param modeSString
     * @return
     */
    public String modeSToCallsign(String modeSString) {
        String callsign = "";
        return callsign;
    }
}
