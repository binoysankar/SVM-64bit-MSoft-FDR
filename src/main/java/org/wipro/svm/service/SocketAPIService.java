package org.wipro.svm.service;

import java.net.Socket;

public interface SocketAPIService {

	void addProductToCart(Socket client, String productID, int dispenseQty);

	void dispenseProduct(Socket client);

	void getProductsForSale(Socket client);

	void getProductBalance(Socket client, String productID);

	void getProduct(Socket client, String productID);

}