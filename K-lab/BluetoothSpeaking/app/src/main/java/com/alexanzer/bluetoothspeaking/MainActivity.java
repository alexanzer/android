package com.alexanzer.bluetoothspeaking;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private  int MY_DATA_CHECK_CODE = 1;
    private Timer timer;
    private TTSManager ttsManager = null;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private InputStream blutoothInputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ttsManager = new TTSManager();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.startDiscovery();
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        bluetoothDevice = getBluetoothDevice(devices);
        bluetoothAdapter.cancelDiscovery();
    }

    @Override
    protected void onResume() {
        super.onResume();

        ttsManager.init(this);

        final Button speachButton = (Button) findViewById(R.id.speachButton);
        speachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsManager.addQueue("Ярик делай перчатку!");
            }
        });

        final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        try {
            bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            blutoothInputStream = bluetoothSocket.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(blutoothInputStream));

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (reader != null) {
                            while (reader.ready()) {
                                final String text = reader.readLine();
                                Log.i("Alex>> ", text);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ttsManager.addQueue(text);
                                    }
                                });
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 100, 100);

        } catch (IOException e) {
            e.printStackTrace();
            if (bluetoothSocket != null) {
                try {
                    bluetoothSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }



    }

    @Override
    protected void onPause() {
        super.onPause();

        timer.cancel();
        try {
            if (blutoothInputStream != null)
                blutoothInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bluetoothSocket != null)
                    bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ttsManager.shutDown();
    }

    private BluetoothDevice getBluetoothDevice(Set<BluetoothDevice> bluetoothDevices) {
        final String mac = "98:D3:31:B1:E7:BF";
        for (BluetoothDevice device : bluetoothDevices) {
            Log.i("MAC-Device", device.getAddress());
            if (mac.equals(device.getAddress())) {
                return device;
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ttsManager.shutDown();
    }
}
