package com.uzair.bluetoothprinter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity {

    public static final int PERMISSION_BLUETOOTH = 1;
    private BluetoothConnection selectedDevice;
    List<PrintDataModel> list;
    private ArrayList<BluetoothDevice> mDeviceList;
    Button btn;
    // bluetooth adapter
    BluetoothAdapter bluetoothAdapter;
    // bottom sheet views
    BottomSheetDialog bottomSheetDialog;
    ProgressBar progressBar;
    AvailableDeviceAdapter adapter;
    ListView listView;
    AppCompatButton closeBtn, scanBtn;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpArrayList();
        init();
        setBottomSheetDialog();

    }

    private void setBottomSheetDialog() {
        //bottom sheet dialog
        bottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialog);
        bottomSheetDialog.setContentView(R.layout.available_device_bottom_sheet);
        bottomSheetDialog.getBehavior().setDraggable(false);
        // views of bottom sheet
        closeBtn = bottomSheetDialog.findViewById(R.id.closeBtn);
        scanBtn = bottomSheetDialog.findViewById(R.id.scanBtn);
        progressBar = bottomSheetDialog.findViewById(R.id.progressBarBT);
        mDeviceList = new ArrayList<BluetoothDevice>();
        listView = bottomSheetDialog.findViewById(R.id.deviceList);
        adapter = new AvailableDeviceAdapter(mDeviceList, MainActivity.this);
        listView.setAdapter(adapter);


        // scan button click
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDeviceList.clear();
                adapter.notifyDataSetChanged();
                bluetoothAdapter.startDiscovery();
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        // click on bottom sheet close button
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                mDeviceList.clear();
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
            }
        });
    }

    private void init() {
        // init bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btn = findViewById(R.id.connect);

        /// click to connect with printer
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn.getText().toString().equals("Print")) {
                    printDataIfPermissionIsEnable();
                } else if (btn.getText().toString().equals("Connect")) {
                    // Getting the Bluetooth adapter
                    if (checkLocationPermission()) {
                        showPairedDevice();
                    } else {
                        requestLocationPermission();
                    }
                }

            }
        });

        // click on scan
        findViewById(R.id.scan)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (checkLocationPermission()) {
                            mDeviceList.clear();
                            bluetoothAdapter.startDiscovery();
                        } else {
                            requestPermissions(new String[]{
                                            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                    101);
                        }
                    }
                });


    }

    // request location permission
    private void requestLocationPermission() {
        requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                101);
    }

    // check location permission
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            return false;
        }
        return true;
    }

    // bluetooth enable activity result
    ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // showPairedDevice();
                } else {
                    Toast.makeText(MainActivity.this, "Unable to use bluetooth printer . please enable bluetooth", Toast.LENGTH_SHORT).show();
                }
            });


    // if bluetooth is enable then show paired device list
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showPairedDevice() {
        // Getting the Bluetooth adapter
        if (bluetoothAdapter.isEnabled()) {
            getBluetoothPairedDeviceList();
        } else {
            //Prompt user to turn on Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            launchSomeActivity.launch(enableBtIntent);
        }
    }

    // dummy array list
    private void setUpArrayList() {

        list = new ArrayList<>();
        list.add(new PrintDataModel("First", "2", 120));
        list.add(new PrintDataModel("Second", "4", 300));
        list.add(new PrintDataModel("Third", "6", 550));
        list.add(new PrintDataModel("Fourth", "1", 40));
        list.add(new PrintDataModel("Fifth", "2", 40));
        list.add(new PrintDataModel("Sixth", "4", 50));
        list.add(new PrintDataModel("Seven", "6", 40));
        list.add(new PrintDataModel("Eight", "3", 60));
        list.add(new PrintDataModel("First", "2", 120));
        list.add(new PrintDataModel("Second", "4", 300));
        list.add(new PrintDataModel("Third", "6", 550));
        list.add(new PrintDataModel("Fourth", "1", 40));
        list.add(new PrintDataModel("Fifth", "2", 40));
        list.add(new PrintDataModel("Sixth", "4", 50));
        list.add(new PrintDataModel("Seven", "6", 40));
        list.add(new PrintDataModel("Eight", "3", 60));
        list.add(new PrintDataModel("First", "2", 120));
        list.add(new PrintDataModel("Second", "4", 300));
        list.add(new PrintDataModel("Third", "6", 550));
        list.add(new PrintDataModel("Fourth", "1", 40));
        list.add(new PrintDataModel("Fifth", "2", 40));
        list.add(new PrintDataModel("Sixth", "4", 50));
        list.add(new PrintDataModel("Seven", "6", 40));
        list.add(new PrintDataModel("Eight", "3", 60));
        list.add(new PrintDataModel("First", "2", 120));
        list.add(new PrintDataModel("Second", "4", 300));
        list.add(new PrintDataModel("Third", "6", 550));
        list.add(new PrintDataModel("Fourth", "1", 40));
        list.add(new PrintDataModel("Fifth", "2", 40));
        list.add(new PrintDataModel("Sixth", "4", 50));
        list.add(new PrintDataModel("Seven", "6", 40));
        list.add(new PrintDataModel("Eight", "3", 60));

        // create string builder in bill printing format
        StringBuilder printText = new StringBuilder();
        // header of bill
        printText.append("[C]Targets\n");
        printText.append("=======================================\n\n");
        printText.append("[L]Name" + " : " + " [C]Qty : " + " [R]Price \n\n");

        int total = 0;
        // body of bill , add one by one to string builder
        for (int i = 0; i < list.size(); i++) {
            int price = list.get(i).getPrice();
            total = total + price;
            String name = list.get(i).getName().substring(0, 5);
            printText.append("[L]" + name + " : [C]" + list.get(i).getQty() + " : [R]" + list.get(i).getPrice() + "\n");
        }

        // footer of bill
        printText.append("\n\n[C]=======================================\n");
        printText.append("[L]Total : " + "[R]" + total);

        TextView textView = findViewById(R.id.text);
        textView.setText(String.valueOf(printText));
    }

    // check permission if bluetooth is enable then print data
    private void printDataIfPermissionIsEnable() {
        // check permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH}, MainActivity.PERMISSION_BLUETOOTH);
        } else {
            // print
            new AsyncBluetoothEscPosPrint(MainActivity.this).execute(MainActivity.this.getAsyncEscPosPrinter(selectedDevice));
        }
    }

    // get permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case MainActivity.PERMISSION_BLUETOOTH: {
                    this.printDataIfPermissionIsEnable();
                    break;
                }
            }


        }
    }

    // get list of bluetooth paired device
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getBluetoothPairedDeviceList() {
        // get bluetooth peripheral connected device list
        final BluetoothConnection[] bluetoothDevicesList = (new BluetoothPrintersConnections()).getList();
        if (bluetoothDevicesList != null && bluetoothDevicesList.length != 0) {
            final String[] items = new String[bluetoothDevicesList.length];
            // items[0] = "Default printer";
            int i = 0;
            for (BluetoothConnection device : bluetoothDevicesList) {
                items[i++] = device.getDevice().getName();
            }
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle("Bluetooth printer selection");
            alertDialog.setItems(items, (dialogInterface, i1) -> {
                int index = i1 - 1;
                if (index == -1) {
                    selectedDevice = null;
                } else {
                    selectedDevice = bluetoothDevicesList[index];
                }
                btn.setText("Print");
            });

            AlertDialog alert = alertDialog.create();
            alert.setCanceledOnTouchOutside(false);
            alert.show();

        } else {

            Toast.makeText(MainActivity.this, "No Paired Device Found please scan for bluetooth device", Toast.LENGTH_SHORT).show();
        }
    }

    // print data
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        SimpleDateFormat format = new SimpleDateFormat("'on' yyyy-MM-dd 'at' HH:mm:ss");
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 32);

        // setup string builder for printing data
        StringBuilder printText = new StringBuilder();
        printText.append("[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.download, DisplayMetrics.DENSITY_MEDIUM)) + "</img>\n");
        printText.append("\n[C]Targets\n");
        printText.append("==============================\n\n");

        // append data in string printText
        int total = 0;
        for (int i = 0; i < list.size(); i++) {
            int price = list.get(i).getPrice();
            total = total + price;
            printText.append("[L]" + list.get(i).getName() + " : [C]" + list.get(i).getQty() + " : [R]" + list.get(i).getPrice() + "\n");
        }

        printText.append("[C]\n\n=======================================\n");
        printText.append("[L]Total : " + "[R]" + total);
        Log.d("test", "printResult: " + printText);

        return printer.setTextToPrint(printText.toString());

    }


    // close discovering if already in processing
    @Override
    public void onPause() {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
        }

        super.onPause();
    }

    // unregistered receiver on destroy
    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    // register receiver when activity start
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);
    }

    // broadcast receiver to detect state ,found device and scan available bluetooth device
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {
                    bottomSheetDialog.show();
                } else if (state == BluetoothAdapter.STATE_OFF) {
                    bottomSheetDialog.dismiss();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                progressBar.setVisibility(View.VISIBLE);
                bottomSheetDialog.show();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressBar.setVisibility(View.GONE);
                if (mDeviceList.size() > 0) {
                    // show bottom sheet
                    // click on list item in bottom sheet
                    adapter.setListener(position -> {
                        bottomSheetDialog.dismiss();
                        BluetoothDevice bluetoothDevice = mDeviceList.get(position);
                        try {
                            Method method = bluetoothDevice.getClass().getMethod("createBond", (Class[]) null);
                            method.invoke(bluetoothDevice, (Object[]) null);
                            Toast.makeText(MainActivity.this, "Device is paired now", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                } else {
                    bottomSheetDialog.dismiss();
                    Toast.makeText(MainActivity.this, "No Available Printer Device Found", Toast.LENGTH_SHORT).show();
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.IMAGING)
                if (device.getName() != null && !mDeviceList.contains(device))
                    mDeviceList.add(device);
                Log.d("uzair", "onReceive: " + device.getName());
                adapter.notifyDataSetChanged();
            }
        }
    };

}