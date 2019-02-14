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
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final int updataDevices = 3;
    private final int modSingle = 34;
    private final int modAuto = 54;
    static public boolean isMeasuring = false;//true:测量  false:校准
    static protected boolean isBTConnect = false;
    static public String connectingDevice = null;
    static public MeasuringData currentData = null;
    static public BluetoothSocket     btSocket         = null;

    private BluetoothDevice            device           = null;
    private BluetoothAdapter           bluetoothAdapter = null;
    private BlueToothBroadcastReceiver receiver         = new BlueToothBroadcastReceiver();
    private ArrayList<BluetoothDevice> devices          = new ArrayList<>();
    private LinearLayout               homePage         = null;
    private DrawerLayout               mainAct          = null;
    private TextView                   preNum           = null;
    private TextView                   maxNum           = null;
    private TextView                   minNum           = null;
    private TextView currentTestName = null;
    private ViewPager homeVP = null;
    private ArrayList<View> views = new ArrayList<>();
    private TabLayout homeTbaL = null;
    private Switch switchMod = null;
    private NavigationView homeNavig = null;
    private View deviceRecycler = null;
    private RecyclerView searchedDevice = null;
    private RecyclerView HistoryData = null;
    private LinearLayoutManager manager = null;
    private Button measEnable = null;
    private AlertDialog searchDevice = null;//设备搜索结果dialog
    private FrameLayout rl = null;
    private boolean isConnecting = false;
    private String[] tabTitle = {"当前测量","历史记录"};
    private int currentMod = modSingle;
    private boolean isAutoEnable = false;
    private ArrayList<MeasuringData> measDatas = null;
    private HistoryDataListAdapter hdAdapter = null;

    //用于更新‘搜索设备结果’的dialog
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == updataDevices){
                SearchDeviceListAdapter ryAdapter = new SearchDeviceListAdapter(devices);
                searchedDevice.setAdapter(ryAdapter);
            }
        }
    };

    public Handler BTReceiver = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            float v = (float) msg.what/255*5;
            float previousNum = PHMeterParam.getInstance().getMeterResult(connectingDevice, v);

            if (isMeasuring){
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss");// HH:mm:ss
                //获取当前时间
                Date date = new Date(System.currentTimeMillis());
                currentData.addMData(previousNum,simpleDateFormat.format(date));
                DecimalFormat decimalFormat=new DecimalFormat(".00");
                preNum.setText(decimalFormat.format(previousNum));
                if (previousNum > currentData.getMax()){
                    maxNum.setText(decimalFormat.format(previousNum));
                    currentData.setMax(previousNum);
                }else if (previousNum < currentData.getMin()){
                    minNum.setText(decimalFormat.format(previousNum));
                    currentData.setMin(previousNum);
                }
            }else {
                CalibDeviceActivity.voltge = v;
                CalibDeviceActivity.isCDataReceive = true;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Init();

        //未连接蓝牙设备时用的布局
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
                if (Build.VERSION.SDK_INT >=6.0 &&
                        ContextCompat.checkSelfPermission(MainActivity.this,Manifest.
                                permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.
                                    permission.ACCESS_FINE_LOCATION}, 1);
                }else {
                    if (!BTisEnabled()){
                        bluetoothAdapter.enable();
                    }else if (BTisEnabled()) {
                        BluetoothRegister();
                        searchDevice.show();
                        searchedDevice = searchDevice.findViewById(R.id.search_device);
                        searchedDevice.setLayoutManager(manager);
                        bluetoothAdapter.startDiscovery();
                    }
                }
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
    protected void onResume() {
        super.onResume();
        isMeasuring = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case 1: {//定位权限
                if(!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    ShowToast("没有定位权限，无法搜索附近蓝牙设备");
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
        View view = View.inflate(this,R.layout.new_test_dialog ,null );
        final EditText name = view.findViewById(R.id.new_test_name)
                ,content = view.findViewById(R.id.new_test_content);
        if (item.getItemId() == R.id.new_test){
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("新建测试")
                    .setView(view)
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");// HH:mm:ss
                            //获取当前时间
                            Date date = new Date(System.currentTimeMillis());
                            String cTime = simpleDateFormat.format(date);

                            MeasuringData data = new MeasuringData(
                                    MeasuringData.findAllData().size(),
                                    cTime, name.getText().toString(),
                                    content.getText().toString() ,true );
                            data.save();
                            measDatas.add(data);
                            hdAdapter.notifyItemInserted(measDatas.size());
                            StartTest(data);
                        }
                    }).create();

            dialog.show();

        }
        return true;
    }

    //搜索蓝牙设备时需要用到的广播
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

        homeVP = findViewById(R.id.home_vp);
        views.add(View.inflate(MainActivity.this,R.layout.current_data ,null ));
        views.add(View.inflate(MainActivity.this,R.layout.meas_data_list ,null ));
        homeVP.setAdapter(new ViewPageAdapter(views));
        homeTbaL = findViewById(R.id.home_tablayout);
        homeTbaL.setupWithViewPager(homeVP);
        homeNavig = findViewById(R.id.home_navig);

        preNum = views.get(0).findViewById(R.id.previous_num);
        minNum = views.get(0).findViewById(R.id.min_num);
        maxNum = views.get(0).findViewById(R.id.max_num);
        currentTestName = views.get(0).findViewById(R.id.current_test_name);
        switchMod = views.get(0).findViewById(R.id.switch_mod);
        measEnable = views.get(0).findViewById(R.id.meas_enable);

        currentData = MeasuringData.getRecentMeas();
        measDatas = MeasuringData.findAllData();

        hdAdapter = new HistoryDataListAdapter(measDatas);
        HistoryData = views.get(1).findViewById(R.id.history_data);
        HistoryData.setLayoutManager(new LinearLayoutManager(this));
        HistoryData.setAdapter(hdAdapter);

        homeNavig.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.device_manager:
                        Intent intent = new Intent(MainActivity.this,ConnectedDeviceList.class);
                        startActivity(intent);
                        break;
                    case R.id.device_calib:
                        Intent intent1 = new Intent(MainActivity.this,CalibDeviceActivity.class );
                        startActivity(intent1);
                        break;
                     default:
                }
                mainAct.closeDrawers();
                return true;
            }
        });

        switchMod.setChecked(false);
        //切换工作模式
        switchMod.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()){
                    currentMod = modAuto;
                    isAutoEnable = true;
                    if (btSocket != null){
                        new BTSend("012",btSocket);
                    }
                }else {
                    currentMod = modSingle;
                    if (btSocket != null){
                        new BTSend("013",btSocket);
                    }
                }
            }
        });

        //暂停/开始工作
        measEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentMod == modAuto){
                    if (isAutoEnable){
                        if (btSocket != null){
                            new BTSend("002",btSocket);
                        }
                    }else if (!isAutoEnable){
                        if (btSocket != null){
                            new BTSend("012",btSocket);
                        }
                    }
                }else if (currentMod == modSingle){
                    if (btSocket != null){
                        new BTSend("112",btSocket);
                    }
                }
            }
        });

        //设备搜索结果dialog
        searchDevice = new AlertDialog.Builder(MainActivity.this)
                .setTitle("搜索到的设备")
                .setView(deviceRecycler)
                .setCancelable(false)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isBTConnect = false;
                        isConnecting = false;
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
            connectingDevice = address;
            searchDevice.cancel();
            unregisterReceiver(receiver);
            homePage.setVisibility(View.VISIBLE);
            mainAct.removeView(rl);
            new BTReceive(btSocket,BTReceiver).start();
            if (!PHMeterParam.getInstance().isCalib(address)){
                ShowToast("设备未校准，请进行校准");
                Intent intent = new Intent(MainActivity.this,CalibDeviceActivity.class);
                startActivity(intent);
            }else {
                StartTest(null);
            }
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
        }finally {
            isConnecting = false;
        }
    }

    private void StartTest(MeasuringData data){
        if (currentData == null && data == null){
            ShowToast("请新建测试");
            views.get(0).setVisibility(View.GONE);
        }else {
            preNum.setText("");
            minNum.setText("");
            maxNum.setText("");
            if (currentData != null){
                if (data != null){
                    currentData.setRecent(false);
                    currentData = data;
                }
            }else if (data != null){
                currentData = data;
            }
            currentData.setRecent(true);
            isMeasuring = true;
            switchMod.setChecked(false);
            new BTSend("013", btSocket);
            currentTestName.setText(currentData.getTestName());
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
            android.view.View view= LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.device_item,viewGroup,false);
            final ViewHolder holder = new ViewHolder(view);
            holder.device.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isConnecting){
                        isConnecting = true;
                        bluetoothAdapter.cancelDiscovery();
                        BluetoothDevice btd = devices.get(holder.getAdapterPosition());
                        if (btd.getBondState() == BluetoothDevice.BOND_NONE){
                            btd.createBond();
                        }else if (btd.getBondState() == BluetoothDevice.BOND_BONDED){
                            BTDConnect(btd.getAddress());
                        }
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

    private class ViewPageAdapter extends PagerAdapter {
        ArrayList<View> views;

        public ViewPageAdapter(ArrayList<View> views) {
            this.views = views;
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            container.addView(views.get(position));
            return views.get(position);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(views.get(position));
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitle[position];
        }
    }

    //历史记录recyclerview 的 adapter
    private class HistoryDataListAdapter extends RecyclerView.Adapter<HistoryDataListAdapter.ViewHolder>{

        private ArrayList<MeasuringData> datas;
        private int openedPosition = -1;

        public HistoryDataListAdapter(ArrayList<MeasuringData> datas) {
            this.datas = datas;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private CardView historyCV;
            private TextView testName,testContent,createTime;
            private Button testContinue;
            private RecyclerView HistDetData;
            private LinearLayout detailLl;

            public ViewHolder(View view){
                super(view);
                historyCV = view.findViewById(R.id.history_CV);
                testName = view.findViewById(R.id.test_name);
                testContent = view.findViewById(R.id.test_content);
                createTime = view.findViewById(R.id.create_time);
                testContinue = view.findViewById(R.id.test_continue);
                HistDetData = view.findViewById(R.id.history_detail_data);
                detailLl = view.findViewById(R.id.detail_ll);

                historyCV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (openedPosition == getAdapterPosition()){
                            openedPosition = -1;
                            notifyItemChanged(getAdapterPosition());
                        }else {
                            int temp = openedPosition;
                            openedPosition = getAdapterPosition();
                            notifyItemChanged(temp);
                            notifyItemChanged(openedPosition);
                        }
                    }
                });

                testContinue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StartTest(datas.get(getAdapterPosition()));
                    }
                });
            }

            public void onBindView(int p, MeasuringData data){
                testName.setText(data.getTestName());
                testContent.setText(data.getTestContent());
                createTime.setText(data.getCreatTime());
                LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this);
                HistoryDetailAdapter adapter = new HistoryDetailAdapter(datas.get(p).getMdatas());
                HistDetData.setLayoutManager(manager);
                HistDetData.setAdapter(adapter);

                if (p == openedPosition){
                    detailLl.setVisibility(View.VISIBLE);
                }else {
                    detailLl.setVisibility(View.GONE);
                }
            }


        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                    R.layout.data_item, viewGroup,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            viewHolder.onBindView(i,datas.get(i) );
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        private class HistoryDetailAdapter extends RecyclerView.Adapter<HistoryDetailAdapter.HDAVHolder>{

            private ArrayList<MeasuringData.Mdata> list;

            public HistoryDetailAdapter(ArrayList<MeasuringData.Mdata> list) {
                this.list = list;
            }

            class HDAVHolder extends RecyclerView.ViewHolder{
                TextView phData,measTime;

                public HDAVHolder(View view) {
                    super(view);
                    phData = view.findViewById(R.id.ph_data);
                    measTime = view.findViewById(R.id.meas_time);
                }
            }

            @NonNull
            @Override
            public HDAVHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                        R.layout.history_data, viewGroup,false );
                return new HDAVHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull HDAVHolder hdavHolder, int i) {
                DecimalFormat decimalFormat=new DecimalFormat(".00");
                hdavHolder.phData.setText(decimalFormat.format(list.get(i).mdata));
                hdavHolder.measTime.setText(list.get(i).mtime);
            }

            @Override
            public int getItemCount() {
                return list.size();
            }
        }
    }

    private void ShowToast(String str){
        Toast.makeText(MainActivity.this,str,Toast.LENGTH_SHORT).show();
    }
}