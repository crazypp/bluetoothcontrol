package com.innopia.btcontrol;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.fragment.app.FragmentActivity;

/*
 * Main Activity class that loads {@link MainFragment}.
 */
public class MainActivity extends FragmentActivity {

    private static final String LOG_TAG = "[btcontrol]";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private BluetoothAdapter mBtAdapter;

    // UI
    private TextView mTextViewBluetoothName;
    private TextView mTextViewScanModeState;
    private ToggleButton mBtnPower;

    public void BtPowerOff() {
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            BluetoothAdapter badapter = BluetoothAdapter.getDefaultAdapter();
            @SuppressLint("MissingPermission") boolean ret = badapter.disable();
        }
    }

    public void BtPowerOn() {
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            BluetoothAdapter badapter = BluetoothAdapter.getDefaultAdapter();
            @SuppressLint("MissingPermission") boolean ret = badapter.enable();
        }
    }

    class SwitchBtnPowerListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton btnView, boolean isChecked){
            if(isChecked) {
                Log.d(LOG_TAG,"Switch to on.");
                BtPowerOn();
            } else {
                Log.d(LOG_TAG,"Switch to off.");
                BtPowerOff();
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // set current bluetooth name
        mTextViewBluetoothName = findViewById(R.id.textView_BluetoothName);
        mTextViewBluetoothName.setText(mBtAdapter.getName());

        // set current scan mode
        mTextViewScanModeState = findViewById(R.id.textView_ScanModeState);
        switch(mBtAdapter.getScanMode()) {
            case BluetoothAdapter.SCAN_MODE_NONE:
                mTextViewScanModeState.setText(R.string.bluetooth_scan_mode_none);
                break;
            case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                mTextViewScanModeState.setText(R.string.bluetooth_scan_mode_connectable);
                break;
            case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                mTextViewScanModeState.setText(R.string.bluetooth_scan_mode_connectable_discoverable);
                break;
        }

        mBtnPower = findViewById(R.id.btn_bt_power);
        mBtnPower.setOnCheckedChangeListener(new SwitchBtnPowerListener());

        // check bluetooth power state
        switch(mBtAdapter.getState()) {
            case BluetoothAdapter.STATE_OFF:
                mBtnPower.setChecked(false);
                break;
            case BluetoothAdapter.STATE_ON:
                mBtnPower.setChecked(true);
                break;
        }

        // add Intent Filter and Receiver
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver, filter);

        // request permission
        if(this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access for bluetooth control.");
            builder.setMessage("Please grant location access to control bluetooth.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(LOG_TAG,"Granted : Coarse location permission.");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to control bluetooth.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }// end of onRequestPermissionsResult

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch(state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(LOG_TAG,"Bluetooth State : Off");
                        mBtnPower.setChecked(false);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(LOG_TAG,"Bluetooth STATE_TURNING_OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(LOG_TAG,"Bluetooth State : On");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(LOG_TAG,"STATE_TURNING_ON STATE_ON");
                        break;
                }
            }else if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch(state) {
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(LOG_TAG, "SCAN_MODE_NONE");
                        mTextViewScanModeState.setText(R.string.bluetooth_scan_mode_none);
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(LOG_TAG, "SCAN_MODE_CONNECTABLE_DISCOVERABLE");
                        mTextViewScanModeState.setText(R.string.bluetooth_scan_mode_connectable_discoverable);
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(LOG_TAG, "SCAN_MODE_CONNECTABLE");
                        mTextViewScanModeState.setText(R.string.bluetooth_scan_mode_connectable);
                        break;
                }
            }
        }
    };
}