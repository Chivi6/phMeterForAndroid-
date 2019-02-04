package com.example.administer.phmeter;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final int updataDevices = 3;

    static protected boolean isBTConnect = false;

    private BluetoothDevice            device = null;
    private BluetoothSocket            btSocket         = null;
    private BluetoothAdapter           bluetoothAdapter = null;
    private BlueToothBroadcastReceiver receiver = new BlueToothBroadcastReceiver();
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private LinearLayout homePage = null;
    private LinearLayout mainAct = null;

    private View deviceRecycler = null;
    private RecyclerView searchedDevice = null;
    private LinearLayoutManager manager = null;
    private AlertDialog searchDevice = null;//设备搜索结果dialog
    private FrameLayout rl = null;


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == updataDevices){
                SearchDeviceListAdapter ryAdapter = new SearchDeviceListAdapter(devices);
                searchedDevice.setAdapter(ryAdapter);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Init();
        Button search = new Button(this);
        rl = new FrameLayout(this);
        FrameLayout.LayoutParams rlParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout.LayoutParams bottonParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        bottonParams.gravity = Gravity.CENTER;
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothRegister();
                searchDevice.show();
                searchedDevice =  searchDevice.findViewById(R.id.search_device);
                searchedDevice.setLayoutManager(manager);
                if (!BTisEnabled()){
                    bluetoothAdapter.enable();
                }
                bluetoothAdapter.startDiscovery();
            }
        });
        search.setText("搜索设备");
        mainAct.addView(rl,rlParams);
        rl.addView(search,bottonParams);



        if (Build.VERSION.SDK_INT >= 6.0) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                }else {
                    ShowToast("failed");
                }

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.plus,menu);
        return true;
    }

    //新建一个ph测试记录
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.new_test){

        }
        return true;
    }

    private void BluetoothRegister(){
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(receiver,intentFilter);
    }

    private void Init(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceRecycler = View.inflate(MainActivity.this,R.layout.serch_device_list,null);

        manager = new LinearLayoutManager(this);

        homePage = findViewById(R.id.home_page);
        mainAct = findViewById(R.id.main_activity);
        Toolbar toolbar = findViewById(R.id.mainActivityBar);
        setSupportActionBar(toolbar);

        //设备搜索结果dialog
        searchDevice = new AlertDialog.Builder(MainActivity.this)
                .setTitle("搜索到的设备")
                .setView(deviceRecycler)
                .setCancelable(false)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isBTConnect = false;
                        try{
                            if (btSocket != null){
                                btSocket.close();
                            }
                        }catch (IOException i){
                            i.printStackTrace();
                        }
                        unregisterReceiver(receiver);
                        devices.removeAll(devices);
                        searchDevice.cancel();
                        bluetoothAdapter.cancelDiscovery();
                    }
                }).create();

    }

    boolean BTisEnabled(){
        if (bluetoothAdapter != null){
            return bluetoothAdapter.isEnabled();
        }else {
            return false;
        }
    }

    private void BTDConnect(String address){

        ShowToast("开始连接，请稍候...");
        device = bluetoothAdapter.getRemoteDevice(address);
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothAdapter.cancelDiscovery();
            btSocket.connect();
            isBTConnect = true;
            ShowToast("连接成功");
            searchDevice.cancel();
            homePage.setVisibility(View.VISIBLE);
            mainAct.removeView(rl);
        }catch (IOException e){
            isBTConnect = false;
            ShowToast("连接失败");
            try {
                if (btSocket != null){
                    btSocket.close();
                }
            }catch (IOException i){
                i.printStackTrace();
            }
        }
    }

    private class BlueToothBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                devices.add((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                Message message = new Message();
                message.what = updataDevices;
                handler.sendMessage(message);
            }else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice btd = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (btd.getBondState() == BluetoothDevice.BOND_BONDED){
                    BTDConnect(btd.getAddress());
                }
            }else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                ShowToast("搜索完成");
            }


        }
    }



    //设备搜索结果recyclerview的adapter
    private class SearchDeviceListAdapter extends RecyclerView.Adapter<SearchDeviceListAdapter.ViewHolder>{
        private List<BluetoothDevice> devices;

        class ViewHolder extends RecyclerView.ViewHolder{
            TextView deviceName;
            TextView deviceContent;
            TextView deviceTime;
            CardView device;

            public ViewHolder(View view){
                super(view);
                deviceName = view.findViewById(R.id.device_name);
                deviceContent = view.findViewById(R.id.device_content);
                deviceTime = view.findViewById(R.id.device_time);
                device = view.findViewById(R.id.device);
            }
        }

        public SearchDeviceListAdapter(List<BluetoothDevice> devices){
            this.devices = devices;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view= LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.device_item,viewGroup,false);
            final ViewHolder holder = new ViewHolder(view);
            holder.device.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothAdapter.cancelDiscovery();
                    BluetoothDevice btd = devices.get(holder.getAdapterPosition());
                    if (btd.getBondState() == BluetoothDevice.BOND_NONE){
                        btd.createBond();
                    }else if (btd.getBondState() == BluetoothDevice.BOND_BONDED){
                        BTDConnect(btd.getAddress());
                    }
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            BluetoothDevice device = devices.get(i);
            viewHolder.deviceName.setText(device.getName());

        }

        @Override
        public int getItemCount() {
            return devices.size();
        }
    }

    private void ShowToast(String str){
        Toast.makeText(MainActivity.this,str,Toast.LENGTH_SHORT).show();
    }
}

