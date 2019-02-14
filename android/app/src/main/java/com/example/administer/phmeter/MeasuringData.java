package com.example.administer.phmeter;

import org.litepal.crud.LitePalSupport;
import org.litepal.LitePal;
import java.util.ArrayList;
import java.util.List;

public class MeasuringData extends LitePalSupport {

    public class Mdata{
        float mdata;
        String mtime;

        public Mdata(float mdata, String mtime) {
            this.mdata = mdata;
            this.mtime = mtime;
        }
    }
    private int id;
    private String creatTime;
    private ArrayList<Mdata> mdatas;
    private String testName;
    private String testContent;
    private float max,min;
    private boolean isRecent;

    public MeasuringData(int id,String creatTime, String testName,String testContent,boolean isRecent) {
        this.id = id;
        this.creatTime = creatTime;
        this.mdatas = new ArrayList<Mdata>();
        this.testName = testName;
        this.testContent = testContent;
        this.max = 0;
        this.min = 0;
        this.isRecent = isRecent;
    }

    public static ArrayList<MeasuringData> findAllData(){
        return (ArrayList<MeasuringData>) LitePal.findAll(MeasuringData.class);
    }

    public static MeasuringData getRecentMeas(){
        List<MeasuringData> list = LitePal.where("isRecent=?","1").find(MeasuringData.class);
        if (list.size() == 0){
            return null;
        }else {
            return list.get(0);
        }
    }

    public void addMData(float mdata,String mtime){
        this.mdatas.add(new Mdata(mdata,mtime ));
        this.save();
    }

    public int getId() {
        return id;
    }

    public String getCreatTime() {
        return creatTime;
    }

    public ArrayList<Mdata> getMdatas() {
        return mdatas;
    }

    public String getTestName() {
        return testName;
    }

    public float getMax() {
        return max;
    }

    public float getMin() {
        return min;
    }

    public String getTestContent() {
        return testContent;
    }

    public boolean isRecent() {
        return isRecent;
    }

    public void setCreatTime(String creatTime) {
        this.creatTime = creatTime;
    }

    public void setMdatas(ArrayList<Mdata> mdatas) {
        this.mdatas = mdatas;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setMax(float max) {
        this.max = max;
        this.save();
    }

    public void setMin(float min) {
        this.min = min;
        this.save();
    }

    public void setTestContent(String testContent) {
        this.testContent = testContent;
    }

    public void setRecent(boolean recent) {
        isRecent = recent;
        this.save();
    }
}
