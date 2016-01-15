package se.vidstige.jadb;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by vidstige on 2014-03-19.
 */
public class SyncTransport {

    private static final boolean DEBUG = false;

    private final DataOutput output;
    private final DataInput input;

    public SyncTransport(OutputStream outputStream, InputStream inputStream) {
        output = new DataOutputStream(outputStream);
        input = new DataInputStream(inputStream);
    }

    public SyncTransport(DataOutput outputStream, DataInput inputStream) {
        output = outputStream;
        input = inputStream;
    }
    public void send(String syncCommand, String name) throws IOException {
        if (DEBUG) {
            log("send " + syncCommand + " (" + name + ")");
            log("raw", name.getBytes());
        }
        if (syncCommand.length() != 4) {
            throw new IllegalArgumentException("sync commands must have length 4");
        }
        output.writeBytes(syncCommand);

//        output.writeInt(Integer.reverseBytes(name.length()));
//        output.writeBytes(name);
        byte[] data = name.getBytes("utf-8");
        output.writeInt(Integer.reverseBytes(data.length));
        output.write(data);
    }

    public void sendStatus(String statusCode, int length) throws IOException {
        if (DEBUG) {
            log("sendStatus " + statusCode + " (" + length + ")");
        }
        output.writeBytes(statusCode);
        output.writeInt(Integer.reverseBytes(length));
    }

    public void verifyStatus() throws IOException, JadbException {
        String status = readString(4);
        int length = readInt();
        if ("FAIL".equals(status)) {
            String error = readString(length);
            if (DEBUG) {
                log("verifyStatusError " + error);
            }
            throw new JadbException(error);
        }
        if (!"OKAY".equals(status)) {
            throw new JadbException("Unknown error: " + status);
        }
        if (DEBUG) {
            log("verifyStatus OK");
        }
    }

    public int readInt() throws IOException {
        return Integer.reverseBytes(input.readInt());
    }

    public String readString(int length) throws IOException {
        byte[] buffer = new byte[length];
        input.readFully(buffer);
        if (DEBUG) {
//            log("readString -> " + new String(buffer, Charset.forName("utf-8")));
        }
        return new String(buffer, Charset.forName("utf-8"));
    }

    public RemoteFileRecord readDirectoryEntry() throws IOException {
        String id = readString(4);
        int mode = readInt();
        int size = readInt();
        int time = readInt();
        int nameLength = readInt();
        String name = readString(nameLength);

        if (!"DENT".equals(id)) {
            return RemoteFileRecord.DONE;
        }
        return new RemoteFileRecord(name, mode, size, time);
    }

    private void sendChunk(byte[] buffer, int offset, int length) throws IOException {
        output.writeBytes("DATA");
        output.writeInt(Integer.reverseBytes(length));
        output.write(buffer, offset, length);
    }

    private int readChunk(byte[] buffer) throws IOException, JadbException {
        String id = readString(4);
        int n = readInt();
        if ("FAIL".equals(id)) {
            throw new JadbException(readString(n));
        }
        if (!"DATA".equals(id)) {
            return -1;
        }
        if (DEBUG) {
            log("readChunk -> " + n);
        }
        input.readFully(buffer, 0, n);
        return n;
    }

    public void sendStream(InputStream in) throws IOException {
        byte[] buffer = new byte[1024 * 64];
        int n = in.read(buffer);
        while (n != -1) {
            sendChunk(buffer, 0, n);
            n = in.read(buffer);
        }
    }

    public void readChunksTo(OutputStream stream) throws IOException, JadbException {
        byte[] buffer = new byte[1024 * 64];
        int n = readChunk(buffer);
        while (n != -1) {
            stream.write(buffer, 0, n);
            n = readChunk(buffer);
        }
    }

    private static void log(String s) {
        System.out.println("ADB::" + s);
    }

    private static void log(String s, byte[] data) {
        System.out.println("ADB::" + s + " [" + bytesToHex(data) + "]");
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }
}
