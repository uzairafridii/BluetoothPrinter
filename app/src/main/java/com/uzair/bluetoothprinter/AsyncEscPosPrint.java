package com.uzair.bluetoothprinter;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import androidx.appcompat.app.AlertDialog;

import com.dantsu.escposprinter.EscPosCharsetEncoding;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;

import java.lang.ref.WeakReference;

public class AsyncEscPosPrint extends AsyncTask<AsyncEscPosPrinter, Integer, Integer> {

    // const for result
    protected final static int FINISH_SUCCESS = 1;
    protected final static int FINISH_NO_PRINTER = 2;
    protected final static int FINISH_PRINTER_DISCONNECTED = 3;
    protected final static int FINISH_PARSER_ERROR = 4;
    protected final static int FINISH_ENCODING_ERROR = 5;
    protected final static int FINISH_BARCODE_ERROR = 6;

    // const for progress dialog
    protected final static int PROGRESS_CONNECTING = 1;
    protected final static int PROGRESS_CONNECTED = 2;
    protected final static int PROGRESS_PRINTING = 3;
    protected final static int PROGRESS_PRINTED = 4;

    protected ProgressDialog dialog;
    protected WeakReference<Context> weakContext;


    public AsyncEscPosPrint(Context context) {
        this.weakContext = new WeakReference<>(context);
    }


    @Override
    protected Integer doInBackground(AsyncEscPosPrinter... printersData) {
        if (printersData.length == 0) {
            return AsyncEscPosPrint.FINISH_NO_PRINTER;
        }

        // updated progress
        this.publishProgress(AsyncEscPosPrint.PROGRESS_CONNECTING);

        AsyncEscPosPrinter printerData = printersData[0];

        try {
            // check printer connection
            DeviceConnection deviceConnection = printerData.getPrinterConnection();
            if (deviceConnection == null) {
                return AsyncEscPosPrint.FINISH_NO_PRINTER;
            }

            // init printer class
            EscPosPrinter printer = new EscPosPrinter(
                    deviceConnection,
                    printerData.getPrinterDpi(),
                    printerData.getPrinterWidthMM(),
                    printerData.getPrinterNbrCharactersPerLine(),
                    new EscPosCharsetEncoding("windows-1252", 16)
            );

            // update progress to printing
            this.publishProgress(AsyncEscPosPrint.PROGRESS_PRINTING);

            // print the data
            printer.printFormattedTextAndCut(printerData.getTextToPrint());

            // update the progress to printed
            this.publishProgress(AsyncEscPosPrint.PROGRESS_PRINTED);

            // show error message according error
        } catch (EscPosConnectionException e) {
            e.printStackTrace();
            return AsyncEscPosPrint.FINISH_PRINTER_DISCONNECTED;
        } catch (EscPosParserException e) {
            e.printStackTrace();
            return AsyncEscPosPrint.FINISH_PARSER_ERROR;
        } catch (EscPosEncodingException e) {
            e.printStackTrace();
            return AsyncEscPosPrint.FINISH_ENCODING_ERROR;
        } catch (EscPosBarcodeException e) {
            e.printStackTrace();
            return AsyncEscPosPrint.FINISH_BARCODE_ERROR;
        }

        // show success message
        return AsyncEscPosPrint.FINISH_SUCCESS;
    }

    // pre execute called when task is started.
    protected void onPreExecute() {
        if (this.dialog == null) {
            Context context = weakContext.get();

            if (context == null) {
                return;
            }

            // setup progress dialog
            this.dialog = new ProgressDialog(context);
            this.dialog.setTitle("Printing in progress...");
            this.dialog.setMessage("...");
            this.dialog.setProgressNumberFormat("%1d / %2d");
            this.dialog.setCancelable(false);
            this.dialog.setIndeterminate(false);
            this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.dialog.show();
        }
    }

    // update progress dialog with message
    protected void onProgressUpdate(Integer... progress) {

        switch (progress[0]) {
            case AsyncEscPosPrint.PROGRESS_CONNECTING:
                this.dialog.setMessage("Connecting printer...");
                break;
            case AsyncEscPosPrint.PROGRESS_CONNECTED:
                this.dialog.setMessage("Printer is connected...");
                break;
            case AsyncEscPosPrint.PROGRESS_PRINTING:
                this.dialog.setMessage("Printer is printing...");
                break;
            case AsyncEscPosPrint.PROGRESS_PRINTED:
                this.dialog.setMessage("Printer has finished...");
                break;
        }
        this.dialog.setProgress(progress[0]);
        this.dialog.setMax(4);
    }

    // on post execute called when task is completed
    protected void onPostExecute(Integer result) {
        this.dialog.dismiss();
        this.dialog = null;

        Context context = weakContext.get();

        if (context == null) {
            return;
        }

        // get result and show message according result
        switch (result) {
            case AsyncEscPosPrint.FINISH_SUCCESS:
                new AlertDialog.Builder(context)
                        .setTitle("Success")
                        .setMessage("Congratulation ! The text is printed !")
                        .show();
                break;
            case AsyncEscPosPrint.FINISH_NO_PRINTER:
                new AlertDialog.Builder(context)
                        .setTitle("No printer")
                        .setMessage("The application can't find any printer connected.")
                        .show();
                break;
            case AsyncEscPosPrint.FINISH_PRINTER_DISCONNECTED:
                new AlertDialog.Builder(context)
                        .setTitle("Broken connection")
                        .setMessage("Unable to connect the printer.")
                        .show();
                break;
            case AsyncEscPosPrint.FINISH_PARSER_ERROR:
                new AlertDialog.Builder(context)
                        .setTitle("Invalid formatted text")
                        .setMessage("It seems to be an invalid syntax problem.")
                        .show();
                break;
            case AsyncEscPosPrint.FINISH_ENCODING_ERROR:
                new AlertDialog.Builder(context)
                        .setTitle("Bad selected encoding")
                        .setMessage("The selected encoding character returning an error.")
                        .show();
                break;
            case AsyncEscPosPrint.FINISH_BARCODE_ERROR:
                new AlertDialog.Builder(context)
                        .setTitle("Invalid barcode")
                        .setMessage("Data send to be converted to barcode or QR code seems to be invalid.")
                        .show();
                break;
        }
    }
}
