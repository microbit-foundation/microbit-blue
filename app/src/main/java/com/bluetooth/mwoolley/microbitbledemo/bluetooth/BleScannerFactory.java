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
import android.content.Context;
import android.os.Build;

/**
 * Created by mwoolley on 21/11/2015.
 */
public class BleScannerFactory {

    public static BleScanner getBleScanner(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new BleScannerPostAndroid4(context);
        } else {
            return new BleScannerAndroid4(context);
        }
    }

}