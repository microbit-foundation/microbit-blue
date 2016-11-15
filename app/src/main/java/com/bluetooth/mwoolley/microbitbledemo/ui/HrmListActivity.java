package com.bluetooth.mwoolley.microbitbledemo.ui;
/*
 * Author: Martin Woolley
 * Twitter: @bluetooth_mdw
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetooth.mwoolley.microbitbledemo.Constants;
import com.bluetooth.mwoolley.microbitbledemo.R;
import com.bluetooth.mwoolley.microbitbledemo.Settings;
import com.bluetooth.mwoolley.microbitbledemo.Utility;
import com.bluetooth.mwoolley.microbitbledemo.bluetooth.HrmAdapterService;
import com.bluetooth.mwoolley.microbitbledemo.bluetooth.BleScanner;
import com.bluetooth.mwoolley.microbitbledemo.bluetooth.BleScannerFactory;
import com.bluetooth.mwoolley.microbitbledemo.bluetooth.ScanResultsConsumer;

import java.util.ArrayList;
import java.util.List;

public class HrmListActivity extends AppCompatActivity implements ScanResultsConsumer {

    private boolean ble_scanning = false;
    private boolean service_connected = false;
    private boolean is_hrm=false;
    private Handler handler = new Handler();
    private ListAdapter ble_device_list_adapter;
    private BleScanner ble_scanner;
    private static final long SCAN_TIMEOUT = 30000;
    private static final int REQUEST_LOCATION = 0;
    private static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION};
    private boolean permissions_granted=false;
    private int device_count=0;
    private Toast toast;
    private BluetoothDevice selected_device;

    static class ViewHolder {
        public TextView text;

    }

    private HrmAdapterService bluetooth_le_adapter;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetooth_le_adapter = ((HrmAdapterService.LocalBinder) service).getService();
            bluetooth_le_adapter.setActivityHandler(mMessageHandler);
            service_connected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetooth_le_adapter = null;
            service_connected = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hrm_list);
        setButtonText();
        getSupportActionBar().setTitle(R.string.screen_title_main);
        showMsg(Utility.htmlColorGreen("Ready"));

        Settings.getInstance().restore(this);

        // connect to the Bluetooth service
        Intent gattServiceIntent = new Intent(this, HrmAdapterService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        ble_device_list_adapter = new ListAdapter();

        ListView listView = (ListView) this.findViewById(R.id.deviceList);
        listView.setAdapter(ble_device_list_adapter);

        ble_scanner = BleScannerFactory.getBleScanner(this.getApplicationContext());

        ble_scanner.setDevice_name_start(null);
        ble_scanner.setSelect_bonded_devices_only(false);

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (!service_connected) {
                    showMsg("Not yet ready - try again soon");
                    return;
                }

                if (ble_scanning) {
                    setScanState(false);
                    ble_scanner.stopScanning();
                }

                BluetoothDevice device = ble_device_list_adapter.getDevice(position);
                selected_device = device;
                connectToDevice(device);

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Settings.getInstance().save(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    public void onScan(View view) {

        if (!ble_scanner.isScanning()) {
            device_count=0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    permissions_granted = false;
                    requestLocationPermission();
                } else {
                    Log.i(Constants.TAG, "Location permission has already been granted. Starting scanning.");
                    permissions_granted = true;
                }
            } else {
                // the ACCESS_COARSE_LOCATION permission did not exist before M so....
                permissions_granted = true;
            }
            startScanning();
        } else {
            showMsg(Utility.htmlColorGreen("Stopping scanning"));
            ble_scanner.stopScanning();
        }
    }

    private void startScanning() {
        if (permissions_granted) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ble_device_list_adapter.clear();
                    ble_device_list_adapter.notifyDataSetChanged();
                }
            });
            simpleToast(getScanningMessage(),2000);
            ble_scanner.startScanning(this, SCAN_TIMEOUT);
        } else {
            showMsg(Utility.htmlColorRed("Permission to perform Bluetooth scanning was not yet granted"));
        }
    }

    // in theory it's not possible to get here without having granted permissions already. May remove this code!
    private void requestLocationPermission() {
        Log.i(Constants.TAG, "Location permission has NOT yet been granted. Requesting permission.");
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)){
            Log.i(Constants.TAG, "Displaying location permission rationale to provide additional context.");
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permission Required");
            builder.setMessage("Please grant Location access so this application can perform Bluetooth scanning");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    Log.d(Constants.TAG, "Requesting permissions after explanation");
                    ActivityCompat.requestPermissions(HrmListActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
                }
            });
            builder.show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            Log.i(Constants.TAG, "Received response for location permission request.");
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission has been granted
                Log.i(Constants.TAG, "Location permission has now been granted. Scanning.....");
                permissions_granted = true;
                if (ble_scanner.isScanning()) {
                    startScanning();
                }
            }else{
                Log.i(Constants.TAG, "Location permission was NOT granted.");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void connectToDevice(BluetoothDevice device) {
        showMsg(Utility.htmlColorBlue("Connecting to device"));
        if (bluetooth_le_adapter.connect(device.getAddress())) {
        } else {
            showMsg(Utility.htmlColorRed("onConnect: failed to connect"));
        }
    }
    // Service message handler�//////////////////
    private Handler mMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle;
            String service_uuid = "";
            String characteristic_uuid = "";
            byte[] b = null;
            TextView value_text = null;

            switch (msg.what) {
                case HrmAdapterService.GATT_CONNECTED:
                    showMsg(Utility.htmlColorGreen("Connected"));
                    showMsg(Utility.htmlColorGreen("Discovering services..."));
                    bluetooth_le_adapter.discoverServices();
                    break;
                case HrmAdapterService.GATT_DISCONNECT:
                    showMsg(Utility.htmlColorRed("Disconnected"));
                    break;
                case HrmAdapterService.GATT_SERVICES_DISCOVERED:
                    Log.d(Constants.TAG, "XXXX Services discovered");
                    showMsg(Utility.htmlColorGreen("Checking device capabilities"));
                    List<BluetoothGattService> slist = bluetooth_le_adapter.getSupportedGattServices();
                    is_hrm = false;
                    for (BluetoothGattService svc : slist) {
                        Log.d(Constants.TAG, "UUID=" + svc.getUuid().toString().toUpperCase() + " INSTANCE=" + svc.getInstanceId());
                        if (svc.getUuid().toString().equalsIgnoreCase(Utility.normaliseUUID(HrmAdapterService.HEARTRATE_SERVICE_UUID))) {
                            is_hrm = true;
                        }
                    }
                    if (!is_hrm) {
                        showMsg(Utility.htmlColorRed("Device is not a heart rate monitor"));
                    } else {
                        showMsg(Utility.htmlColorGreen("OK"));
                        Intent intent;
                        intent = new Intent(HrmListActivity.this, HrmActivity.class);
                        startActivity(intent);
                    }
                    break;
            }
        }
    };

    private void generalAlert(String title, String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.show();
    }

    private void simpleToast(String message, int duration) {
        toast = Toast.makeText(this, message, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void setScanState(boolean value) {
        ble_scanning = value;
        ((Button) this.findViewById(R.id.hrmScanButton)).setText(value ? Constants.STOP_SCANNING : Constants.FIND_HRM_MONITORS);
    }

    @Override
    public void candidateBleDevice(final BluetoothDevice device, byte[] scan_record, int rssi) {
        if (device != null && device.getName() != null && !device.getName().contains("BBC")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ble_device_list_adapter.addDevice(device);
                    ble_device_list_adapter.notifyDataSetChanged();
                    device_count++;
                }
            });
        }
    }

    @Override
    public void scanningStarted() {
        setScanState(true);
        showMsg(Utility.htmlColorGreen(getScanningMessage()));
    }

    @Override
    public void scanningStopped() {
        setScanState(false);
        if (device_count > 0) {
            showMsg(Utility.htmlColorGreen("Ready"));
        } else {
            showMsg(Utility.htmlColorRed(getNoneFoundMessage()));
        }
    }
    // adaptor
    private class ListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> ble_devices;

        public ListAdapter() {
            super();
            ble_devices = new ArrayList<BluetoothDevice>();

        }

        public void addDevice(BluetoothDevice device) {
            if (!ble_devices.contains(device)) {
                ble_devices.add(device);
            }
        }

        public boolean contains(BluetoothDevice device) {
            return ble_devices.contains(device);
        }

        public BluetoothDevice getDevice(int position) {
            return ble_devices.get(position);
        }

        public void clear() {
            ble_devices.clear();
        }

        @Override
        public int getCount() {
            return ble_devices.size();
        }

        @Override
        public Object getItem(int i) {
            return ble_devices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = HrmListActivity.this.getLayoutInflater().inflate(
                        R.layout.list_row, null);
                viewHolder = new ViewHolder();
                viewHolder.text = (TextView) view.findViewById(R.id.textView);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            BluetoothDevice device = ble_devices.get(i);
            String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.text.setText(deviceName);
            else
                viewHolder.text.setText("unknown device");

            return view;
        }
    }


    private void showMsg(final String msg) {
        Log.d(Constants.TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) HrmListActivity.this.findViewById(R.id.message)).setText(Html.fromHtml(msg));
            }
        });
    }

    private String getScanningMessage() {
        return "Scanning for heart rate monitors";
    }

    private void setButtonText() {
        String text=Constants.FIND_HRM_MONITORS;
        final String button_text = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) HrmListActivity.this.findViewById(R.id.hrmScanButton)).setText(button_text);
            }
        });

    }

    private String getNoneFoundMessage() {
        return "No devices found";
    }

}
