package org.wipro.svm.model;

public class ImgNameAndFaceId {

	private String matchingImgName = "";
	private String faceUid = "";
	private String capturedImageName = "";
	
	public String getCapturedImageName() {
		return capturedImageName;
	}
	public void setCapturedImageName(String capturedImageName) {
		this.capturedImageName = capturedImageName;
	}
	public String getMatchingImgName() {
		return matchingImgName;
	}
	public void setMatchingImgName(String matchingImgName) {
		this.matchingImgName = matchingImgName;
	}
	public String getFaceUid() {
		return faceUid;
	}
	public void setFaceUid(String faceUid) {
		this.faceUid = faceUid;
	}

}
