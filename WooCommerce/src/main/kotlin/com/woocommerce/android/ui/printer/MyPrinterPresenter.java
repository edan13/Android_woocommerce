package com.woocommerce.android.ui.printer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.woocommerce.android.R;

import java.util.ArrayList;
import java.util.List;

public class MyPrinterPresenter implements ReceiveListener {

    private static final int REQUEST_PERMISSION = 100;
    private static final int DISCONNECT_INTERVAL = 500;//millseconds
    public static final String MyPREFERENCES = "MyPrinterPrefs" ;
    public static final String PRINTER_NAME = "PRINTER_NAME";
    private String strDeviceName = "";
    SharedPreferences sharedPreference;
    private Activity mActivity = null;
//    public static EditText mEditTarget = null;
//    public static Spinner mSpnSeries = null;
//    public static Spinner mSpnLang = null;
    public static Printer mPrinter = null;
//    public static ToggleButton mDrawer = null;

    private List<String> printDataOrderStatus;
    private List<String> printDataShippingMethodNotice;
    private List<String> printDataShippingLabelList;
    private List<String> printDataProductList;
    private List<String> printDataPaymentInfo;
    private List<String> printDataCustomerInfo;
    private List<String> printDataBillingInfo;
    private List<String> printDataShipmentList;
    private List<String> printDataNoteList;


    public MyPrinterPresenter(Activity activity,
                     List<String> printDataOrderStatus,
                     List<String> printDataShippingMethodNotice,
                     List<String> printDataShippingLabelList,
                     List<String> printDataProductList,
                     List<String> printDataPaymentInfo,
                     List<String> printDataCustomerInfo,
                     List<String> printDataBillingInfo,
                     List<String> printDataShipmentList,
                     List<String> printDataNoteList
                     )
    {
        mActivity = activity;
        this.printDataOrderStatus = printDataOrderStatus;
        this.printDataShippingMethodNotice = printDataShippingMethodNotice;
        this.printDataShippingLabelList = printDataShippingLabelList;
        this.printDataProductList = printDataProductList;
        this.printDataPaymentInfo = printDataPaymentInfo;
        this.printDataCustomerInfo = printDataCustomerInfo;
        this.printDataBillingInfo = printDataBillingInfo;
        this.printDataShipmentList = printDataShipmentList;
        this.printDataNoteList = printDataNoteList;

        initializeObject();
    }


    public void setDeviceName(String strDeviceName)
    {
        this.strDeviceName = strDeviceName;
    }

    private boolean initializeObject() {
        try {
//            mPrinter = new Printer(((SpnModelsItem) mSpnSeries.getSelectedItem()).getModelConstant(),
//                                   ((SpnModelsItem) mSpnLang.getSelectedItem()).getModelConstant(),
//
//                                   mContext);
            mPrinter = new Printer(Printer.TM_M30, Printer.MODEL_ANK, mActivity);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "Printer", mActivity);
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
            mPrinter.connect(strDeviceName, Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "connect", mActivity);
            return false;
        }

