package com.gunurung.musicfile;

public class ByteConverter {
    public static byte[] toLittleEndian_4(int src) { //32비트 4바이트
        byte littleEndian[] = new byte[4];
        littleEndian[3] = (byte)(src >>> 24);
        littleEndian[2] = (byte)((src >> 16) & 0xff);
        littleEndian[1] = (byte)((src >> 8)  & 0xff);
        littleEndian[0] = (byte)(src & 0xff);
        return littleEndian;
    }
    public static byte[] toLittleEndian_2(short src) { //16비트 2바이트
        byte littleEndian[] = new byte[2];
        littleEndian[1] = (byte)(src >>> 8);
        littleEndian[0] = (byte)(src & 0xff);
        return littleEndian;
    }

    public static int fromLittleEndian_4(byte[] buffer, int offset) {
        return (buffer[offset]            & 0xff)     |
                        ((buffer[offset + 1] << 8) & 0xff00)   |
                        ((buffer[offset + 2] << 16)& 0xff0000) |
                        ((buffer[offset + 3] << 24)& 0xff000000);


    }

    public static short fromLittleEndian_2(byte[] buf, int offset) {
        return (short)((buf[offset+1] << 8) | (buf[offset] & 0xff));
    }
}
