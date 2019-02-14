package com.example.administer.phmeter;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;

public class BTSend extends Thread {
    public BTSend(String str,BluetoothSocket btSocket){
        sendText = str;
        this.btSocket = btSocket;
    }

    private String sendText;
    private BluetoothSocket btSocket;

    @Override
    public void run() {
        try{
            if (btSocket != null){
                OutputStream os = btSocket.getOutputStream();
                byte[] sendData = sendText.getBytes();
                os.write(sendData);
            }
        }catch (IOException i){
            i.printStackTrace();
        }
    }
}
