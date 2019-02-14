package com.example.administer.phmeter;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.DecimalFormat;

public class CalibDeviceActivity extends AppCompatActivity {

    static public boolean isCDataReceive = false;
    static public float voltge;

    private boolean isShouDong = false;
    private int calibTimes = 0;
    private Button shoudong,calib,next;
    private EditText standard,measVoltge;
    private float standardPH[] = new float[2];
    private float standardVol[] = new float[2];

    private Handler enableButton = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1){
                calibTimes = 1;
                standardVol[0] = voltge;
                DecimalFormat decimalFormat=new DecimalFormat(".00");
                measVoltge.setText(decimalFormat.format(standardVol[0]));
                ShowToast("请点击下一步进行第二次校准");
            }
            if (msg.what == 2){
                calibTimes = 0;
                standardVol[1] = voltge;
                DecimalFormat decimalFormat=new DecimalFormat(".00");
                measVoltge.setText(decimalFormat.format(standardVol[1]));
                Message message = new Message();
                message.what = 3;
                enableButton.sendMessage(message);
            }
            if (msg.what == 3){
                float a = (standardPH[0]-standardPH[1])/(standardVol[0]-standardVol[1]);
                float b = standardPH[0]-a*standardVol[0];
                PHMeterParam.getInstance().SetParam(MainActivity.connectingDevice,a ,b );
                ShowToast("校准完成");
                next.setText("完成");
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calib_device);
        Init();
    }

    private void Init(){
        isCDataReceive = false;
        voltge = 0;
        shoudong = findViewById(R.id.shoudong);
        calib = findViewById(R.id.calib);
        next = findViewById(R.id.next);
        standard = findViewById(R.id.standard);
        measVoltge = findViewById(R.id.measVoltge);
        MainActivity.isMeasuring = false;
        new BTSend("013", MainActivity.btSocket).start();

        shoudong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isShouDong){
                    calib.setEnabled(false);
                    measVoltge.setFocusableInTouchMode(true);
                    isShouDong = true;
                }else {
                    calibTimes = 0;
                    calib.setEnabled(true);
                    measVoltge.clearFocus();
                    measVoltge.setFocusableInTouchMode(false);
                    isShouDong = false;
                }

            }
        });

        calib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shoudong.setEnabled(false);
                if (!isShouDong){
                String ph = standard.getText().toString();
                if (!ph.equals("")){
                    calib.setEnabled(false);
                    new BTSend("113", MainActivity.btSocket).start();
                    if (calibTimes == 0){
                        standardPH[0] = Float.parseFloat(ph);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (!isCDataReceive){ }
                                isCDataReceive = false;
                                Message msg = new Message();
                                msg.what = 1;
                                enableButton.sendMessage(msg);
                            }
                        }).start();
                    }else if (calibTimes == 1){
                        standardPH[1] = Float.parseFloat(ph);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (!isCDataReceive){ }
                                isCDataReceive = false;
                                Message msg = new Message();
                                msg.what = 2;
                                enableButton.sendMessage(msg);
                            }
                        }).start();
                    }
                }else {
                    ShowToast("请输入PH值");
                }


            }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isShouDong){
                calib.setEnabled(true);
                standard.setText("");
                measVoltge.setText("");
                }else {
                    String ph = standard.getText().toString(),vol = measVoltge.getText().toString();
                    if (!ph.equals("") && !vol.equals("")){
                        if (calibTimes<2){
                            standardPH[calibTimes] = Float.parseFloat(ph);
                            standardVol[calibTimes] = Float.parseFloat(vol);
                            standard.setText("");
                            measVoltge.setText("");
                            calibTimes++;
                        }
                        if (calibTimes == 2){
                            Message message = new Message();
                            message.what = 3;
                            enableButton.sendMessage(message);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivity.isMeasuring = true;
    }

    private void ShowToast(String str){
        Toast.makeText(CalibDeviceActivity.this,str,Toast.LENGTH_SHORT).show();
    }
}
