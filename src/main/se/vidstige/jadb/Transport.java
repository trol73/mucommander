package se.vidstige.jadb;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

class Transport {
	private static final boolean DEBUG = true;

	private final OutputStream outputStream;
	private final InputStream inputStream;


	private Transport(OutputStream outputStream, InputStream inputStream) {
		this.outputStream = outputStream;
		this.inputStream = inputStream;
	}

	public Transport(Socket socket) throws IOException {
		this(socket.getOutputStream(), socket.getInputStream());
	}

	public String readString() throws IOException {
		String encodedLength = readString(4);
		int length = Integer.parseInt(encodedLength, 16); 		
		return readString(length);
	}
	
	public void verifyResponse() throws IOException, JadbException  {
		String response = readString(4);
		if (!"OKAY".equals(response)) {
			String error;
			try {
				error = readString();
			} catch (Exception e) {
				error = response;
			}
            throw new JadbException("command failed: " + error);
        }
	}

	public String readString(int length) throws IOException {
		DataInput reader = new DataInputStream(inputStream);
		byte[] responseBuffer = new byte[length];		
		reader.readFully(responseBuffer);
        return new String(responseBuffer, Charset.forName("utf-8"));
	}

	public String getCommandLength(String command) {
		return String.format("%04x", command.length());
	}
	
	public void send(String command) throws IOException {
		if (DEBUG) {
			log("command " + command);
		}
		OutputStreamWriter writer = new OutputStreamWriter(outputStream);
		// TODO UTF-8
		writer.write(getCommandLength(command));
		writer.write(command);
		writer.flush();
	}

    public SyncTransport startSync() throws IOException, JadbException {
        send("sync:");
        verifyResponse();
        return new SyncTransport(outputStream, inputStream);
    }

    public void close() throws IOException {
        inputStream.close();
        outputStream.close();
    }

	private static void log(String s) {
		System.out.println("ADB::" + s);
	}
}
