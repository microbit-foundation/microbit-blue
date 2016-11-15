package com.bluetooth.mwoolley.microbitbledemo.bluetooth;
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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.bluetooth.mwoolley.microbitbledemo.Constants;

/**
 * Created by mwoolley on 21/11/2015.
 */
abstract public class BleScanner {

    BluetoothAdapter bluetooth_adapter = null;
    Handler handler = new Handler();
    ScanResultsConsumer scan_results_consumer;
    Context context;
    boolean scanning=false;
    String device_name_start="";
    boolean select_bonded_devices_only=true;

    public BleScanner(Context context) {
        this.context = context;

        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetooth_adapter = bluetoothManager.getAdapter();

        // check bluetooth is available and on
        if (bluetooth_adapter == null || !bluetooth_adapter.isEnabled()) {
            Log.d(Constants.TAG, "Bluetooth is NOT switched on");
            Intent enableBtIntent = new Intent(	BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(enableBtIntent);
        }
        Log.d(Constants.TAG, "Bluetooth is switched on");
    }

    abstract public void startScanning(ScanResultsConsumer scan_results_consumer);
    abstract public void startScanning(ScanResultsConsumer scan_results_consumer, long stop_after_ms);
    abstract public void stopScanning();

    public boolean isScanning() {
        return scanning;
    }

    void setScanning(boolean scanning) {
        this.scanning = scanning;
        if (!scanning) {
            scan_results_consumer.scanningStopped();
        } else {
            scan_results_consumer.scanningStarted();        }
    }

    public String getDevice_name_start() {
        return device_name_start;
    }

    public void setDevice_name_start(String device_name_start) {
        this.device_name_start = device_name_start;
    }

    public boolean isSelect_bonded_devices_only() {
        return select_bonded_devices_only;
    }

    public void setSelect_bonded_devices_only(boolean select_bonded_devices_only) {
        this.select_bonded_devices_only = select_bonded_devices_only;
    }

}
