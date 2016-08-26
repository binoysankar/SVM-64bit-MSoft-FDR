package org.wipro.svm.service.impl;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.commons.lang.RandomStringUtils;
import org.wipro.svm.model.ImageNameUID;
import org.wipro.svm.model.ImgNameAndFaceId;
import org.wipro.svm.model.RecognizeResult;
import org.wipro.svm.service.FaceDetectionService;
import org.wipro.svm.utils.Base64Util;
import org.wipro.svm.utils.SvmConstants;

import com.github.sarxos.webcam.Webcam;

import javafx.scene.control.ProgressBar;

public class FaceDetectionBetaFaceImpl implements FaceDetectionService {

	Properties properties;
	
	public FaceDetectionBetaFaceImpl(Properties props) {
		this.properties = props;
	}
	
	public ImgNameAndFaceId startFaceRecognitionBetaFace() throws Exception {
		ImgNameAndFaceId imgNameAndFaceId = new ImgNameAndFaceId();
		ImageNameUID imageNameUID = new ImageNameUID();
		FaceRecognitionBetaFaceImpl faceRecognitionImpl = new FaceRecognitionBetaFaceImpl(properties);
//		Map<String, String> sourceImgMap = new HashMap<>();
		String imgNameByte64DataStr = null;

/////////////////////////////////Delete a face///////////////////////////////////////////
//				String faceUid = "68546b26-36e1-11e6-a900-001c420bd602";
//				faceRecognitionImpl.deleteFace(faceUid);
/////////////////////////////////Delete a face///////////////////////////////////////////
		
		File capturedImage = imageCapture();
		imgNameAndFaceId.setCapturedImageName(capturedImage.getName().split("\\.")[0]);
		
		System.out.println("Sending Request: " + Calendar.getInstance().getTime());
		
//		convertSrcImagetoByteArr(new File("C:/Vendron/SVM/images/tempFolder/dcNdd.png"), sourceImgMap);
		imgNameByte64DataStr = convertSrcImagetoByteArr(capturedImage);

		if (!imgNameByte64DataStr.equals(SvmConstants.EMPTY_STRING) || imgNameByte64DataStr != null) {
			// Get src image UID
			String imgUid = faceRecognitionImpl.uploadCapturedImage(imgNameByte64DataStr, imageNameUID);

			// Get src image face UID
			imgNameAndFaceId.setFaceUid(faceRecognitionImpl.getFaceUId(imgUid, imageNameUID));

			if (imgNameAndFaceId.getFaceUid() != null
					&& !imgNameAndFaceId.getFaceUid().equals(SvmConstants.EMPTY_STRING)) {
				// Recognize faces
				String recognizeUid = faceRecognitionImpl.getrecognizeUid(imgNameAndFaceId.getFaceUid());

				// Get recognize result
				List<RecognizeResult> recognizeResultList = faceRecognitionImpl.getRecognizeResult(recognizeUid);

				// List of matching Image Names
				imgNameAndFaceId.setMatchingImgName(
						faceRecognitionImpl.getMatchingImageNameList(recognizeResultList, imageNameUID));
			}
		}
		
		return imgNameAndFaceId;
	}

	private File imageCapture() throws InterruptedException {
		File saveImage = null;

		try {
			Webcam webcam = Webcam.getDefault();
			webcam.setViewSize(new Dimension(640, 480));
			webcam.open();

			String randomFileName = RandomStringUtils.randomAlphabetic(5);
			saveImage = new File(
					properties.getProperty("source.face.images.path") + randomFileName + SvmConstants.PNG_FILE_EXTENSION);
			
			ImageIO.write(webcam.getImage(), SvmConstants.PNG_FILE_TYPE, saveImage);
			webcam.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return saveImage;
	}
	
	private String convertSrcImagetoByteArr(File capturedImage) {
		String imgNameByte64DataStr = null;
		String imageDataStr = null;
		BufferedImage buffImage;

		try {
			buffImage = ImageIO.read(capturedImage);
			if (buffImage != null) {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				ImageIO.write(buffImage, SvmConstants.PNG_FILE_TYPE, os);
				
				byte[] data = os.toByteArray();
				imageDataStr = Base64Util.encode(data);
				
				imgNameByte64DataStr = capturedImage.getName() + SvmConstants.COLON + imageDataStr;
//				sourceImgMap.put(capturedImage.getName(), imageDataStr);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return imgNameByte64DataStr;
	}

	@Override
	public String startFaceRecognition(ProgressBar bar) throws Exception {

		return null;
	}
	
}