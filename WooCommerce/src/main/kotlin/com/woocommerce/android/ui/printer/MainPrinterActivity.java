package com.woocommerce.android.ui.printer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;


import com.epson.epos2.Epos2Exception;
import com.epson.epos2.Log;

import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.woocommerce.android.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainPrinterActivity extends Activity implements View.OnClickListener, ReceiveListener {

    private static final int REQUEST_PERMISSION = 100;
    private static final int DISCONNECT_INTERVAL = 500;//millseconds
    public static final String MyPREFERENCES = "MyPrinterPrefs" ;
    public static final String PRINTER_NAME = "PRINTER_NAME";
    private String strDeviceName = "";
    SharedPreferences sharedPreference;
    private Context mContext = null;
    public static EditText mEditTarget = null;
    public static Spinner mSpnSeries = null;
    public static Spinner mSpnLang = null;
    public static Printer mPrinter = null;
    public static ToggleButton mDrawer = null;

    private ArrayList<String> printDataOrderStatus;
    private ArrayList<String> printDataShippingMethodNotice;
    private ArrayList<String> printDataShippingLabelList;
    private ArrayList<String> printDataProductList;
    private ArrayList<String> printDataPaymentInfo;
    private ArrayList<String> printDataCustomerInfo;
    private ArrayList<String> printDataBillingInfo;
    private ArrayList<String> printDataShipmentList;
    private ArrayList<String> printDataNoteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_printer);
        sharedPreference = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

