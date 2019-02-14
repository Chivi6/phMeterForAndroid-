package com.example.administer.phmeter;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.litepal.LitePal;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class ConnectedDeviceList extends AppCompatActivity {

    private RecyclerView recyclerView = null;
    private RecyclerView.LayoutManager manager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected_device_list);

        recyclerView = findViewById(R.id.connected_device_recycle);
        manager = new LinearLayoutManager(this);
        DeviceListAdapter adapter = new DeviceListAdapter();
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    private class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder>{

        private ArrayList<DeviceDataBase> devices = (ArrayList<DeviceDataBase>) LitePal.findAll(DeviceDataBase.class);

        class ViewHolder extends RecyclerView.ViewHolder{
            TextView deviceAddress,deviceParam;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                deviceAddress = itemView.findViewById(R.id.device_addr_name);
                deviceParam = itemView.findViewById(R.id.param);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).
                    inflate(R.layout.connected_device_item,viewGroup,false );
            ViewHolder holder = new ViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            DeviceDataBase dataBase = devices.get(i);
            String addr = dataBase.getDeviceAddress();
            viewHolder.deviceAddress.setText(addr);
            float a = PHMeterParam.getInstance().getaParam(addr,PHMeterParam.aParam );
            float b = PHMeterParam.getInstance().getaParam(addr,PHMeterParam.bParam );
            DecimalFormat decimalFormat=new DecimalFormat(".00");
            viewHolder.deviceParam.setText("A"+decimalFormat.format(a)+"  B"+decimalFormat.format(b));
        }

        @Override
        public int getItemCount() {
            return devices.size();
        }
    }


}
