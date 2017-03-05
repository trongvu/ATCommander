package com.android.trongvu.atcommander;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidshell.R;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends Activity {

    public static final String ACTION_USB_PERMISSION = "com.android.trongvu.atcommander.USB_PERMISSION";
    public static final String LOG_TAG = "ATCommander";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                UsbDevice dev = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    connectUSBDevice(dev);
                } else {
                    Log.d(LOG_TAG, "permission denied for accessory " + dev);
                }
            }
        }
    };
    EditText input;
    Button btn;
    TextView out;
    ScrollView mScrollView;
    String command;
    UsbDevice dev = null;
    UsbDeviceConnection mUsbDeviceConnection = null;
    UsbManager mUsbManager = null;
    Handler mHandler = new Handler();
    UsbSerialDevice serialPort = null;
    private PendingIntent mPermissionIntent;
    private boolean isConnected = false;
    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(final byte[] arg0) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    out.append(new String(arg0) + "\n");
                }
            });
            // we could not scroll till the end right after set text
            // so we wait for 100ms before scrolling
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            }, 100);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        input = (EditText) findViewById(R.id.txt);
        btn = (Button) findViewById(R.id.btn);
        out = (TextView) findViewById(R.id.out);
        mScrollView = (ScrollView) findViewById(R.id.scrollview);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                onExecute(arg0);
            }
        });
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onExecute(btn);
                    return true;
                }
                return false;
            }
        });
    }

    private void connectUSBDevice(UsbDevice dev) {
        synchronized (this) {
            if (dev != null) {
                Log.i(LOG_TAG, "open device");
                mUsbDeviceConnection = mUsbManager.openDevice(dev);
                serialPort = UsbSerialDevice.createUsbSerialDevice(dev, mUsbDeviceConnection);
                if (serialPort == null) {
                    Log.i(LOG_TAG, "open Serial port == null");
                    mUsbDeviceConnection.close();
                    return;
                }
                Log.i(LOG_TAG, "open Serial port != null");
                if (!serialPort.open()) {
                    Log.i(LOG_TAG, "open Serial port open failed");
                    mUsbDeviceConnection.close();
                    return;
                }
                // Devices are opened with default values, Usually
                // 9600,8,1,None,OFF
                // CDC driver default values 115200,8,1,None,OFF
                Log.i(LOG_TAG, "open Serial port opened");
                serialPort.setBaudRate(115200);
                serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                serialPort.read(mCallback);
                Toast.makeText(getApplicationContext(), "AT ready", Toast.LENGTH_SHORT).show();
                isConnected = true;
            }
        }
    }

    private void onExecute(View arg0) {
        // send AT command
        if (!isConnected || serialPort == null) {
            Log.i(LOG_TAG, "!isConnected || serialPort == null");
            Toast.makeText(getApplicationContext(), "AT is not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        String command = input.getText().toString();
        Log.i(LOG_TAG, command);
        if ("clear".equalsIgnoreCase(command)) {
            out.setText("");
            input.setText("");
        } else {
            command += "\r\n";
            serialPort.write(command.getBytes());
            out.append(command);
            input.setText("");
        }
    }

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
        // TODO Auto-generated method stub
        super.onResume();
        mUsbManager = (UsbManager) getApplicationContext().getSystemService(Context.USB_SERVICE);
        mUsbManager.getDeviceList();
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        //dev block, check if is there any CDC devices
        while (deviceIterator.hasNext()) {
            UsbDevice tmp = deviceIterator.next();
            out.append("vid = " + Integer.toHexString(tmp.getVendorId()) + ":" +
                    " pid = " + Integer.toHexString(tmp.getProductId()) +
                    " isCdcDevice = " + UsbSerialDevice.isCdcDevice(tmp) + "\n");
        }

        //reset to 1st position
        deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            dev = deviceIterator.next();
        }
        if (dev == null) {
            Log.i(LOG_TAG, "dev == null");
            return;
        }
        Log.i(LOG_TAG, "dev != null, switch mode");
        //new ExecuteAsRootBase().execute();
        // we should request permision here
        if(!mUsbManager.hasPermission(dev)) {
            if(ExecuteAsRootBase.canRunRootCommands()){
                //we can run root command, so change configuration before request permission
                ExecuteAsRootBase.execute();
            }
            mUsbManager.requestPermission(dev, mPermissionIntent);
        }else{
            connectUSBDevice(dev);
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        if(serialPort != null){
            serialPort.close();
        }
        if(mUsbDeviceConnection != null){
            mUsbDeviceConnection.close();
        }
    }
}
