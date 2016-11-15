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
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.bluetooth.mwoolley.microbitbledemo.Constants;
import com.bluetooth.mwoolley.microbitbledemo.Settings;

/**
 * Created by mwoolley on 21/11/2015.
 */
public class BleScannerAndroid4 extends BleScanner {

    public BleScannerAndroid4(Context context) {
        super(context);
    }

    private BluetoothAdapter.LeScanCallback le_scan_callback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scan_record) {
            if (!scanning) {
                return;
            }
            if (device_name_start != null && device.getName() != null && !device.getName().startsWith(device_name_start)) {
                return;
            }

            if (select_bonded_devices_only && Settings.getInstance().isFilter_unpaired_devices()) {
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    return;
                }
            }

            scan_results_consumer.candidateBleDevice(device, scan_record, rssi);
        }
    };

    @Override
    public void startScanning(ScanResultsConsumer scan_results_consumer) {
        if (scanning) {
            Log.d(Constants.TAG,"Already scanning so ignoring startScanning request");
            return;
        }
        this.scan_results_consumer = scan_results_consumer;
        setScanning(true);
        bluetooth_adapter.startLeScan(le_scan_callback);
    }

    public void startScanning(final ScanResultsConsumer scan_results_consumer, long stop_after_ms) {
        if (scanning) {
            Log.d(Constants.TAG,"Already scanning so ignoring startScanning request");
            return;
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bluetooth_adapter.stopLeScan(le_scan_callback);
                setScanning(false);
            }
        }, stop_after_ms);

        startScanning(scan_results_consumer);
    }

    @Override
    public void stopScanning() {
        setScanning(false);
        bluetooth_adapter.stopLeScan(le_scan_callback);
    }

}
