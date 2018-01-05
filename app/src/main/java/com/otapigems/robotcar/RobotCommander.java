/*
    RobotCar.RobotCommander
    Communication layer via Bluetooth to control a rolling robot.
    Copyright (C) 2018  Barnabás Nagy - otapiGems.com - otapiGems@protonmail.ch

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.otapigems.robotcar;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RobotCommander {
    private BluetoothSocket robotSocket;
    private OutputStream outputStream;
    private InputStream inputStream;

    private boolean stopThread;
    private Thread listeningThread;

    public RobotCommander(BluetoothSocket robotSocket, OutputStream outputStream, InputStream inputStream){
        this.robotSocket = robotSocket;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        beginListenForData();
        reset();
    }
    private byte intToUnsignedByte(int in) {
        return (byte) (in);
    }
    private byte[] shortToByteArray(int x) {
        byte res[]=new byte[2];
        res[0] = (byte) (x);
        res[1] = (byte) ((x >> 8) & 0xff);
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
    private void sleep(int milliseconds) {
        try{ Thread.sleep(milliseconds); }catch(InterruptedException e){ }
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

    String firmwareVersion;
    public void cmdAskFirmwareVersion() {
        firmwareVersion = null;
        sendToRobot("V");
    }

    /**
     * Answer back the measured distance ahead
     * @return obstacle distance in cm
     */
    public String cmdTellFirmwareVersion() throws InterruptedException {
        int timeout = 30;

        while (firmwareVersion == null) {
            sleep(100);
            timeout--;
            if (timeout == 0) {
                return "Could not retrieve the version";
            }
        }

        return firmwareVersion;
    }
    void setFirmware(String fw) {
        firmwareVersion = fw;
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
     * Set neck position. 0 is left, 90 is ahead, 179 is right
     * @param position degrees
     */
    public void cmdNeckPosition(int position) {
        if (position<0 || position > 179) {
            throw new IndexOutOfBoundsException("Neck position should between 0..180");
        }
        byte[] msg = new byte[2];
        msg[0] = (byte) 'n';
        msg[1] = (byte) (180 - intToUnsignedByte(position));
        sendToRobot(msg);
    }

    public void reset() {
        cmdAlertDistance(0);
        cmdStopDistance(5);
        cmdMotor(0,0,1);
        cmdNeckPosition(90);
    }


    public void stop() {
        cmdMotor(0,0,1);
    }
    public void forward() {
        cmdMotor(255,255,300);
    }
    public void backward() {
        cmdMotor(-255,-255,300);
    }
    public void turnLeft() {
        cmdMotor(-255,255,200);
    }
    public void turnRight() {
        cmdMotor(255,-255,200);
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

    public void sendToRobot(String message) {
        byte[] msg = new byte[message.length()];
        for (int i = 0; i < message.length(); i++) {
            msg[i] = (byte) message.charAt(i);
        }
        sendToRobot(msg);
    }


    void beginListenForData()
    {

        final Handler handler = new Handler();
        stopThread = false;
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
                                        print(outs);
                                        if (outs.startsWith("@V@")) {
                                            setFirmware(outs.substring((3)));
                                        }

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
