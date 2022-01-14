package com.uzair.bluetoothprinter;

import android.content.Context;

import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;

public class AsyncBluetoothEscPosPrint extends AsyncEscPosPrint {
    public AsyncBluetoothEscPosPrint(Context context) {
        super(context);
    }

    protected Integer doInBackground(AsyncEscPosPrinter... printersData) {
        if (printersData.length == 0) {
            return AsyncEscPosPrint.FINISH_NO_PRINTER;
        }

        // get printer connection
        AsyncEscPosPrinter printerData = printersData[0];
        DeviceConnection deviceConnection = printerData.getPrinterConnection();

        // update progress dialog
        this.publishProgress(AsyncEscPosPrint.PROGRESS_CONNECTING);

        // check connection is success or not
        if (deviceConnection == null) {
            printersData[0] = new AsyncEscPosPrinter(
                    BluetoothPrintersConnections.selectFirstPaired(),
                    printerData.getPrinterDpi(),
                    printerData.getPrinterWidthMM(),
                    printerData.getPrinterNbrCharactersPerLine()
            );
            // set print data
            printersData[0].setTextToPrint(printerData.getTextToPrint());
        } else {
            try {
                // connect printer device
                deviceConnection.connect();
            } catch (EscPosConnectionException e) {
                e.printStackTrace();
            }
        }

        return super.doInBackground(printersData);
    }
}
