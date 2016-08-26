package org.wipro.svm.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier; 
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent; 
import gnu.io.SerialPortEventListener; 
import java.util.Enumeration;


public class PrintServiceRXTXImpl implements SerialPortEventListener {
	SerialPort serialPort;
        /** The port we're normally going to use. */
	private static final String PORT_NAME = "COM16";
	/**
	* A BufferedReader which will be fed by a InputStreamReader 
	* converting the bytes into characters 
	* making the displayed results codepage independent
	*/
	private BufferedReader inputStream;
	/** The output stream to the port */
	private OutputStream outputStream;
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 19200;

	public void printText(String printMessage) {
                // the next line is for Raspberry Pi and 
                // gets us into the while loop and was suggested here was suggested http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
//                System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");

		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		//First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
				if (currPortId.getName().equals(PORT_NAME)) {
					portId = currPortId;
					break;
				}
		}
		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			inputStream = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			outputStream = serialPort.getOutputStream();
//				String hexInitPrinter = "0x1B0x40";// ESC @
				String hexCRandLF = "0x1B0x0A0x1B0x0A"; // CR & LF
				// char[] hexCutPaper = new char[]{0x1d,'V',1};
				// String hexHello = "0x480x650x6c0x6c0x6f";
				byte ESC = 0x1B;
				byte[] cutPaper = { ESC, 0x69 };

				String hexHello = convertStringToHex(printMessage);
//				byte[] byteInitPrinter = hexStringToByteArray(hexInitPrinter);
				byte[] byteCRandLF = hexStringToByteArray(hexCRandLF);
				byte[] byteHello = hexStringToByteArray(hexHello);
				// byte[] byteCutPaper = {27, 100, 3};

				outputStream.write(byteHello);
				Thread.sleep(100);
				outputStream.write(byteCRandLF);
				outputStream.write(byteCRandLF);
				outputStream.write(byteCRandLF);
				outputStream.write(cutPaper);
				// outputStream.write(byteCutPaper);
			
			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			System.err.println(e.toString());
		} finally {
			disconnect();
		}
	}
	
	public static String convertStringToHex(String printMessage) {
		char[] chars = printMessage.toCharArray();

		StringBuffer hex = new StringBuffer();
		for (int i = 0; i < chars.length; i++) {
			hex.append(Integer.toHexString((int) chars[i]));
		}

		return hex.toString();
	}
	
	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				String inputLine=inputStream.readLine();
				System.out.println(inputLine);
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}
	
	public void disconnect() {
	    if (serialPort != null) {
	        try {
	            // close the i/o streams.
	        	outputStream.close();
	        	inputStream.close();
	        } catch (IOException ex) {
	        	// Close the port.
	        	serialPort.close();
	        }
	        // Close the port.
	        serialPort.close();
	    }
	}

	public static void main(String[] args) throws Exception {
		PrintServiceRXTXImpl main = new PrintServiceRXTXImpl();
		main.printText("Hello World!");
		/*Thread t = new Thread() {
			public void run() {
				//the following line will keep this app alive for 1000 seconds,
				//waiting for events to occur and responding to them (printing incoming messages to console).
				try {
					Thread.sleep(1000000);
					} catch (InterruptedException ie) {}
			}
		};
		t.start();
		System.out.println("Started");*/
	}
}
