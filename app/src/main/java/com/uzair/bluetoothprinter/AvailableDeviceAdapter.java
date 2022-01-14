package com.uzair.bluetoothprinter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class AvailableDeviceAdapter extends BaseAdapter {
    private List<BluetoothDevice> mData;
    private Context context;
    private OnPairItemClickListener mListener;

    public AvailableDeviceAdapter(List<BluetoothDevice> mData, Context context) {
        this.mData = mData;
        this.context = context;
    }

    public void setListener(OnPairItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(R.layout.list_item, parent, false);
        TextView txtName = (TextView) convertView.findViewById(R.id.itemTitle);

        txtName.setText(mData.get(position).getName());
        txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPairButtonClick(position);
            }
        });

        return convertView;
    }

    public interface OnPairItemClickListener {
        public abstract void onPairButtonClick(int position);
    }
}
