package org.wipro.svm.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;

import org.wipro.svm.service.PrintService;

public class PrintServiceImpl implements PrintService {
	static Enumeration portList;
	static CommPortIdentifier portId;
	static SerialPort serialPort;
	static OutputStream outputStream;

	public static String convertStringToHex(String printMessage) {
		char[] chars = printMessage.toCharArray();

		StringBuffer hex = new StringBuffer();
		for (int i = 0; i < chars.length; i++) {
			hex.append(Integer.toHexString((int) chars[i]));
		}

		return hex.toString();
	}

	/* (non-Javadoc)
	 * @see org.wipro.svm.service.impl.PrintService#convertHexToString(java.lang.String)
	 */
	@Override
	public String convertHexToString(String hex) {

		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();

		// 49204c6f7665204a617661 split into two characters 49, 20, 4c...
		for (int i = 0; i < hex.length() - 1; i += 2) {

			// grab the hex in pairs
			String output = hex.substring(i, (i + 2));
			// convert hex to decimal
			int decimal = Integer.parseInt(output, 16);
			// convert the decimal to character
			sb.append((char) decimal);

			temp.append(decimal);
		}
		// System.out.println("Decimal : " + temp.toString());

		return sb.toString();
	}

	public static void printText(String printMessage) {
		System.out.println("Inside printText()");
		
		portList = CommPortIdentifier.getPortIdentifiers();

		while (portList.hasMoreElements()) {
			System.out.println("Inside while loop");
			
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				System.out.println("Inside portId.getPortType() == CommPortIdentifier.PORT_SERIAL");
				
				if (portId.getName().equals("COM16")) {
					System.out.println("Inside COM16");
					
					try {
						serialPort = (SerialPort) portId.open("PrinterUtils", 2000);
					} catch (PortInUseException e) {
					}
					try {
						outputStream = serialPort.getOutputStream();
					} catch (IOException e) {
					}
					try {
						serialPort.setSerialPortParams(19200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
								SerialPort.PARITY_NONE);
					} catch (UnsupportedCommOperationException e) {
					}
					try {
//						String hexInitPrinter = "0x1B0x40";// ESC @
						String hexCRandLF = "0x1B0x0A0x1B0x0A"; // CR & LF
						// char[] hexCutPaper = new char[]{0x1d,'V',1};
						// String hexHello = "0x480x650x6c0x6c0x6f";
						byte ESC = 0x1B;
						byte[] cutPaper = { ESC, 0x69 };

						String hexHello = convertStringToHex(printMessage);
//						byte[] byteInitPrinter = hexStringToByteArray(hexInitPrinter);
						byte[] byteCRandLF = hexStringToByteArray(hexCRandLF);
						byte[] byteHello = hexStringToByteArray(hexHello);
						// byte[] byteCutPaper = {27, 100, 3};

						System.out.println("Before outputStream.write(byteHello)");
						
						outputStream.write(byteHello);
						Thread.sleep(100);
						outputStream.write(byteCRandLF);
						outputStream.write(byteCRandLF);
						outputStream.write(byteCRandLF);
						outputStream.write(cutPaper);
						
						System.out.println("After outputStream.write(cutPaper);");
						// outputStream.write(byteCutPaper);

						} catch (IOException | InterruptedException e) {
							System.out.println("Exception Occured: " + e.getMessage().toString());
					}
				}
			}
		}
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

}
