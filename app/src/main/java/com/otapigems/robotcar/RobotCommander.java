package com.otapigems.robotcar;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by otapi_aocivry on 2017. 08. 06..
 */

public class RobotCommander {
    private byte intToUnsignedByte(int in) {
        return (byte) (in-Integer.MAX_VALUE);
    }
    /**
     * Alert back asynch if ultrasonic sensor sees something at this distance (cm) or closer. To disable, set it to 0 (by default)
     * @param distance in cm
     */
    public void cmdAlertDistance(int distance) {
        //// TODO: 2017. 08. 08. Implement this...
    }

    /**
     * Stop motors if something is ahead or closer (in cm). Default is 5. To disable, set this to 0
     * @param distance 0..255
     */
    public void cmdStopDistance(int distance) {
        if (distance<0 || distance > 255) {
            throw new IndexOutOfBoundsException("Distance should be between 0..255");
        }

        sendToRobot("d"+intToUnsignedByte(distance));
    }

    /**
     * Answer back the measured distance ahead
     * @return obstacle distance in cm
     */
    public byte cmdTellDistance() {
        /// TODO: 2017. 08. 08. Implement this...
        return 0;
    }

    /**
     * Run the motors. Negative is backward, 0 is stop
     * @param left -255...255
     * @param right -255...255
     * @param timer Stop motors when this time (milliseconds) runs out. Default is 300, 0..32,767
     */
    public void cmdMotor(int left, int right, int timer) {

        if (Math.abs(left)>255 || Math.abs(right)>255) {
            throw new IndexOutOfBoundsException("Motor parameters are out of bounds. Should be below 256.");
        }
        if (timer<0 || timer>32767) {
            throw new IndexOutOfBoundsException("Motor timer is out of bounds. Should be between 0..32,767.");
        }
        String command = "";
        if (left < 0) {
            command+= "l";
        } else {
            command+="L";
        }
        command+=intToUnsignedByte(Math.abs(left));

        if (right < 0) {
            command+= "r";
        } else {
            command+="R";
        }
        command+=intToUnsignedByte(Math.abs(right));

        short unsignedTimer = (short) ((short)timer- Short.MAX_VALUE);
        command+=ByteBuffer.allocate(2).putShort(unsignedTimer).array();
        sendToRobot(command);
    }

    /**
     * Set neck position. 0 is left, 90 is ahead, 180 is right
     * @param position degrees
     */
    public void cmdNeckPosition(int position) {
        if (position<0 || position > 180) {
            throw new IndexOutOfBoundsException("Neck position should between 0..180");
        }
        sendToRobot("n"+intToUnsignedByte(position));
    }

    public void reset() {
        cmdAlertDistance(0);
        cmdStopDistance(5);
        cmdMotor(0,0,1);
        cmdNeckPosition(90);
    }

    private BluetoothSocket robotSocket;
    private OutputStream outputStream;
    private InputStream inputStream;

    private boolean stopThread;

    public RobotCommander(BluetoothSocket robotSocket, OutputStream outputStream, InputStream inputStream){
        this.robotSocket = robotSocket;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        reset();
    }

    public void forward() {
        cmdMotor(255,255,300);
    }

    void print(String message)
    {
        Log.d("RobotCommander", message);
    }

    public void sendToRobot(String message) {
        message.concat("\n");
        try {
            outputStream.write(message.getBytes());
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
        reset();
        stopThread = true;
        outputStream.close();
        inputStream.close();
        robotSocket.close();
    }
}
