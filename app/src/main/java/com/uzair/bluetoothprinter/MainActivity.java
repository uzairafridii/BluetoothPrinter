package com.uzair.bluetoothprinter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

import java.io.DataInput;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity {

    public static final int PERMISSION_BLUETOOTH = 1;
    private BluetoothConnection selectedDevice;
    List<PrintDataModel> list;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    Button btn;
    // bluetooth adapter
    BluetoothAdapter bluetoothAdapter;
    ProgressDialog mProgressDlg;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setMessage("Scanning...");
        mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                bluetoothAdapter.cancelDiscovery();
            }
        });

        btn = findViewById(R.id.connect);
        setUpArrayList();


        /// click to connect with printer
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn.getText().toString().equals("Print")) {
                    printDataIfPermissionIsEnable();
                } else if (btn.getText().toString().equals("Connect")) {
                    // Getting the Bluetooth adapter
                    if (checkRuntimePermission()) {
                        showPairedDevice();
                    } else {
                        requestPermissions(new String[]{
                                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                101);
                    }
                }

            }
        });

        // click on scan
        findViewById(R.id.scan)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (checkRuntimePermission()) {
                            bluetoothAdapter.startDiscovery();
                        } else {
                            requestPermissions(new String[]{
                                            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                    101);
                        }
                    }
                });

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkRuntimePermission() {
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
                    showPairedDevice();
                } else {
                    Toast.makeText(MainActivity.this, "Unable to use bluetooth printer . please enable bluetooth", Toast.LENGTH_SHORT).show();
                }
            });


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showPairedDevice() {
        // Getting the Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {
            getBluetoothPairedDeviceList();
        } else {
            //Prompt user to turn on Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            launchSomeActivity.launch(enableBtIntent);
        }
    }

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
        StringBuilder builder = new StringBuilder();
        // header of bill
        builder.append("[C]Targets\n");
        builder.append("=======================================\n\n");
        builder.append("[L]Name" + " : " + " [C]Qty : " + " [R]Price \n\n");

        int total = 0;
        // body of bill , add one by one to string builder
        for (int i = 0; i < list.size(); i++) {
            int price = list.get(i).getPrice();
            total = total + price;
            builder.append("[L]" + list.get(i).getName() + " : [C]" + list.get(i).getQty() + " : [R]" + list.get(i).getPrice() + "\n");
        }

        // footer of bill
        builder.append("\n\n[C]=======================================\n");
        builder.append("[L]Total : " + "[R]" + total);

        TextView textView = findViewById(R.id.text);
        textView.setText(String.valueOf(builder));
    }

    // check permission if bluetooth is enable
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

            Toast.makeText(MainActivity.this, "No Paired Device Found", Toast.LENGTH_SHORT).show();
        }
    }

    // print data
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        SimpleDateFormat format = new SimpleDateFormat("'on' yyyy-MM-dd 'at' HH:mm:ss");
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 32);

        StringBuilder builder = new StringBuilder();
        builder.append("[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.ic_launcher_background, DisplayMetrics.DENSITY_MEDIUM)) + "</img>\n");
        builder.append("\n[C]Targets\n");
        builder.append("==============================\n\n");

        int total = 0;
        for (int i = 0; i < list.size(); i++) {
            int price = list.get(i).getPrice();
            total = total + price;
            builder.append("[L]" + list.get(i).getName() + " : [C]" + list.get(i).getQty() + " : [R]" + list.get(i).getPrice() + "\n");
        }

        builder.append("[C]\n\n=======================================\n");
        builder.append("[L]Total : " + "[R]" + total);

        TextView textView = findViewById(R.id.text);
        textView.setText(String.valueOf(builder));
        Log.d("test", "printResult: " + builder);

        return printer.setTextToPrint(builder.toString());

//        return printer.setTextToPrint(
//                "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.ic_launcher_background, DisplayMetrics.DENSITY_MEDIUM)) + "</img>\n" +
//                        "[L]\n" +
//                        "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
//                        "[L]\n" +
//                        "[C]<u type='double'>" + format.format(new Date()) + "</u>\n" +
//                        "[C]\n" +
//                        "[C]================================\n" +
//                        "[L]\n" +
//                        "[L]<b>BEAUTIFUL SHIRT</b>[R]9.99€\n" +
//                        "[L]  + Size : S\n" +
//                        "[L]\n" +
//                        "[L]<b>AWESOME HAT</b>[R]24.99€\n" +
//                        "[L]  + Size : 57/58\n" +
//                        "[L]\n" +
//                        "[C]--------------------------------\n" +
//                        "[R]TOTAL PRICE :[R]34.98€\n" +
//                        "[R]TAX :[R]4.23€\n" +
//                        "[L]\n" +
//                        "[C]================================\n" +
//                        "[L]\n" +
//                        "[L]<u><font color='bg-black' size='tall'>Customer :</font></u>\n" +
//                        "[L]Raymond DUPONT\n" +
//                        "[L]5 rue des girafes\n" +
//                        "[L]31547 PERPETES\n" +
//                        "[L]Tel : +33801201456\n" +
//                        "\n" +
//                        "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
//                        "[L]\n" +
//                        "[C]<qrcode size='20'>http://www.developpeur-web.dantsu.com/</qrcode>\n"
//        );
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

    /// broadcast receiver to scan available bluetooth device
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {
                    Toast.makeText(MainActivity.this, "Bluetooth Enabled", Toast.LENGTH_SHORT).show();
                } else if (state == BluetoothAdapter.STATE_OFF) {
                    Toast.makeText(MainActivity.this, "Bluetooth Disabled", Toast.LENGTH_SHORT).show();
                }
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList = new ArrayList<BluetoothDevice>();
                mProgressDlg.show();
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mProgressDlg.dismiss();

                if (mDeviceList.size() > 0) {
                    View mView = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_dialog, null);
                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setView(mView);
                    Dialog dialog = alert.create();
                    dialog.show();
                    ListView listView = dialog.findViewById(R.id.deviceList);
                    AvailableDeviceAdapter adapter = new AvailableDeviceAdapter(mDeviceList, MainActivity.this);
                    listView.setAdapter(adapter);

                    // click on item
                    adapter.setListener(position -> {
                        dialog.dismiss();
                        BluetoothDevice bluetoothDevice = mDeviceList.get(position);
                        try {
                            Method method = bluetoothDevice.getClass().getMethod("createBond", (Class[]) null);
                            method.invoke(bluetoothDevice, (Object[]) null);
                            Toast.makeText(MainActivity.this, "Device is paired now", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });


                    dialog.show();
                } else {
                    Toast.makeText(MainActivity.this, "No Available Printer Device Found", Toast.LENGTH_SHORT).show();
                }

            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.IMAGING) {
                    if (device.getName() != null && !mDeviceList.contains(device))
                        mDeviceList.add(device);
                } else {
//                    Log.d("foundDevice", "onReceive: other " + device.getName());
                }
            }
        }
    };

}