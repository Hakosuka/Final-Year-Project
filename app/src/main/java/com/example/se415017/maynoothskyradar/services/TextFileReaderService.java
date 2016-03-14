package com.example.se415017.maynoothskyradar.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * This service reads from a text file which has about 3 minutes worth of SBS-1 messages received
 * from detected aircraft. This is to simulate new messages being received, just in case the
 * server goes down.
 *
 * @author Ciaran Cumiskey #12342236
 * @date 14 March 2016
 */
public class TextFileReaderService extends Service {
    public TextFileReaderService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