//        printDataOrderStatus = getIntent().getStringArrayListExtra("printDataOrderStatus");
//        printDataShippingMethodNotice = getIntent().getStringArrayListExtra("printDataShippingMethodNotice");
//        printDataShippingLabelList = getIntent().getStringArrayListExtra("printDataShippingLabelList");
//        printDataProductList = getIntent().getStringArrayListExtra("printDataProductList");
//        printDataPaymentInfo = getIntent().getStringArrayListExtra("printDataPaymentInfo");
//        printDataCustomerInfo = getIntent().getStringArrayListExtra("printDataCustomerInfo");
//        printDataBillingInfo = getIntent().getStringArrayListExtra("printDataBillingInfo");
//        printDataShipmentList = getIntent().getStringArrayListExtra("printDataShipmentList");
//        printDataNoteList = getIntent().getStringArrayListExtra("printDataNoteList");

        requestRuntimePermission();

        mContext = this;

        int[] target = {
            R.id.btnDiscovery,
            R.id.btnPrint,
            R.id.btnSampleCoupon,
                R.id.btnMonitor,
            R.id.btnSetting
        };

        for (int i = 0; i < target.length; i++) {
            Button button = (Button)findViewById(target[i]);
            button.setOnClickListener(this);
        }

        mSpnSeries = (Spinner)findViewById(R.id.spnModel);
        ArrayAdapter<SpnModelsItem> seriesAdapter = new ArrayAdapter<SpnModelsItem>(this, android.R.layout.simple_spinner_item);
        seriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_m10), Printer.TM_M10));
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_m30), Printer.TM_M30));
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_p20), Printer.TM_P20));
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_p60), Printer.TM_P60));
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_p60ii), Printer.TM_P60II));
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_p80), Printer.TM_P80));
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_t20), Printer.TM_T20));
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_t60), Printer.TM_T60));
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_t70), Printer.TM_T70));
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_t81), Printer.TM_T81));
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_t82), Printer.TM_T82));
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_t83), Printer.TM_T83));
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_t83iii), Printer.TM_T83III));
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_t88), Printer.TM_T88));
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_t90), Printer.TM_T90));
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_t90kp), Printer.TM_T90KP));
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_t100), Printer.TM_T100));
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_u220), Printer.TM_U220));
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_u330), Printer.TM_U330));
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_l90), Printer.TM_L90));
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_h6000), Printer.TM_H6000));
        seriesAdapter.add(new SpnModelsItem(getString(R.string.printerseries_m30ii), Printer.TM_M30II));
        mSpnSeries.setAdapter(seriesAdapter);
        mSpnSeries.setSelection(1);
        mSpnSeries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                finalizeObject();
                initializeObject();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ;
            }
        });

        mSpnLang = (Spinner)findViewById(R.id.spnLang);
        ArrayAdapter<SpnModelsItem> langAdapter = new ArrayAdapter<SpnModelsItem>(this, android.R.layout.simple_spinner_item);
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        langAdapter.add(new SpnModelsItem(getString(R.string.lang_ank), Printer.MODEL_ANK));
        langAdapter.add(new SpnModelsItem(getString(R.string.lang_japanese), Printer.MODEL_JAPANESE));
        langAdapter.add(new SpnModelsItem(getString(R.string.lang_chinese), Printer.MODEL_CHINESE));
        langAdapter.add(new SpnModelsItem(getString(R.string.lang_taiwan), Printer.MODEL_TAIWAN));
        langAdapter.add(new SpnModelsItem(getString(R.string.lang_korean), Printer.MODEL_KOREAN));
        langAdapter.add(new SpnModelsItem(getString(R.string.lang_thai), Printer.MODEL_THAI));
        langAdapter.add(new SpnModelsItem(getString(R.string.lang_southasia), Printer.MODEL_SOUTHASIA));
        mSpnLang.setAdapter(langAdapter);
        mSpnLang.setSelection(0);
        mSpnLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                finalizeObject();
                initializeObject();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ;
            }
        });


        mDrawer = (ToggleButton) findViewById(R.id.toggleDrawer);

        mEditTarget = (EditText)findViewById(R.id.edtTarget);

        initializeObject();

        try {
            Log.setLogSettings(mContext, Log.PERIOD_TEMPORARY, Log.OUTPUT_STORAGE, null, 0, 1, Log.LOGLEVEL_LOW);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "setLogSettings", mContext);
        }

        if(sharedPreference.contains(PRINTER_NAME))
        {
            strDeviceName = sharedPreference.getString(PRINTER_NAME, "");
            if(!strDeviceName.isEmpty())
            {
                mEditTarget.setText(strDeviceName);
            }
            else
            {
                openDiscoverDlg();
            }
        }
        else
        {
            openDiscoverDlg();
        }
    }

    @Override protected void onResume() {
        super.onResume();
    }

    @Override public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(getString(R.string.title_target), target);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    private AlertDialog alert11;
    private void openDiscoverDlg()
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(Html.fromHtml("<font color='#FF7F27'>There is no device. Will you search device?</font>"));
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Search now",
                (dialog, id) -> {
                    Intent intent = new Intent(MainPrinterActivity.this, DiscoveryActivity.class);
                    startActivityForResult(intent, 0);
                    alert11.dismiss();
                });

        builder1.setNegativeButton(
                "Cancel",
                (dialog, id) -> alert11.dismiss());

        alert11 = builder1.create();
        alert11.show();
    }

    @Override
    protected void onDestroy() {

        finalizeObject();
        super.onDestroy();
    }

    String target = "";
    @Override
    protected void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        if (data != null && resultCode == RESULT_OK) {
            target = data.getStringExtra(getString(R.string.title_target));
            if (target != null) {

                SharedPreferences.Editor editor = sharedPreference.edit();
                editor.putString(PRINTER_NAME, target);
                editor.commit();

                EditText mEdtTarget = (EditText)findViewById(R.id.edtTarget);
                mEdtTarget.setText(target);
            }
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;

        switch (v.getId()) {
            case R.id.btnDiscovery:
                intent = new Intent(this, DiscoveryActivity.class);
                startActivityForResult(intent, 0);
                break;

            case R.id.btnPrint:
                updateButtonState(false);
                if (!runPrintReceiptSequence()) {
                    updateButtonState(true);
                }
                break;

            case R.id.btnSampleCoupon:
                updateButtonState(false);
                if (!runPrintCouponSequence()) {
                    updateButtonState(true);
                }
                break;
            case R.id.btnMonitor:
                intent = new Intent(this, MonitorActivity.class);
                startActivityForResult(intent, 0);
                break;

            case R.id.btnSetting:
                intent = new Intent(this, SettingActivity.class);
                startActivityForResult(intent, 0);
                break;

            default:
                // Do nothing
                break;
        }
    }

    private boolean runPrintReceiptSequence() {

        if (!createReceiptData()) {
            return false;
        }

        if (!printData()) {
            return false;
        }

        return true;
    }

    private boolean createReceiptData() {
        String method = "";
        Bitmap logoData = BitmapFactory.decodeResource(getResources(), R.drawable.store);
        StringBuilder textData = new StringBuilder();
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;

        if (mPrinter == null) {
            return false;
        }

        try {

            if(mDrawer.isChecked()) {
                method = "addPulse";
                mPrinter.addPulse(Printer.PARAM_DEFAULT,
                        Printer.PARAM_DEFAULT);
            }

            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);

//            method = "addImage";
//            mPrinter.addImage(logoData, 0, 0,
//                              logoData.getWidth(),
//                              logoData.getHeight(),
//                              Printer.COLOR_1,
//                              Printer.MODE_MONO,
//                              Printer.HALFTONE_DITHER,
//                              Printer.PARAM_DEFAULT,
//                              Printer.COMPRESS_AUTO);

            method = "addFeedLine";
            mPrinter.addFeedLine(1);
            textData.append( printDataOrderStatus.get(0) + "\n");
            textData.append( printDataOrderStatus.get(1) + "\n");
            textData.append( printDataOrderStatus.get(2) + "\n");
            textData.append("\n");
            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addFeedLine";
            mPrinter.addFeedLine(1);
            textData.append("Product\n");
            textData.append("\n");
            for(int i = 0; i <printDataProductList.size(); i++)
            {
                textData.append( printDataProductList.get(i) + "\n");
            }
            textData.append("\n");
            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());


            method = "addFeedLine";
            mPrinter.addFeedLine(1);
            textData.append("Payment\n");
            textData.append("\n");
            textData.append( "Products total     " + printDataPaymentInfo.get(0) + "\n");
            textData.append( "Shipping           " + printDataPaymentInfo.get(1) + "\n");
            textData.append( "Taxes              " + printDataPaymentInfo.get(2) + "\n");
            textData.append( "Order total        " + printDataPaymentInfo.get(3) + "\n");
            textData.append("\n");
            textData.append( "Paid by customer   " + printDataPaymentInfo.get(4) + "\n");
            textData.append( printDataPaymentInfo.get(5) + "\n");
            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addFeedLine";
            mPrinter.addFeedLine(1);
            textData.append("Customer information\n");
            textData.append("\n");
            textData.append("Shipping details\n");
            for(int i = 0; i < printDataCustomerInfo.size(); i++)
            {
                textData.append( printDataCustomerInfo.get(i) + "\n");
            }
            textData.append("\n");
            textData.append("Billing details\n");
            for(int i = 0; i < printDataBillingInfo.size(); i++)
            {
                textData.append( printDataBillingInfo.get(i) + "\n");
            }
            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());


            method = "addFeedLine";
            mPrinter.addFeedLine(1);
            textData.append("Order notes\n");
            textData.append("\n");
            for(int i = 0; i < printDataNoteList.size(); i++)
            {
                textData.append( printDataNoteList.get(i) + "\n");
            }
            textData.append("\n");
            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

