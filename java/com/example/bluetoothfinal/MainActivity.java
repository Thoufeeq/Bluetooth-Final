package com.example.bluetoothfinal;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button button;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice device;
    BluetoothSocket socket;
    InputStream inputstream;
    String address = "98:D3:31:40:9A:F7";
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int DATA_RECEIVED = 1;
    TextView incoming;
    byte[] buffer = new byte[1024];
    int bytes;
    String data;
    String cmd1 = "Music Player turned ON";
    String cmd2 = "Music Player turned OFF";
    String cmd3 = "Command not recognized";
    MediaPlayer mediaPlayer;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    //update UI (TextView)
                    data = Character.toString((char) bytes);
                    if (data.equals("A")) {
                        incoming.setText(cmd1);
                        music_on();
                    }
                    else if (data.equals("B")) {
                        incoming.setText(cmd2);
                        music_off();
                    }
                    else {incoming.setText(cmd3);}
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.b1);
        incoming = (TextView) findViewById(R.id.incoming);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                device_connect();
            }
        });
    }

    public void device_connect() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        device = mBluetoothAdapter.getRemoteDevice(address);

        ConnectThread mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    public void music_on() {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.song1);
        mediaPlayer.start();
    }

    public void music_off() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        else {
            Toast.makeText(getApplicationContext(),"No music is being played!.",Toast.LENGTH_SHORT).show();
        }
    }

    private class ConnectThread extends Thread {
        public ConnectThread(BluetoothDevice device) {
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                Toast.makeText(getApplicationContext(),"Connection Successful!",Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(),"Listening for incoming data...",Toast.LENGTH_SHORT).show();
            } catch (IOException e) { }
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            try {
                socket.connect();
                inputstream = socket.getInputStream();
                while (true) {
                    bytes = inputstream.read();
                    mHandler.obtainMessage(DATA_RECEIVED, bytes, -1, buffer)
                            .sendToTarget();
                }
            } catch (IOException connectException) {
                try {
                    socket.close();
                } catch (IOException closeException) { }
            }

        }

    }

}
