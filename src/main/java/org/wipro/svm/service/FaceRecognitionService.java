package org.wipro.svm.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.wipro.svm.model.ImageNameUID;
import org.wipro.svm.model.RecognizeResult;

public interface FaceRecognitionService {

//	public abstract String processImage(File reference, Properties properties) throws IOException;
	
	public abstract String uploadCapturedImage(String imgNameByte64DataStr, ImageNameUID imageNameUIDs) throws JSONException, IOException;
	
	public abstract String getFaceUId(String imgUid, ImageNameUID imageNameUID) throws JSONException, IOException;
	
	public abstract String getrecognizeUid(String faceUid) throws IOException;
	
	public abstract List<RecognizeResult> getRecognizeResult(String recognizeUid) throws IOException;
	
	public abstract String getMatchingImageNameList(List<RecognizeResult> recognizeResultList, ImageNameUID imageNameUIDs);

}