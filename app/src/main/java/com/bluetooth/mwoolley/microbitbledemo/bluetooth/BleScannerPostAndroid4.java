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
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.bluetooth.mwoolley.microbitbledemo.Constants;
import com.bluetooth.mwoolley.microbitbledemo.Settings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mwoolley on 21/11/2015.
 */
public class BleScannerPostAndroid4 extends BleScanner {

    private static final int PERMISSIONS_ACCESS_COARSE_LOCATION=1;
    private BluetoothLeScanner scanner = null;

    public BleScannerPostAndroid4(Context context) {
        super(context);
    }

    @Override
    public void startScanning(ScanResultsConsumer scan_results_consumer) {
        if (scanning) {
            Log.d(Constants.TAG,"Already scanning so ignoring startScanning request");
            return;
        }
        this.scan_results_consumer = scan_results_consumer;
        setScanning(true);
        scanLeDevices();
    }

    @Override
    public void startScanning(final ScanResultsConsumer scan_results_consumer, long stop_after_ms) {
        if (scanning) {
            Log.d(Constants.TAG, "Already scanning so ignoring startScanning request");
            return;
        }
        if (scanner == null) {
            scanner = bluetooth_adapter.getBluetoothLeScanner();
            Log.d(Constants.TAG, "Created BluetoothScanner object");
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(Constants.TAG, "Stopping scanning");
                scanner.stopScan(scan_callback);
                setScanning(false);
            }
        }, stop_after_ms);

        startScanning(scan_results_consumer);
    }

    @Override
    public void stopScanning() {
        setScanning(false);
        scanner.stopScan(scan_callback);
    }

    private ScanCallback scan_callback = new ScanCallback() {
        public void onScanResult(int callbackType, final ScanResult result) {
            if (!scanning) {
                return;
            }
            if (device_name_start != null && result.getDevice().getName() != null && !result.getDevice().getName().startsWith(device_name_start)) {
                return;
            }

            if (select_bonded_devices_only && Settings.getInstance().isFilter_unpaired_devices()) {
                if (result.getDevice().getBondState() != BluetoothDevice.BOND_BONDED) {
                    return;
                }
            }

            scan_results_consumer.candidateBleDevice(result.getDevice(), result.getScanRecord().getBytes(), result.getRssi());
        }
    };

    private void scanLeDevices() {
        Log.d(Constants.TAG,"Scanning");
        List<ScanFilter> filters;
        filters = new ArrayList<ScanFilter>();
        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        setScanning(true);
        scanner.startScan(filters, settings, scan_callback);
    }

}
