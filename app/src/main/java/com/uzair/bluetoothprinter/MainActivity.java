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
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.PhoneNumberUtils;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity {

    public static final int PERMISSION_BLUETOOTH = 1;
    private BluetoothConnection selectedDevice;
    List<PrintDataModel> dataModelList;
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


        // scan button click to start scanning for available bluetooth device
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if gps is enable then start scanning
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

        // click on bottom sheet close button and stop scanning if already in scanning
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


        // click on function button
        findViewById(R.id.fucntion)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // add data
                        TestModel testModel1 = new TestModel(101, 2, 10, 2, 1000);
                        TestModel testModel2 = new TestModel(102, 3, 5, 3, 600);
                        TestModel testModel3 = new TestModel(103, 2, 5.5, 4, 1000);

                        // get data with result
                        TestModel result1 = testFunction(testModel1);
                        TestModel result2 = testFunction(testModel2);
                        TestModel result3 = testFunction(testModel3);
                        // show data in log
                        Log.d("testModel", " ******* Value Add*****");
                        Log.d("testModelOne", "onClick: id " + result1.getItemId());
                        Log.d("testModelOne", "onClick: qty " + result1.getQty());
                        Log.d("testModelOne", "onClick: dis value " + result1.getDiscVal());
                        Log.d("testModelOne", "onClick: dis type " + result1.getDisType());
                        Log.d("testModelOne", "onClick: amount " + result1.getAmount());
                        Log.d("testModelOne", "onClick: result " + result1.getResult());

                        Log.d("testModel", " ******* Value subtract*****");
                        Log.d("testModelTwo", "onClick: id " + result2.getItemId());
                        Log.d("testModelTwo", "onClick: qty " + result2.getQty());
                        Log.d("testModelThree", "onClick: dis value " + result2.getDiscVal());
                        Log.d("testModelFour", "onClick: dis type " + result2.getDisType());
                        Log.d("testModelFifth", "onClick: amount " + result2.getAmount());
                        Log.d("testModelSixth", "onClick: result " + result2.getResult());

                        Log.d("testModel", "******* Percentage*****");
                        Log.d("testModelTwo", "onClick: id " + result3.getItemId());
                        Log.d("testModelTwo", "onClick: qty " + result3.getQty());
                        Log.d("testModelThree", "onClick: dis value " + result3.getDiscVal());
                        Log.d("testModelFour", "onClick: dis type " + result3.getDisType());
                        Log.d("testModelFifth", "onClick: amount " + result3.getAmount());
                        Log.d("testModelSixth", "onClick: result " + result3.getResult());


                    }
                });
    }

    // function to show result according disc type
    private TestModel testFunction(TestModel testModel) {
        double result = 0;
        switch (testModel.getDisType()) {
            case 1: {
                // qty
                break;
            }
            case 2: {
                // value +
                result = testModel.getAmount() + (testModel.getQty() * testModel.getDiscVal());
                break;
            }

            case 3: {
                // value -
                result = testModel.getAmount() - (testModel.getQty() * testModel.getDiscVal());
                break;
            }

            case 4: {
                //value percentage
                result = testModel.getAmount() - ((int) (testModel.getAmount() * (testModel.getDiscVal() / 100.0f)));
                break;
            }
        }

        return new TestModel(testModel.getItemId(), testModel.getQty(), testModel.discVal, testModel.getDisType(), testModel.getAmount(), result);

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
                if (btn.getText().toString().trim().equals("Print")) {
                    printDataIfPermissionIsEnable();
                } else if (btn.getText().toString().trim().equals("Connect")) {
                    // check permissions
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
                            // check location permission
                            if (checkLocationPermission()) {
                                // check gps is enable or not
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


        // click on whatsapp button
        findViewById(R.id.whatsapp)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (isStoragePermissionEnabled()) {
                            try {
                                PackageManager packageManager = getPackageManager();
                                // get the file path
                                Uri uri = FileProvider.getUriForFile(MainActivity.this,
                                        MainActivity.this.getPackageName() + ".provider", generatePdfFile());

                                Intent i = new Intent(Intent.ACTION_SEND);
                               // String phone = "+923047901748";
                                //    String url = "https://api.whatsapp.com/send?phone=" + phone +"&text="+uri;
                                    i.setPackage("com.whatsapp");
                                 //   i.setData(Uri.parse(url));
                                    i.setType("application/pdf");
                                    i.putExtra(Intent.EXTRA_STREAM, uri);
                                    if (i.resolveActivity(packageManager) != null) {
                                        startActivity(i);
                                    }
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                                Log.d("pdfError", "onClick: " + e.getMessage());
                            }


                        } else {
                            requestStoragePermission();
                        }
                    }
                });


    }

    /// check runtime storage permission
    private boolean isStoragePermissionEnabled() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)) {

            return false;
        }
        return true;
    }

    // request for permission
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                101);
    }

    // create and return pdf file
    private File generatePdfFile() throws IOException {
        // storage path
        String extstoragedir = Environment.getExternalStorageDirectory().getPath();
        File folder = new File(extstoragedir, "Target");

        // if folder not exist create it
        if (!folder.exists()) {
            folder.mkdir();
        }

        // generate random integer for storage file name
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Bill" + n + ".pdf";

        // storage resource
        final File file = new File(folder, fname);
        if (!file.exists())
            file.createNewFile();
        FileOutputStream fOut = new FileOutputStream(file);


        // set pdf height
        int pdfHeight, itemHeight = 15, header = 150, footer = 120;
        pdfHeight = (itemHeight * dataModelList.size()) + header + footer;

        // pdf doc
        PdfDocument pdfDocument = new PdfDocument();
        Paint paintText = new Paint();
        Paint forLinePaint = new Paint();
        /// create page
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(260, pdfHeight, 1)
                .create();
        /// set page to document
        PdfDocument.Page myPage = pdfDocument.startPage(myPageInfo);
        Canvas canvas = myPage.getCanvas();

        // app name or customer name
        paintText.setTextSize(13.3f);
        paintText.setColor(getResources().getColor(R.color.black));
        canvas.drawText("Customer Name", 20, 25, paintText);
        canvas.drawText("Uzair Aziz", 160, 25, paintText);
        canvas.drawText("Phone No", 20, 40, paintText);
        canvas.drawText("+9304790148", 160, 40, paintText);
        canvas.drawText("Shop Name", 20, 55, paintText);
        canvas.drawText("Good Luck", 160, 55, paintText);
        // draw line after above text
        forLinePaint.setStyle(Paint.Style.STROKE);
        forLinePaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));
        forLinePaint.setStrokeWidth(1);
        canvas.drawLine(20, 70, 250, 70, forLinePaint);


        /// column title
        paintText.setTextSize(15.3f);
        paintText.setColor(getResources().getColor(R.color.black));
        canvas.drawText("Items", 20, 90, paintText);
        canvas.drawText("Qty", 110, 90, paintText);
        canvas.drawText("Price", 200, 90, paintText);
        canvas.drawText("", 200, 95, paintText);
        // column values
        paintText.setTextSize(12.3f);
        paintText.setColor(getResources().getColor(R.color.grey));
        int verticalSpace = 90;
        int total = 0;

        // add items one by one
        for (int i = 0; i < dataModelList.size(); i++) {
            total = total + dataModelList.get(i).getPrice();
            verticalSpace = verticalSpace + 15;
            canvas.drawText(dataModelList.get(i).getName(), 20, verticalSpace, paintText);
            canvas.drawText(dataModelList.get(i).getQty(), 110, verticalSpace, paintText);
            canvas.drawText("" + dataModelList.get(i).getPrice(), 200, verticalSpace, paintText);
        }

        // draw line after above text
        forLinePaint.setStyle(Paint.Style.STROKE);
        forLinePaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));
        forLinePaint.setStrokeWidth(1);
        canvas.drawLine(20, verticalSpace + 20, 250, verticalSpace + 20, forLinePaint);

        // total
        paintText.setTextSize(13.3f);
        paintText.setColor(getResources().getColor(R.color.black));
        canvas.drawText("Total", 20, verticalSpace + 45, paintText);
        canvas.drawText("" + total, 200, verticalSpace + 45, paintText);
        canvas.drawText("Disc Value", 20, verticalSpace + 60, paintText);
        canvas.drawText("20", 200, verticalSpace + 60, paintText);
        canvas.drawText("GST ", 20, verticalSpace + 75, paintText);
        canvas.drawText("10", 200, verticalSpace + 75, paintText);

        // finish page
        pdfDocument.finishPage(myPage);

        /// store pdf file in external storage.
        pdfDocument.writeTo(fOut);
        pdfDocument.close();


        return file;
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

    // bluetooth enable activity result
    ActivityResultLauncher<Intent> launchBluetoothActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                } else {
                    Toast.makeText(MainActivity.this, "Unable to use bluetooth printer . please enable bluetooth", Toast.LENGTH_SHORT).show();
                }
            });

    // dummy array list
    private void setUpArrayList() {

        dataModelList = new ArrayList<>();
        dataModelList.add(new PrintDataModel("First", "2", 120));
        dataModelList.add(new PrintDataModel("Second", "4", 300));
        dataModelList.add(new PrintDataModel("Third", "6", 550));
        dataModelList.add(new PrintDataModel("Fourth", "1", 40));
        dataModelList.add(new PrintDataModel("Fifth", "2", 40));
        dataModelList.add(new PrintDataModel("Sixth", "4", 50));
        dataModelList.add(new PrintDataModel("Seven", "6", 40));
        dataModelList.add(new PrintDataModel("Eight", "3", 60));
        dataModelList.add(new PrintDataModel("First", "2", 120));
        dataModelList.add(new PrintDataModel("Second", "4", 300));
        dataModelList.add(new PrintDataModel("Third", "6", 550));
        dataModelList.add(new PrintDataModel("Fourth", "1", 40));
        dataModelList.add(new PrintDataModel("Fifth", "2", 40));
        dataModelList.add(new PrintDataModel("Sixth", "4", 50));
        dataModelList.add(new PrintDataModel("Seven", "6", 40));
        dataModelList.add(new PrintDataModel("Eight", "3", 60));
        dataModelList.add(new PrintDataModel("First", "2", 120));
        dataModelList.add(new PrintDataModel("Second", "4", 300));
        dataModelList.add(new PrintDataModel("Third", "6", 550));
        dataModelList.add(new PrintDataModel("Fourth", "1", 40));
        dataModelList.add(new PrintDataModel("Fifth", "2", 40));
        dataModelList.add(new PrintDataModel("Sixth", "4", 50));
        dataModelList.add(new PrintDataModel("Seven", "6", 40));
        dataModelList.add(new PrintDataModel("Eight", "3", 60));
        dataModelList.add(new PrintDataModel("First", "2", 120));
        dataModelList.add(new PrintDataModel("Second", "4", 300));
        dataModelList.add(new PrintDataModel("Third", "6", 550));
        dataModelList.add(new PrintDataModel("Fourth", "1", 40));
        dataModelList.add(new PrintDataModel("Fifth", "2", 40));
        dataModelList.add(new PrintDataModel("Sixth", "4", 50));
        dataModelList.add(new PrintDataModel("Seven", "6", 40));
        dataModelList.add(new PrintDataModel("Eight", "3", 60));
        dataModelList.add(new PrintDataModel("First", "2", 120));
        dataModelList.add(new PrintDataModel("Second", "4", 300));
        dataModelList.add(new PrintDataModel("Third", "6", 550));
        dataModelList.add(new PrintDataModel("Fourth", "1", 40));
        dataModelList.add(new PrintDataModel("Fifth", "2", 40));
        dataModelList.add(new PrintDataModel("Sixth", "4", 50));
        dataModelList.add(new PrintDataModel("Seven", "6", 40));
        dataModelList.add(new PrintDataModel("Eight", "3", 60));
        dataModelList.add(new PrintDataModel("First", "2", 120));
        dataModelList.add(new PrintDataModel("Second", "4", 300));
        dataModelList.add(new PrintDataModel("Third", "6", 550));
        dataModelList.add(new PrintDataModel("Fourth", "1", 40));
        dataModelList.add(new PrintDataModel("Fifth", "2", 40));
        dataModelList.add(new PrintDataModel("Sixth", "4", 50));
        dataModelList.add(new PrintDataModel("Seven", "6", 40));
        dataModelList.add(new PrintDataModel("Eight", "3", 60));

        // create string builder in bill printing format
//        StringBuilder printText = new StringBuilder();
//        printText.append("[L]Name[C]Qty[R]Price\n");
//        printText.append("[C]\n");
//        printText.append("[L] ******************************** \n");
//        // printText.append("[L]\n");
//
//        int total = 0;
//        // body of bill , add one by one to string builder
//        for (int i = 0; i < list.size(); i++) {
//            int price = list.get(i).getPrice();
//            total = total + price;
//            String name = list.get(i).getName().substring(0, 5);
//            printText.append("[L]" + name + "[C]" + list.get(i).getQty() + "[R]" + list.get(i).getPrice()+"\n");
//            // printText.append("[C]\n");
//        }
//
//        // footer of bill
//        printText.append("[C]\n");
//        printText.append("[L]********************************\n");
//        printText.append("<b><font size='big'>[R]Total</font></b>" + "<b><font size='big'>[R]" + total + "</font></b>");
//
//        TextView textView = findViewById(R.id.text);
//        textView.setText(String.valueOf(printText));
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
        // clear list and get bluetooth paired device list
        pairedDeviceList.clear();
        final BluetoothConnection[] bluetoothDevicesList = (new BluetoothPrintersConnections()).getList();
        if (bluetoothDevicesList != null && bluetoothDevicesList.length != 0) {
            // make list of device from bluetooth connection list
            for (BluetoothConnection device : bluetoothDevicesList) {
                pairedDeviceList.add(device.getDevice().getName());
            }

            // init list view adapter
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
        //  SimpleDateFormat format = new SimpleDateFormat("'on' yyyy-MM-dd 'at' HH:mm:ss");
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 32);

        StringBuilder printText = new StringBuilder();
        /**
         * header of bill
         */
        //    printText.append("[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.download, DisplayMetrics.DENSITY_MEDIUM)) + "</img>\n\n");
        printText.append("[L]Customer Name[R]Uzair Aziz\n");
        printText.append("[L]Phone[R]03030405060\n");
        printText.append("[L]Shop Address[R]Dist Kohat KPK\n");
        printText.append("[L]************[C]************[R]***\n");

        int total = 0;
        /**
         *body of bill , add one by one to string builder
         */
        printText.append("[L]<b>Name</b>[C]<b>Qty</b>[R]<b>Price</b>\n");
        printText.append("[L]************[C]************[R]***\n");
        for (int i = 0; i < dataModelList.size(); i++) {

            int price = dataModelList.get(i).getPrice();
            total = total + price;
            String name = dataModelList.get(i).getName().substring(0, 5);
            printText.append("[L]" + name + "[C]" + dataModelList.get(i).getQty() + "[R]" + dataModelList.get(i).getPrice() + "\n");
        }

        /**
         *   footer of bill
         */

        printText.append("[C]\n");
        printText.append("[L]**********[C]****************[R]**\n");
        printText.append("<b>[R]Total</b>" + "<b>[R]" + total + "</b>\n");
        printText.append("<b>[R]GST LNC</b>" + "<b>[R]5%</b>\n");
        printText.append("<b>[R]Discount</b>" + "<b>[R]2%</b>");

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

    // broadcast receiver to detect state ,found available device and scan for available bluetooth device
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {
                    // if gps is enable then start scanning
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
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                progressBar.setVisibility(View.VISIBLE);
                availableDeviceBottomSheetDialog.show();

            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressBar.setVisibility(View.GONE);
                if (mDeviceList.size() > 0) {
                    // click on list item in bottom sheet
                    adapter.setListener(position -> {
                        availableDeviceBottomSheetDialog.dismiss();
                        BluetoothDevice bluetoothDevice = mDeviceList.get(position);
                        try {
                            // create bond or paired device
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
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
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