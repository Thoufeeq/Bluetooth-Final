package com.example.bluetoothfinal;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice device;
    BluetoothSocket socket;
    InputStream inputstream;
    String address = "20:13:10:08:09:19";
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    static final int MESSAGE_READ = 1;
    Button button;
    TextView incoming;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // get something from the thread (read incoming data)

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.b1);
        incoming = (TextView) findViewById(R.id.incoming);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        device = mBluetoothAdapter.getRemoteDevice(address);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                connect_device();
            }
        });

        ConnectThread mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    public void connect_device() {
        // perform some action
        Toast.makeText(getApplicationContext(), "Button Clicked!", Toast.LENGTH_SHORT).show();

    }

    private class ConnectThread extends Thread {

        public ConnectThread(BluetoothDevice device) {
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                socket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    socket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            ConnectedThread mConnectedThread = new ConnectedThread(socket);
            mConnectedThread.start();
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectedThread extends Thread {
        public ConnectedThread(BluetoothSocket socket) {
            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                inputstream = socket.getInputStream();
            } catch (IOException e) { }

        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = inputstream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) { }
        }
    }


}


