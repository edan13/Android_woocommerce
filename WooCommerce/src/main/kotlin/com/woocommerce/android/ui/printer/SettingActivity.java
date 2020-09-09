package com.woocommerce.android.ui.printer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.woocommerce.android.R;

public class SettingActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        int[] target = {
                R.id.btnMaintenanceCounterPrinterSetting,
                R.id.btnFirmwareUpdate
        };

        for (int i = 0; i < target.length; i++) {
            Button button = (Button)findViewById(target[i]);
            button.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;

        switch (v.getId()) {
            case R.id.btnMaintenanceCounterPrinterSetting:
                intent = new Intent(this, PrinterSettingActivity.class);
                startActivityForResult(intent, 0);
                break;

            case R.id.btnFirmwareUpdate:
                intent = new Intent(this, FirmwareUpdateActivity.class);
                startActivityForResult(intent, 0);
                break;

            default:
                // Do nothing
                break;
        }
    }

}
