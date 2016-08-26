package org.wipro.svm.model;

import java.util.List;

public class CustomerDetails {
	
	private String userId;
	private String userName;
	private String referenceId;
	private String image;
	private String mobileNumber;
	private String emailAddress;
	private String address1;
	private String address2;
	private String address3;
	private String tempAddress;
	private String lastPurchaseDate;
	private String lastPuchaseItem;
	private String lastPurchaseLocation;
	private List<String> productPurchaseHistory;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getReferenceId() {
		return referenceId;
	}
	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public String getAddress1() {
		return address1;
	}
	public void setAddress1(String address1) {
		this.address1 = address1;
	}
	public String getAddress2() {
		return address2;
	}
	public void setAddress2(String address2) {
		this.address2 = address2;
	}
	public String getAddress3() {
		return address3;
	}
	public void setAddress3(String address3) {
		this.address3 = address3;
	}
	public String getTempAddress() {
		return tempAddress;
	}
	public void setTempAddress(String tempAddress) {
		this.tempAddress = tempAddress;
	}
	public String getLastPurchaseDate() {
		return lastPurchaseDate;
	}
	public void setLastPurchaseDate(String lastPurchaseDate) {
		this.lastPurchaseDate = lastPurchaseDate;
	}
	public String getLastPuchaseItem() {
		return lastPuchaseItem;
	}
	public void setLastPuchaseItem(String lastPuchaseItem) {
		this.lastPuchaseItem = lastPuchaseItem;
	}
	public String getLastPurchaseLocation() {
		return lastPurchaseLocation;
	}
	public void setLastPurchaseLocation(String lastPurchaseLocation) {
		this.lastPurchaseLocation = lastPurchaseLocation;
	}
	public List<String> getProductPurchaseHistory() {
		return productPurchaseHistory;
	}
	public void setProductPurchaseHistory(List<String> productPurchaseHistory) {
		this.productPurchaseHistory = productPurchaseHistory;
	}

}
