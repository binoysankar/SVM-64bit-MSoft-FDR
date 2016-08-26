package org.wipro.svm.model;

public class RecognizeResult {

	private String confidence;
	private String faceUid;
	private boolean match;
	private String imgName;
	
	public String getImgName() {
		return imgName;
	}
	public void setImgName(String imgName) {
		this.imgName = imgName;
	}
	public String getConfidence() {
		return confidence;
	}
	public void setConfidence(String confidence) {
		this.confidence = confidence;
	}
	public String getFaceUid() {
		return faceUid;
	}
	public void setFaceUid(String faceUid) {
		this.faceUid = faceUid;
	}
	public boolean isMatch() {
		return match;
	}
	public void setMatch(boolean match) {
		this.match = match;
	}
}
