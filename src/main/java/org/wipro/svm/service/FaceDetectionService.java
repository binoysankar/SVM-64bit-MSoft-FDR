package org.wipro.svm.service;

import org.wipro.svm.model.ImgNameAndFaceId;

import javafx.scene.control.ProgressBar;

public interface FaceDetectionService {

//	BetaFace
	public abstract ImgNameAndFaceId startFaceRecognitionBetaFace() throws Exception;
	
//	Microsoft Cognitive Face
	public abstract String startFaceRecognition(ProgressBar bar) throws Exception;

}