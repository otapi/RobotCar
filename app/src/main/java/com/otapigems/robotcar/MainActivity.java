package com.otapigems.robotcar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    BluetoothDevice robotBT;
    BluetoothSocket robotSocket;
    private final String DEVICE_ADDRESS="20:13:10:15:33:66";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private OutputStream outputStream;
    private InputStream inputStream;
    Thread thread;
    byte buffer[];
    int bufferPosition;
    boolean stopThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    void printConn(String message) {
        TextView textView = (TextView) findViewById(R.id.connectionStatusMessage);
        textView.setText(message);
        Log.d("Connection", message);
    }
    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
    // Do something in response to button
        String IdAsString = view.getResources().getResourceName(view.getId());

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(view.getTag().toString());
        Log.d("?", view.getTag().toString());

        switch (view.getTag().toString()) {
            case "up":
                sendToRobot("U");
                break;
            case "down":
                sendToRobot("D");
                break;
            case "left":
                sendToRobot("l");
                break;
            case "right":
                sendToRobot("R");
                break;
            case "stop":
                sendToRobot("S");
                break;
        }
    }

    public void connectBluetooth(View view) {
        printConn("Try to connect...");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            printConn("Device doesnt Support Bluetooth");
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

        if (bondedDevices.isEmpty()) {
            printConn("Please Pair the Device first");
            return;
        } else {
            if (bondedDevices.size() == 1) {
                robotBT = bondedDevices.iterator().next();
            } else {
                /*
                for (BluetoothDevice iterator : bondedDevices)
                {
                    if(iterator.getAddress().equals(DEVICE_ADDRESS))
                    {
                        device=iterator;
                        found=true;
                        break;
                    }
                }
                */
                for (BluetoothDevice iterator : bondedDevices)
                {
                    printConn(iterator.getAddress()+": "+iterator.getName());
                }
                printConn("Too much or zero bluetooth connections: "+bondedDevices.size());
                return;
            }
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
        try {
            outputStream = robotSocket.getOutputStream();

            inputStream = robotSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            printConn("Error: " + e.getMessage());
            return;
        }


        printConn("Connected: " + robotBT.getAddress() + ", " + robotBT.getName());
        //beginListenForData();
    }

    void sendToRobot(String mess) {
        mess.concat("\n");
        try {
            outputStream.write(mess.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];

        Thread thread  = new Thread(new Runnable()
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

        thread.start();
    }

    public void onClickStop(View view) throws IOException {
        stopThread = true;
        outputStream.close();
        inputStream.close();
        robotSocket.close();


    }
}
