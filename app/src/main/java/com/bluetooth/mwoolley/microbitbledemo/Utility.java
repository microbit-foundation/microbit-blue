package com.bluetooth.mwoolley.microbitbledemo;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import android.view.View;

/*
 * Author: Martin Woolley
 * Twitter: @bluetooth_mdw
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
public class Utility {

    private static final String BLUETOOTH_SIG_UUID_BASE = "0000XXXX-0000-1000-8000-00805f9b34fb";
    public static final String TAG = "BBC microbit";
    private static final String HEX_CHARS="01234567890ABCDEF";
    /*
     * time smoothing constant for low-pass filter
     * 0 ≤ alpha ≤ 1 ; a smaller value basically means more smoothing
     * See: http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
     */
    static final float ALPHA = 0.15f;

    /**
     * @see http://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
     * @see http://developer.android.com/reference/android/hardware/SensorEvent.html#values
     */
    public static float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    public static String normaliseUUID(String uuid) {
        String normalised_128_bit_uuid = uuid;
        if (uuid.length() == 4) {
            normalised_128_bit_uuid = BLUETOOTH_SIG_UUID_BASE.replace("XXXX",uuid);
        }
        if (uuid.length() == 32) {
            normalised_128_bit_uuid = uuid.substring(0,8) + "-"
                    + uuid.substring(8,12) + "-"
                    + uuid.substring(12,16) + "-"
                    + uuid.substring(16,20) + "-"
                    + uuid.substring(20,32);
        }
        return normalised_128_bit_uuid;
    }

    public static String extractCharacteristicUuidFromTag(String tag) {
        String uuid="";
        String [] parts = tag.split("_");
        if (parts.length == 4) {
            uuid = parts[3];
        }
        return uuid;
    }

    public static short shortFromLittleEndianBytes(byte [] bytes) {

        if (bytes == null || bytes.length != 2) {
            return 0;
        }

        short result=0;
        result = (short) (((bytes[1]  & 0xff ) << 8) + (bytes[0] & 0xff));
        if ((result | 0x8000) == 0x8000 ) {
            result = (short) (result * -1);
        }
        return result;
    }

    public static short shortFromLittleEndianBytes(byte b0, byte b1) {

        short result=0;
        result = (short) (((b1  & 0xff ) << 8) + (b0 & 0xff));
        if ((result | 0x8000) == 0x8000 ) {
            result = (short) (result * -1);
        }
        return result;
    }

    public static int intFromLittleEndianBytes(byte [] bytes) {

        if (bytes == null || bytes.length != 4) {
            return 0;
        }

        int result=0;
        result = (int) ((bytes[3] << 24) + (bytes[2] << 16) + (bytes[1] << 8) + bytes[0]);
        if ((result | 0x80000000) == 0x80000000 ) {
            result = (int) (result * -1);
        }
        return result;
    }

    public static String extractServiceUuidFromTag(String tag) {
        String uuid="";
        String [] parts = tag.split("_");
        if (parts.length == 4) {
            uuid = parts[2];
        }
        return uuid;
    }

	public static byte[] getByteArrayFromHexString(String hex_string) {
     String hex = hex_string.replace(" ","");
     hex = hex.toUpperCase();
	
		byte[] bytes = new byte[hex.length() / 2];
		int i = 0;
		int j = 0;
		while (i < hex.length()) {
			String h1 = hex.substring(i, i + 1);
			String h2 = hex.substring(i + 1, i + 2);
			try {
				int b = (Integer.valueOf(h1, 16).intValue() * 16) + (Integer.valueOf(h2, 16).intValue());
				bytes[j++] = (byte) b;
				i = i + 2;
			} catch (NumberFormatException e) {
				System.out.println("NFE handling " + h1 + h2 + " with i=" + i);
				throw e;
			}
		}
		return bytes;
	}


	public static String byteArrayAsHexString(byte[] bytes) {
		if (bytes == null) {
			return "[null]";
		}
		int l = bytes.length;
		StringBuffer hex = new StringBuffer();
		for (int i = 0; i < l; i++) {
			if ((bytes[i] >= 0) & (bytes[i] < 16))
				hex.append("0");
			hex.append(Integer.toString(bytes[i] & 0xff, 16).toUpperCase());
		}
		return hex.toString();
	}

    public static byte [] leBytesFromShort(short s) {
        byte [] bytes = new byte[2];
        bytes[0] = (byte)(s & 0xff);
        bytes[1] = (byte)((s >> 8) & 0xff);
        return bytes;
    }

    public static byte [] leBytesFromTwoShorts(short s1,short s2) {
        byte [] bytes = new byte[4];
        bytes[0] = (byte)(s1 & 0xff);
        bytes[1] = (byte)((s1 >> 8) & 0xff);
        bytes[2] = (byte)(s2 & 0xff);
        bytes[3] = (byte)((s2 >> 8) & 0xff);
        return bytes;
    }

    public static byte [] leBytesFromInt(int s) {
        byte [] bytes = new byte[4];
        bytes[0] = (byte)(s & 0xff);
        bytes[1] = (byte)((s >> 8) & 0xff);
        bytes[2] = (byte)((s >> 16) & 0xff);
        bytes[3] = (byte)((s >> 24) & 0xff);
        return bytes;
    }

    public static byte [] beBytesFromInt(int s) {
        byte [] bytes = new byte[4];
        bytes[3] = (byte)(s & 0xff);
        bytes[2] = (byte)((s >> 8) & 0xff);
        bytes[1] = (byte)((s >> 16) & 0xff);
        bytes[0] = (byte)((s >> 24) & 0xff);
        return bytes;
    }

    public static boolean isValidHex(String hex_string) {
        System.out.println("isValidHex("+hex_string+")");
        String hex = hex_string.replace(" ","");
        hex = hex.toUpperCase();
        int len = hex.length();
        int remainder = len % 2;
        if (remainder != 0) {
            System.out.println("isValidHex: not even number of chars");
            return false;
        }
        for (int i=0;i<len;i++) {
            if (!HEX_CHARS.contains(hex.substring(i,i+1))) {
                return false;
            }
        }
        return true;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexArray = HEX_CHARS.toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String htmlColorRed(String s) {
        s = "<font color=\"#FF0000\">" + s + "</font>";
        return s;
    }

    public static String htmlColorGreen(String s) {
        s = "<font color=\"#009933\">" + s + "</font>";
        return s;
    }

    public static String htmlColorBlue(String s) {
        s = "<font color=\"#3300CC\">" + s + "</font> ";
        return s;
    }

    public static void main(String args[]) {

        short s1 = 15;
        byte[] s1_bytes = leBytesFromShort(s1);
        System.out.println(byteArrayAsHexString(s1_bytes));

        short s2 = 256;
        byte[] s2_bytes = leBytesFromShort(s2);
        System.out.println(byteArrayAsHexString(s2_bytes));

        byte [] s1_s2_bytes = leBytesFromTwoShorts(s1,s2);
        System.out.println(byteArrayAsHexString(s1_s2_bytes));

    }

    public static int toggleVisibility(int current_visibility) {
        if (current_visibility == View.INVISIBLE) {
            return View.VISIBLE;
        } else {
            return View.INVISIBLE;
        }
    }


}