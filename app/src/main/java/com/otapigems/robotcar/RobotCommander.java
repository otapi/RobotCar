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
    private BluetoothSocket robotSocket;
    private OutputStream outputStream;
    private InputStream inputStream;

    private boolean stopThread;

    public RobotCommander(BluetoothSocket robotSocket, OutputStream outputStream, InputStream inputStream){
        this.robotSocket = robotSocket;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        beginListenForData();
        //reset();
    }
    private byte intToUnsignedByte(int in) {
        return (byte) (in-(Integer.MAX_VALUE+1));
    }
    private byte[] shortToByteArray(int x) {
        short unsignedX = (short) x;
        byte res[]=new byte[2];
        res[0]= (byte)(((short)(unsignedX>>7)) & ((short)0x7f) | 0x80 );
        res[1]= (byte)((unsignedX & ((short)0x7f)));
        return res;
    }
    private byte[] addChar(byte[] a, char theNewChar) {
        return addByte(a, (byte) theNewChar);
    }
    private byte[] addByte(byte[] a, byte theNewByte) {
        byte[] c = new byte[a.length + 1];
        System.arraycopy(a, 0, c, 0, a.length);
        c[a.length] = theNewByte;
        return c;
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
        byte[] msg = new byte[2];
        msg[0] = (byte) 'd';
        msg[1] = intToUnsignedByte(distance);
        sendToRobot(msg);
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
        byte[] command = new byte[6];
        if (left < 0) {
            command[0] = 'l';
        } else {
            command[0] = 'L';
        }
        command[1] =intToUnsignedByte(Math.abs(left));

        if (right < 0) {
            command[2] = 'r';
        } else {
            command[2]='R';
        }
        command[3]=intToUnsignedByte(Math.abs(right));

        byte ut[] = shortToByteArray(timer);
        command[4] = ut[0];
        command[5] = ut[1];

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
        byte[] msg = new byte[2];
        msg[0] = (byte) 'n';
        msg[1] = intToUnsignedByte(position);
        sendToRobot(msg);
    }

    public void reset() {
        cmdAlertDistance(0);
        cmdStopDistance(5);
        cmdMotor(0,0,1);
        cmdNeckPosition(90);
    }



    public void forward() {
        cmdMotor(200,200,300);
    }

    void print(String message)
    {
        Log.d("RobotCommander", message);
    }

    public void sendToRobot(byte[] message) {

        try {
            outputStream.write(addChar(message, '@'));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


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
                String messageBuffer = "";
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
                            messageBuffer+= string;

                            while(messageBuffer.contains("\n")) {
                                final String outs = messageBuffer.substring(0,messageBuffer.indexOf("\n"));
                                messageBuffer = messageBuffer.substring(messageBuffer.indexOf("\n")+1);

                                handler.post(new Runnable() {
                                    public void run()
                                    {
                                        Log.d("Data incoming",outs);
                                    }
                                });
                            }


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


    public void close() throws IOException {
        reset();
        stopThread = true;
        outputStream.close();
        inputStream.close();
        robotSocket.close();
    }
}
