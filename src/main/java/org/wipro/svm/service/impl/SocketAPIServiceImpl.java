package org.wipro.svm.service.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import org.wipro.svm.service.SocketAPIService;
import org.wipro.svm.utils.SvmConstants;

public class SocketAPIServiceImpl implements SocketAPIService {

	@Override
	public void addProductToCart(Socket client, String productID, int dispenseQty) {

		PrintWriter output;
		try {
			output = new PrintWriter(client.getOutputStream(), true);
			String msg = SvmConstants.ADD_PRODUCT_TO_CART_ONE + productID + SvmConstants.ADD_PRODUCT_TO_CART_TWO + String.valueOf(dispenseQty) + SvmConstants.HASH;
			output.printf(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void dispenseProduct(Socket client) {
		PrintWriter output;
		try {
			output = new PrintWriter(client.getOutputStream(), true);
			String msg = SvmConstants.DISPENSE_PRODUCT;
			output.printf(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void getProductsForSale(Socket client) {
		PrintWriter output;
		try {
			output = new PrintWriter(client.getOutputStream(), true);
			String msg = SvmConstants.GET_PRODUCTS_FOR_SALE;
			output.printf(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void getProduct(Socket client, String productId) {
		PrintWriter output;
		try {
			output = new PrintWriter(client.getOutputStream(), true);
			String msg = SvmConstants.GET_PRODUCT + productId + SvmConstants.HASH;
			output.printf(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void getProductBalance(Socket client, String productId) {
		PrintWriter output;
		try {
			output = new PrintWriter(client.getOutputStream(), true);
			String msg = SvmConstants.GET_PRODUCT_BALANCE + productId + SvmConstants.HASH;
			output.printf(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startQRScan(Socket client) {
		PrintWriter output;
		try {
			output = new PrintWriter(client.getOutputStream(), true);
			String msg = SvmConstants.START_QR_SCAN;
			output.printf(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startQRScanCameraIndex(Socket client) {
		PrintWriter output;
		try {
			output = new PrintWriter(client.getOutputStream(), true);
			String msg = SvmConstants.START_QR_SCAN_CAMERA_INDEX;
			output.printf(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
