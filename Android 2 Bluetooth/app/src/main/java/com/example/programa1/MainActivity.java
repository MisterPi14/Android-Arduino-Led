package com.example.programa1;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSION_CODE = 2;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton btnEncender = findViewById(R.id.btnEncender);
        ImageButton btnApagar = findViewById(R.id.btnApagar);
        Button btnConnect = findViewById(R.id.btnConnect);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth no soportado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();
            }
        });

        btnEncender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected) {
                    btnEncender.setVisibility(View.GONE);
                    btnApagar.setVisibility(View.VISIBLE);
                    sendData("1"); // Encender LED
                } else {
                    Toast.makeText(MainActivity.this, "No conectado al dispositivo Bluetooth", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnApagar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected) {
                    btnApagar.setVisibility(View.GONE);
                    btnEncender.setVisibility(View.VISIBLE);
                    sendData("0"); // Apagar LED
                } else {
                    Toast.makeText(MainActivity.this, "No conectado al dispositivo Bluetooth", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Encienda Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, REQUEST_PERMISSION_CODE);
            } else {
                connectToDevice();
            }
        } else {
            connectToDevice();
        }
    }

    private void connectToDevice() {
        Intent intent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            String address = data.getStringExtra("deviceAddress");
            device = bluetoothAdapter.getRemoteDevice(address);
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // UUID para el módulo HC-05

            try {
                socket = device.createRfcommSocketToServiceRecord(uuid);
                socket.connect();
                outputStream = socket.getOutputStream();
                connected = true;
                Toast.makeText(this, "Conexión establecida", Toast.LENGTH_SHORT).show();
                sendData("A"); // Señal de conexión al Arduino
            } catch (IOException e) {
                Log.e(TAG, "Error al conectar", e);
                Toast.makeText(this, "Error al conectar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendData(String message) {
        try {
            outputStream.write(message.getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Error al enviar datos", e);
            Toast.makeText(this, "Error al enviar datos", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error al cerrar conexión", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                connectToDevice();
            } else {
                Toast.makeText(this, "Permisos necesarios no otorgados", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
