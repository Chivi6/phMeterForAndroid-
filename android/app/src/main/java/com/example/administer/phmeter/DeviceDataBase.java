package com.example.administer.phmeter;

import org.litepal.crud.LitePalSupport;

public class DeviceDataBase extends LitePalSupport {
    private String deviceAddress;

    public DeviceDataBase(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
        this.save();
    }


}
