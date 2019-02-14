package com.example.administer.phmeter;

import android.content.Context;
import android.content.SharedPreferences;

import org.litepal.LitePal;
import org.litepal.exceptions.LitePalSupportException;

public class PHMeterParam {
    public static final String aParam = "a",bParam = "b",isCalibed = "isCalibed";
    private Context mcontext = MyApplication.getContext();
    private static PHMeterParam instance = new PHMeterParam();

    private PHMeterParam(){

    }

    public static PHMeterParam getInstance(){
        return instance;
    }

    //判断设备是否校准
    public boolean isCalib(String address){
        return mcontext.getSharedPreferences(address,Context.MODE_PRIVATE).
        getBoolean(isCalibed,false);
    }

    //设置校准数据
    public void SetParam(String address,float a,float b){
        SharedPreferences.Editor editor = mcontext.getSharedPreferences(address,Context.MODE_PRIVATE).edit();
        editor.putFloat(aParam,a);
        editor.putFloat(bParam,b);
        editor.putBoolean(isCalibed,true);
        editor.apply();

        LitePal.deleteAll(DeviceDataBase.class,"deviceAddress=?",address);
        DeviceDataBase dataBase = new DeviceDataBase(address);
        dataBase.save();
    }

    public float getMeterResult(String address,float v){
        SharedPreferences preferences = mcontext.getSharedPreferences(address,Context.MODE_PRIVATE);
        if (preferences.getBoolean(isCalibed,false)){
            return preferences.getFloat(aParam,0)*v+preferences.getFloat(bParam,0);
        }else {
            return 0;
        }
    }

    public float getaParam(String address,String param){
        SharedPreferences preferences = mcontext.getSharedPreferences(address,Context.MODE_PRIVATE );
        if (param.equals(aParam)){
            return preferences.getFloat(aParam,0 );
        }else if (param.equals(bParam)){
            return preferences.getFloat(bParam,0 );
        }
        return 0;
    }

}
