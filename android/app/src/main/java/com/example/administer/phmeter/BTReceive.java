package com.example.administer.phmeter;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;

public class BTReceive extends Thread {

    private BluetoothSocket btSocket;
    private Handler BTReceiver;

    public BTReceive(BluetoothSocket btSocket, Handler BTReceiver) {
        this.btSocket = btSocket;
        this.BTReceiver = BTReceiver;
    }

    @Override
        public void run() {
            try{
                InputStream is = btSocket.getInputStream();
                byte b[] = new byte[1];
                while (is != null){
                    is.read(b);
                    int i = b[0] & 0xff;
                    Message msg = new Message();
                    msg.what = i;
                    BTReceiver.sendMessage(msg);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

}
