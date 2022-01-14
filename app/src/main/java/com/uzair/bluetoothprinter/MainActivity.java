package com.uzair.bluetoothprinter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
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
    Button btn;
    // bluetooth adapter
    BluetoothAdapter bluetoothAdapter;
    // bottom sheet views
    BottomSheetDialog availableDeviceBottomSheetDialog, pairedDeviceBottomSheetDialog;
    ProgressBar progressBar;
    AvailableDeviceAdapter adapter;
    ArrayList<BluetoothDevice> mDeviceList;
    ArrayList<String> pairedDeviceList;
    ArrayAdapter pairedDeviceListViewAdapter;
    ListView availableDeviceListView, pairedDeviceListView;
    AppCompatButton closeBtn, scanBtn;
    LocationManager locationManager;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpArrayList();
        init();
        setUpAvailableDeviceBottomSheetDialog();
        setUpPairedDeviceBottomSheetDialog();

    }

    // paired device bottom sheet list
    private void setUpPairedDeviceBottomSheetDialog() {
        // bottom sheet
        pairedDeviceBottomSheetDialog = new BottomSheetDialog(MainActivity.this, R.style.SheetDialog);
        pairedDeviceBottomSheetDialog.setContentView(R.layout.paired_device_bottom_sheet);
        pairedDeviceBottomSheetDialog.getBehavior().setDraggable(false);
        // views
        pairedDeviceListView = pairedDeviceBottomSheetDialog.findViewById(R.id.pairedDeviceList);
        pairedDeviceList = new ArrayList<>();


    }

    // available device bottom sheet list
    private void setUpAvailableDeviceBottomSheetDialog() {
        //bottom sheet dialog
        availableDeviceBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialog);
        availableDeviceBottomSheetDialog.setContentView(R.layout.available_device_bottom_sheet);
        availableDeviceBottomSheetDialog.getBehavior().setDraggable(false);
        // views of bottom sheet
        closeBtn = availableDeviceBottomSheetDialog.findViewById(R.id.closeBtn);
        scanBtn = availableDeviceBottomSheetDialog.findViewById(R.id.scanBtn);
        progressBar = availableDeviceBottomSheetDialog.findViewById(R.id.progressBarBT);
        availableDeviceListView = availableDeviceBottomSheetDialog.findViewById(R.id.deviceList);
        // list and adapter for available bluetooth list
        mDeviceList = new ArrayList<BluetoothDevice>();
        adapter = new AvailableDeviceAdapter(mDeviceList, MainActivity.this);
        availableDeviceListView.setAdapter(adapter);


        // scan button click
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    mDeviceList.clear();
                    adapter.notifyDataSetChanged();
                    bluetoothAdapter.startDiscovery();
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(MainActivity.this, "Please enable gps to scan available device ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // click on bottom sheet close button
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                availableDeviceBottomSheetDialog.dismiss();
                mDeviceList.clear();
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
            }
        });
    }

    private void init() {
        // location manage for gps checking
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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

                    // connect with printer
                    if (checkLocationPermission()) {
                        // here will show paired device list
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
                        // scan button to start bluetooth available device scanning
                        if (bluetoothAdapter.isEnabled()) {
                            if (checkLocationPermission()) {
                                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                    mDeviceList.clear();
                                    bluetoothAdapter.startDiscovery();
                                } else {
                                    Toast.makeText(MainActivity.this, "Please enable gps to scan available device", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                requestLocationPermission();
                            }
                        } else {
                            showBluetoothEnableDialog();

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
    ActivityResultLauncher<Intent> launchBluetoothActivity = registerForActivityResult(
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
            showBluetoothEnableDialog();
        }
    }

    // show dialog to enable bluetooth
    private void showBluetoothEnableDialog() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        launchBluetoothActivity.launch(enableBtIntent);
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
        // get bluetooth paired device list
        pairedDeviceList.clear();
        final BluetoothConnection[] bluetoothDevicesList = (new BluetoothPrintersConnections()).getList();
        if (bluetoothDevicesList != null && bluetoothDevicesList.length != 0) {
            // make list of device from bluetooth connection list
            for (BluetoothConnection device : bluetoothDevicesList) {
                pairedDeviceList.add(device.getDevice().getName());
            }

            // init adapter
            pairedDeviceListViewAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, pairedDeviceList) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    // change item color to white
                    View view = super.getView(position, convertView, parent);
                    TextView textView = (TextView) view.findViewById(android.R.id.text1);
                    textView.setTextColor(Color.WHITE);
                    return view;
                }
            };

            // add click on list view item
            pairedDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectedDevice = bluetoothDevicesList[position];
                    btn.setText("Print");
                    pairedDeviceBottomSheetDialog.dismiss();
                }
            });

            // setup adapter and show dialog
            pairedDeviceListView.setAdapter(pairedDeviceListViewAdapter);
            pairedDeviceListViewAdapter.notifyDataSetChanged();
            pairedDeviceBottomSheetDialog.show();

        } else {
            Toast.makeText(MainActivity.this, "No Paired Device Found Please Scan For Available Bluetooth Device", Toast.LENGTH_LONG).show();
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
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        bluetoothAdapter.startDiscovery();
                        availableDeviceBottomSheetDialog.show();
                    } else {
                        Toast.makeText(MainActivity.this, "Please enable gps to scan available device", Toast.LENGTH_SHORT).show();
                    }
                } else if (state == BluetoothAdapter.STATE_OFF) {
                    if (bluetoothAdapter.isDiscovering()) {
                        bluetoothAdapter.cancelDiscovery();
                    }
                    availableDeviceBottomSheetDialog.dismiss();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                progressBar.setVisibility(View.VISIBLE);
                availableDeviceBottomSheetDialog.show();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressBar.setVisibility(View.GONE);
                if (mDeviceList.size() > 0) {
                    // click on list item in bottom sheet
                    adapter.setListener(position -> {
                        availableDeviceBottomSheetDialog.dismiss();
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
                    // availableDeviceBottomSheetDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Please Scan Again No Available Device Found", Toast.LENGTH_SHORT).show();
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