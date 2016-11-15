package com.bluetooth.mwoolley.microbitbledemo;

/**
 * Created by Martin on 27/01/2016.
 */
public class MicroBitEvent {

    private short event_type;
    private short event_value;

    public MicroBitEvent(short event_type, short event_value) {
        this.event_type = event_type;
        this.event_value = event_value;
    }

    public MicroBitEvent(byte [] event_bytes) {
        byte[] event_type_bytes = new byte[2];
        byte[] event_value_bytes = new byte[2];
        System.arraycopy(event_bytes, 0, event_type_bytes, 0, 2);
        System.arraycopy(event_bytes, 2, event_value_bytes, 0, 2);
        event_type = Utility.shortFromLittleEndianBytes(event_type_bytes);
        event_value = Utility.shortFromLittleEndianBytes(event_value_bytes);
    }

    public short getEvent_type() {
        return event_type;
    }

    public void setEvent_type(short event_type) {
        this.event_type = event_type;
    }

    public short getEvent_value() {
        return event_value;
    }

    public void setEvent_value(short event_value) {
        this.event_value = event_value;
    }

    public byte [] getEventBytesForBle() {
        byte[] event_type_bytes = new byte[2];
        byte[] event_value_bytes = new byte[2];
        byte[] event_bytes = new byte[4];
        event_type_bytes = Utility.leBytesFromShort(event_type);
        event_value_bytes = Utility.leBytesFromShort(event_value);
        System.arraycopy(event_type_bytes, 0, event_bytes, 0, 2);
        System.arraycopy(event_value_bytes, 0, event_bytes, 2, 2);
        return event_bytes;
    }
}
