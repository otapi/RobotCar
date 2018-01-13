/*
    RobotCar
    Android app to control a rolling robot.

    Copyright (C) 2018  Barnabás Nagy - otapiGems.com - otapiGems@protonmail.ch

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.otapigems.robotcar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    RobotCommander robotCommander = null;

    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    SeekBar wheelBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectBluetooth(null);

        Button b = (Button) findViewById(R.id.btnUp);
        b.setOnTouchListener(onTouchHandler);
        b = (Button) findViewById(R.id.btnDown);
        b.setOnTouchListener(onTouchHandler);
        b = (Button) findViewById(R.id.btnRigh);
        b.setOnTouchListener(onTouchHandler);
        b = (Button) findViewById(R.id.btnLeft);
        b.setOnTouchListener(onTouchHandler);
        b = (Button) findViewById(R.id.btnStop);
        b.setOnTouchListener(onTouchHandler);

        SeekBar s = (SeekBar) findViewById(R.id.headBar);
        s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                robotCommander.cmdNeckPosition(seekBar.getProgress());
            }

            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        wheelBar = (SeekBar) findViewById(R.id.wheelBar);
        wheelBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(100);
            }
        });
    }



    private Thread cmdThread;
    boolean stopThread;
    View.OnTouchListener onTouchHandler = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            stopThread = false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d("Connection", "down");
                    if (cmdThread != null && cmdThread.isAlive()) {
                        Log.d("Connection", "Thread is active");
                        cmdThread.interrupt();
                        try {
                            cmdThread.join();
                        } catch (InterruptedException e) {

                        }

                    }
                    final String command = v.getTag().toString();
                    cmdThread = new Thread(new Runnable() {
                        public void run() {
                            while (!Thread.currentThread().isInterrupted() && !stopThread) {
                                switch (command) {
                                    case "up":
                                        //robotCommander.forward();
                                        int left = 255;
                                        int right = 255;
                                        int wheel = wheelBar.getProgress();
                                        if (wheel <100) {
                                            left = 255-(240*(100-wheel)/100);

                                        }
                                        if (wheel >100) {
                                            right = 255-(240*(wheel-100)/100);

                                        }

                                        robotCommander.cmdMotor(left, right, 300);
                                        break;
                                    case "down":
                                        robotCommander.backward();
                                        break;
                                    case "left":
                                        robotCommander.turnLeft();
                                        break;
                                    case "right":
                                        robotCommander.turnRight();
                                        break;
                                }
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    stopThread = true;
                                }
                            }
                        }
                    });
                    cmdThread.start();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    Log.d("Connection", "before interrupt");
                    stopThread = true;
                    cmdThread.interrupt();
                    Log.d("Connection", "after interrupt");
                    robotCommander.stop();
                    return true;
            }
            return false;
        }

    };
    void printConn(String message) {
        TextView textView = (TextView) findViewById(R.id.connectionStatusMessage);
        textView.setText(message);
        Log.d("Connection", message);
    }

    public void connectBluetooth(View view) {
        printConn("Try to connect...");
        robotCommander = null;
        BluetoothDevice robotBT = null;
        BluetoothSocket robotSocket;

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            printConn("Device doesn't Support Bluetooth");
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

        if (bondedDevices.isEmpty()) {
            printConn("Please Pair the Device HC-06 first. The pairing code is 1234.");
            return;
        } else {
            if (bondedDevices.size() == 1) {
                robotBT = bondedDevices.iterator().next();
            } else {

                if (bondedDevices.size() == 0) {
                    printConn("Can't found any bluetooth connections");
                    return;
                }
                printConn("More bluetooth connections found : "+bondedDevices.size());

                for (BluetoothDevice iterator : bondedDevices)
                {
                    printConn(iterator.getAddress()+": "+iterator.getName());
                    if(iterator.getName().equals("HC-06"))
                    {
                        robotBT=iterator;
                    }

                }

            }
        }
        if (robotBT == null) {
            printConn("Please Pair the Device HC-06 first. The pairing code is 1234.");
            return;
        }
        printConn("Found: " + robotBT.getAddress() + ", " + robotBT.getName());

        try {
            robotSocket = robotBT.createRfcommSocketToServiceRecord(PORT_UUID);
            robotSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            printConn("Error: " + e.getMessage());
            return;
        }
        OutputStream outputStream;
        InputStream inputStream;
        try {
            outputStream = robotSocket.getOutputStream();

            inputStream = robotSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            printConn("Error: " + e.getMessage());
            return;
        }
        robotCommander = new RobotCommander(robotSocket, outputStream, inputStream);

        printConn("Connected: " + robotBT.getAddress() + ", " + robotBT.getName());
    }

}
