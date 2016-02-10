package com.example.se415017.maynoothskyradar.helpers;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InterfaceAddress;
import java.net.Socket;

/**
 * Created by se415017 on 09/11/2015.
 */
public class SBSDecoder {
    //TODO: Sockets sorted out, this is kind of useless
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
    public SBSDecoder(String serverURL, int serverPort){
        this.port = serverPort;
        this.drBrownsServer = serverURL;
        try {
            socket = new Socket(drBrownsServer, port);
            receive(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] receive(Socket s) throws IOException {
        //Redundant
        in = s.getInputStream();
        int result = in.read();
        for (int i = 0; i < 128; i++){
            buffer[i] = Byte.parseByte(Integer.toBinaryString(result), 2);
        }
        Log.d(getClass().toString(), buffer.toString());
        return buffer;
    }

    /**
     * This method takes in the Mode-S code and converts it to the aircraft's callsign
     * @param modeSString
     * @return callsign - the callsign of the aircraft
     */
    public String modeSToCallsign(String modeSString) {
        String callsign = "";
        return callsign;
    }
}
