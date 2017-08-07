package com.otapigems.robotcar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
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
    RobotCommander robotCommander = null;

    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectBluetooth(null);
    }

    void printConn(String message) {
        TextView textView = (TextView) findViewById(R.id.connectionStatusMessage);
        textView.setText(message);
        Log.d("Connection", message);
    }
    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        // Do something in response to button

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(view.getTag().toString());
        Log.d("?", view.getTag().toString());

        switch (view.getTag().toString()) {
            case "up":
                robotCommander.sendToRobot("U");
                break;
            case "down":
                robotCommander.sendToRobot("D");
                break;
            case "left":
                robotCommander.sendToRobot("L");
                break;
            case "right":
                robotCommander.sendToRobot("R");
                break;
            case "stop":
                robotCommander.sendToRobot("S");
                break;
        }
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
            printConn("Please Pair the Device first");
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