//            textData.append("SUBTOTAL                160.38\n");
//            textData.append("TAX                      14.43\n");
//            method = "addText";
//            mPrinter.addText(textData.toString());
//            textData.delete(0, textData.length());
//
//            method = "addTextSize";
//            mPrinter.addTextSize(2, 2);
//            method = "addText";
//            mPrinter.addText("TOTAL    174.81\n");
//            method = "addTextSize";
//            mPrinter.addTextSize(1, 1);
//            method = "addFeedLine";
//            mPrinter.addFeedLine(1);
//
//            textData.append("CASH                    200.00\n");
//            textData.append("CHANGE                   25.19\n");
//            textData.append("------------------------------\n");
//            method = "addText";
//            mPrinter.addText(textData.toString());
//            textData.delete(0, textData.length());
//
//            textData.append("Purchased item total number\n");
//            textData.append("Sign Up and Save !\n");
//            textData.append("With Preferred Saving Card\n");
//            method = "addText";
//            mPrinter.addText(textData.toString());
//            textData.delete(0, textData.length());
//            method = "addFeedLine";
//            mPrinter.addFeedLine(2);
//
//            method = "addBarcode";
//            mPrinter.addBarcode("01209457",
//                                Printer.BARCODE_CODE39,
//                                Printer.HRI_BELOW,
//                                Printer.FONT_A,
//                                barcodeWidth,
//                                barcodeHeight);

            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);
        }
        catch (Exception e) {
            mPrinter.clearCommandBuffer();
            ShowMsg.showException(e, method, mContext);
            return false;
        }

        return true;
    }

    private boolean runPrintCouponSequence() {

        if (!createCouponData()) {
            return false;
        }

        if (!printData()) {
            return false;
        }

        return true;
    }

    private boolean createCouponData() {
        String method = "";
        Bitmap coffeeData = BitmapFactory.decodeResource(getResources(), R.drawable.coffee);
        Bitmap wmarkData = BitmapFactory.decodeResource(getResources(), R.drawable.wmark);

        final int barcodeWidth = 2;
        final int barcodeHeight = 64;

        if (mPrinter == null) {
            return false;
        }

        try {
            method = "addImage";
            mPrinter.addImage(wmarkData, 0, 0, wmarkData.getWidth(), wmarkData.getHeight(), Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT);

            method = "addImage";
            mPrinter.addImage(coffeeData, 0, 0, coffeeData.getWidth(), coffeeData.getHeight(), Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT, 3, Printer.PARAM_DEFAULT);

            method = "addBarcode";
            mPrinter.addBarcode("01234567890", Printer.BARCODE_UPC_A, Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT, barcodeWidth, barcodeHeight);

            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);
        }
        catch (Exception e) {
            mPrinter.clearCommandBuffer();
            ShowMsg.showException(e, method, mContext);
            return false;
        }

        return true;
    }

    private boolean printData() {
        if (mPrinter == null) {
            return false;
        }

        if (!connectPrinter()) {
            mPrinter.clearCommandBuffer();
            return false;
        }

        try {
            mPrinter.sendData(Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            mPrinter.clearCommandBuffer();
            ShowMsg.showException(e, "sendData", mContext);
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                // Do nothing
            }
            return false;
        }

        return true;
    }

    private boolean initializeObject() {
        try {
//            mPrinter = new Printer(((SpnModelsItem) mSpnSeries.getSelectedItem()).getModelConstant(),
//                                   ((SpnModelsItem) mSpnLang.getSelectedItem()).getModelConstant(),
//
//                                   mContext);
            mPrinter = new Printer(Printer.TM_M30, Printer.MODEL_ANK, mContext);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "Printer", mContext);
            return false;
        }

        mPrinter.setReceiveEventListener(this);

        return true;
    }

    private void finalizeObject() {
        if (mPrinter == null) {
            return;
        }

        mPrinter.setReceiveEventListener(null);

        mPrinter = null;
    }

    private boolean connectPrinter() {
        if (mPrinter == null) {
            return false;
        }

        try {
            mPrinter.connect(mEditTarget.getText().toString(), Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "connect", mContext);
            return false;
        }

        return true;
    }

    private void disconnectPrinter() {
        if (mPrinter == null) {
            return;
        }

        while (true) {
            try {
                mPrinter.disconnect();
                break;
            } catch (final Exception e) {
                if (e instanceof Epos2Exception) {
                    //Note: If printer is processing such as printing and so on, the disconnect API returns ERR_PROCESSING.
                    if (((Epos2Exception) e).getErrorStatus() == Epos2Exception.ERR_PROCESSING) {
                        try {
                            Thread.sleep(DISCONNECT_INTERVAL);
                        } catch (Exception ex) {
                        }
                    }else{
                        runOnUiThread(new Runnable() {
                            public synchronized void run() {
                                ShowMsg.showException(e, "disconnect", mContext);
                            }
                        });
                        break;
                    }
                }else{
                    runOnUiThread(new Runnable() {
                        public synchronized void run() {
                            ShowMsg.showException(e, "disconnect", mContext);
                        }
                    });
                    break;
                }
            }
        }

        mPrinter.clearCommandBuffer();
    }

    private String makeErrorMessage(PrinterStatusInfo status) {
        String msg = "";

        if (status.getOnline() == Printer.FALSE) {
            msg += getString(R.string.handlingmsg_err_offline);
        }
        if (status.getConnection() == Printer.FALSE) {
            msg += getString(R.string.handlingmsg_err_no_response);
        }
        if (status.getCoverOpen() == Printer.TRUE) {
            msg += getString(R.string.handlingmsg_err_cover_open);
        }
        if (status.getPaper() == Printer.PAPER_EMPTY) {
            msg += getString(R.string.handlingmsg_err_receipt_end);
        }
        if (status.getPaperFeed() == Printer.TRUE || status.getPanelSwitch() == Printer.SWITCH_ON) {
            msg += getString(R.string.handlingmsg_err_paper_feed);
        }
        if (status.getErrorStatus() == Printer.MECHANICAL_ERR || status.getErrorStatus() == Printer.AUTOCUTTER_ERR) {
            msg += getString(R.string.handlingmsg_err_autocutter);
            msg += getString(R.string.handlingmsg_err_need_recover);
        }
        if (status.getErrorStatus() == Printer.UNRECOVER_ERR) {
            msg += getString(R.string.handlingmsg_err_unrecover);
        }
        if (status.getErrorStatus() == Printer.AUTORECOVER_ERR) {
            if (status.getAutoRecoverError() == Printer.HEAD_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_head);
            }
            if (status.getAutoRecoverError() == Printer.MOTOR_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_motor);
            }
            if (status.getAutoRecoverError() == Printer.BATTERY_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_battery);
            }
            if (status.getAutoRecoverError() == Printer.WRONG_PAPER) {
                msg += getString(R.string.handlingmsg_err_wrong_paper);
            }
        }
        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_0) {
            msg += getString(R.string.handlingmsg_err_battery_real_end);
        }

        return msg;
    }

    private void dispPrinterWarnings(PrinterStatusInfo status) {
        EditText edtWarnings = (EditText)findViewById(R.id.edtWarnings);
        String warningsMsg = "";

        if (status == null) {
            return;
        }

        if (status.getPaper() == Printer.PAPER_NEAR_END) {
            warningsMsg += getString(R.string.handlingmsg_warn_receipt_near_end);
        }

        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1) {
            warningsMsg += getString(R.string.handlingmsg_warn_battery_near_end);
        }

        edtWarnings.setText(warningsMsg);
    }

    private void updateButtonState(boolean state) {
        Button btnDiscovery = (Button)findViewById(R.id.btnDiscovery);
        Button btnReceipt = (Button)findViewById(R.id.btnPrint);
        Button btnCoupon = (Button)findViewById(R.id.btnSampleCoupon);
        Button btnMonitor = (Button)findViewById(R.id.btnMonitor);
        Button btnSetting = (Button)findViewById(R.id.btnSetting);

        btnDiscovery.setEnabled(state);
        btnReceipt.setEnabled(state);
        btnCoupon.setEnabled(state);
        btnMonitor.setEnabled(state);
        btnSetting.setEnabled(state);
    }

    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
        runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                ShowMsg.showResult(code, makeErrorMessage(status), mContext);

                dispPrinterWarnings(status);

                updateButtonState(true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        disconnectPrinter();
                    }
                }).start();
            }
        });
    }

    private void requestRuntimePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        int permissionStorage = ContextCompat
                .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> requestPermissions = new ArrayList<>();

        if (permissionStorage == PackageManager.PERMISSION_DENIED) {
            requestPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionLocation == PackageManager.PERMISSION_DENIED) {
            requestPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!requestPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, requestPermissions.toArray(new String[requestPermissions.size()]), REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode != REQUEST_PERMISSION || grantResults.length == 0) {
            return;
        }

        List<String> requestPermissions = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(permissions[i]);
            }
            if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                    && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(permissions[i]);
            }
        }

        if (!requestPermissions.isEmpty()) {
            ActivityCompat
                    .requestPermissions(this, requestPermissions.toArray(new String[requestPermissions.size()]), REQUEST_PERMISSION);
        }
    }
}
