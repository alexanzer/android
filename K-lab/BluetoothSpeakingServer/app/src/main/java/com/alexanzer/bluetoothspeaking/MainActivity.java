package com.alexanzer.bluetoothspeaking;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private TextToSpeech textToSpeech;
    private Timer timer;
    private Timer timerAccept;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothServerSocket bluetoothServerSocket;
    private BluetoothSocket bluetoothSocket;
    private BufferedWriter  blutoothWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        final EditText speachText = (EditText) findViewById(R.id.speachText);
        final Button speachButton = (Button) findViewById(R.id.speachButton);
        speachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (blutoothWriter != null) {
                    try {
                        blutoothWriter.write(speachText.getText().toString());
                        blutoothWriter.newLine();
                        blutoothWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                textToSpeech.setLanguage(new Locale("ru", "RU"));
            }
        });

        final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        try {
            bluetoothServerSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("K-Lab Server", MY_UUID);
            timerAccept = new Timer();
            timerAccept.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        final BluetoothSocket socket = bluetoothServerSocket.accept(100);
                        if (socket != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    bluetoothSocket = socket;
                                    try {
                                        blutoothWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 100, 100);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            bluetoothAdapter.cancelDiscovery();
            if (bluetoothSocket != null) {
                try {
                    bluetoothSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }



    }

    @Override
    protected void onPause() {
        super.onPause();

        timerAccept.cancel();
        textToSpeech.shutdown();
        try {
            if (blutoothWriter != null)
                blutoothWriter.close();
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
        bluetoothAdapter.cancelDiscovery();
    }

    private BluetoothDevice getBluetoothDevice(Set<BluetoothDevice> bluetoothDevices) {
        final String mac = "80:61:8F:2C:70:72";
        for (BluetoothDevice device : bluetoothDevices) {
            if (mac.equals(device.getAddress())) {
                return device;
            }
        }
        return null;
    }
}
