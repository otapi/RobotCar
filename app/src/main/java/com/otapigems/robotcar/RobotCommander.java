package com.otapigems.robotcar;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by otapi_aocivry on 2017. 08. 06..
 */

public class RobotCommander {
    class RobotCommAsk {
        /**
         * Alert back asynch if ultrasonic sensor sees something at this distance (cm) or closer. To disable, set it to 0 (by default)
         */
        public float alertDistance = 0;
        /**
         * Stop motors if something is ahead or closer (in cm). Default is 1. To disable, set this to -1
         */
        public float stopDistance = 5;
        /**
         * Answer back the measured distance ahead (in cm)
         */
        public boolean tellDistance = false;
        /**
         * Move right motor by speed between -255 and 255.
         */
        public int motorRight = 0;
        /**
         * Move right motor by speed between -255 and 255
         */
        public int motorLeft = 0;
        /**
         * Stop motors when this time (milliseconds) runs out. Default is 300
         */
        public int motorTimer = 300;
        /**
         * Set the neckPosition. 0=left, 90 ahead (default), 180=right
         */
        public int neckPosition = 90;
    }
    class RobotCommAnswer {
        /**
         * Measured distance ahead (cm)
         */
        public float distance;
        /**
         * Motors stopped (upon stopDistance)
         */
        public boolean motorsStopped = false;
        /**
         * Any message
         */
        public String message = null;
    }

    public RobotCommAsk robotCommAsk;
    private BluetoothSocket robotSocket;
    private OutputStream outputStream;
    private InputStream inputStream;

    private boolean stopThread;

    public RobotCommander(BluetoothSocket robotSocket, OutputStream outputStream, InputStream inputStream){
        this.robotSocket = robotSocket;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        robotCommAsk = new RobotCommAsk();
        sendToRobot();
    }

    public void forward() {
        robotCommAsk.motorLeft = 255;
        robotCommAsk.motorRight = 255;
        sendToRobot();
        robotCommAsk.motorLeft = 0;
        robotCommAsk.motorRight = 0;
    }

    void print(String message)
    {
        Log.d("RobotCommander", message);
    }

    public void sendToRobot() {
        Gson gson = new Gson();
        String json = gson.toJson(robotCommAsk);

        //json.concat("\n");
        try {
            outputStream.write(json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        /*
    void beginListenForData()
    {
        Thread listeningThread;
        byte buffer[];

        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];

        listeningThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread)
                {
                    try
                    {
                        int byteCount = inputStream.available();
                        if(byteCount > 0)
                        {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string=new String(rawBytes,"UTF-8");
                            handler.post(new Runnable() {
                                public void run()
                                {
                                    Log.d("Data incoming",string);
                                }
                            });

                        }
                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }
                }
            }
        });

        listeningThread.start();
    }
    */

    public void close() throws IOException {
        stopThread = true;
        outputStream.close();
        inputStream.close();
        robotSocket.close();
    }
}