        return true;
    }

    public void printOrder()
    {
        if (!runPrintReceiptSequence()) {
        }
        else
        {
            Toast.makeText(mActivity, "Unknown Error.", Toast.LENGTH_SHORT).show();
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
        Bitmap logoData = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.store);
        StringBuilder textData = new StringBuilder();
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;

        if (mPrinter == null) {
            return false;
        }

        try {

//            if(mDrawer.isChecked()) {
//                method = "addPulse";
//                mPrinter.addPulse(Printer.PARAM_DEFAULT,
//                        Printer.PARAM_DEFAULT);
//            }

            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);

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

            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);
        }
        catch (Exception e) {
            mPrinter.clearCommandBuffer();
            ShowMsg.showException(e, method, mActivity);
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
            ShowMsg.showException(e, "sendData", mActivity);
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
                        mActivity.runOnUiThread(new Runnable() {
                            public synchronized void run() {
                                ShowMsg.showException(e, "disconnect", mActivity);
                            }
                        });
                        break;
                    }
                }else{
                    mActivity.runOnUiThread(new Runnable() {
                        public synchronized void run() {
                            ShowMsg.showException(e, "disconnect", mActivity);
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
            msg += mActivity.getString(R.string.handlingmsg_err_offline);
        }
        if (status.getConnection() == Printer.FALSE) {
            msg += mActivity.getString(R.string.handlingmsg_err_no_response);
        }
        if (status.getCoverOpen() == Printer.TRUE) {
            msg += mActivity.getString(R.string.handlingmsg_err_cover_open);
        }
        if (status.getPaper() == Printer.PAPER_EMPTY) {
            msg += mActivity.getString(R.string.handlingmsg_err_receipt_end);
        }
        if (status.getPaperFeed() == Printer.TRUE || status.getPanelSwitch() == Printer.SWITCH_ON) {
            msg += mActivity.getString(R.string.handlingmsg_err_paper_feed);
        }
        if (status.getErrorStatus() == Printer.MECHANICAL_ERR || status.getErrorStatus() == Printer.AUTOCUTTER_ERR) {
            msg += mActivity.getString(R.string.handlingmsg_err_autocutter);
            msg += mActivity.getString(R.string.handlingmsg_err_need_recover);
        }
        if (status.getErrorStatus() == Printer.UNRECOVER_ERR) {
            msg += mActivity.getString(R.string.handlingmsg_err_unrecover);
        }
        if (status.getErrorStatus() == Printer.AUTORECOVER_ERR) {
            if (status.getAutoRecoverError() == Printer.HEAD_OVERHEAT) {
                msg += mActivity.getString(R.string.handlingmsg_err_overheat);
                msg += mActivity.getString(R.string.handlingmsg_err_head);
            }
            if (status.getAutoRecoverError() == Printer.MOTOR_OVERHEAT) {
                msg += mActivity.getString(R.string.handlingmsg_err_overheat);
                msg += mActivity.getString(R.string.handlingmsg_err_motor);
            }
            if (status.getAutoRecoverError() == Printer.BATTERY_OVERHEAT) {
                msg += mActivity.getString(R.string.handlingmsg_err_overheat);
                msg += mActivity.getString(R.string.handlingmsg_err_battery);
            }
            if (status.getAutoRecoverError() == Printer.WRONG_PAPER) {
                msg += mActivity.getString(R.string.handlingmsg_err_wrong_paper);
            }
        }
        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_0) {
            msg += mActivity.getString(R.string.handlingmsg_err_battery_real_end);
        }

        return msg;
    }

    private void dispPrinterWarnings(PrinterStatusInfo status) {
        String warningsMsg = "";
        if (status == null) {
            return;
        }

        if (status.getPaper() == Printer.PAPER_NEAR_END) {
            warningsMsg += mActivity.getString(R.string.handlingmsg_warn_receipt_near_end);
        }

        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1) {
            warningsMsg += mActivity.getString(R.string.handlingmsg_warn_battery_near_end);
        }

        Toast.makeText(mActivity, warningsMsg, Toast.LENGTH_SHORT).show();
    }

//    private void updateButtonState(boolean state) {
//        Button btnDiscovery = (Button)findViewById(R.id.btnDiscovery);
//        Button btnReceipt = (Button)findViewById(R.id.btnPrint);
//        Button btnCoupon = (Button)findViewById(R.id.btnSampleCoupon);
//        Button btnMonitor = (Button)findViewById(R.id.btnMonitor);
//        Button btnSetting = (Button)findViewById(R.id.btnSetting);
//
//        btnDiscovery.setEnabled(state);
//        btnReceipt.setEnabled(state);
//        btnCoupon.setEnabled(state);
//        btnMonitor.setEnabled(state);
//        btnSetting.setEnabled(state);
//    }
//


    @Override public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                ShowMsg.showResult(code, makeErrorMessage(status), mActivity);

                dispPrinterWarnings(status);

//                updateButtonState(true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        disconnectPrinter();
                    }
                }).start();
            }
        });
    }
}
