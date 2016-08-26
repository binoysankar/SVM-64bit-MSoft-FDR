package org.wipro.svm.service.impl;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.commons.lang.RandomStringUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;
import org.wipro.svm.model.ImgNameAndFaceId;
import org.wipro.svm.service.FaceDetectionService;
import org.wipro.svm.utils.SvmConstants;

import com.github.sarxos.webcam.Webcam;

import javafx.scene.control.ProgressBar;

public class FaceDetectionServiceImpl implements FaceDetectionService {

	Properties properties;
	
	public FaceDetectionServiceImpl(Properties props) {
		this.properties = props;
	}
	
	/*@Override
	public String startFaceRecognition() throws Exception {
		String imageName = "";
		File capturedImage = imageCapture();
//		changeImageToGrayScale(capturedImage);
		boolean faceDetected = faceDetectAndCrop(capturedImage);
		
		// FOR TESTING - TO REMOVE
//		FileUtils.copyFileToDirectory(capturedImage, new File("C:/Vendron/SVM/images/tempFolder/"));
		// FOR TESTING - TO REMOVE
		
		if (faceDetected) {
			imageName = new FaceRecognitionServiceImpl().processImage(capturedImage, properties);
		}
		
		return imageName;
	}*/

	private File imageCapture() throws InterruptedException {
		File saveImage = null;
		try {
			Webcam webcam = Webcam.getDefault();
			webcam.setViewSize(new Dimension(640, 480));
			webcam.open();

			Thread.sleep(3000);
			String randomFileName = RandomStringUtils.randomAlphabetic(8);
			saveImage = new File(properties.getProperty("source.face.images.path") + randomFileName + SvmConstants.PNG_FILE_EXTENSION);

			ImageIO.write(webcam.getImage(), SvmConstants.PNG_FILE_TYPE, saveImage);
			webcam.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return saveImage;
	}

	private boolean faceDetectAndCrop(File capturedImage) {
		boolean faceDetected = false;

//		changeImageToGrayScale(capturedImage);

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		CascadeClassifier faceDetector = new CascadeClassifier(properties.getProperty("haarcascade.frontalface.path"));
		Mat image = Highgui.imread(capturedImage.getAbsolutePath());
		MatOfRect faceDetections = new MatOfRect();

		faceDetector.detectMultiScale(image, faceDetections);
//		System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));
		Rect rectCrop = null;

		if (faceDetections.toArray().length > 0) {
			for (Rect rect : faceDetections.toArray()) {
				rectCrop = new Rect(rect.x, rect.y, rect.width, rect.height);
			}

			Mat imageRoi = new Mat(image, rectCrop);
			Highgui.imwrite(capturedImage.getAbsolutePath(), imageRoi);
			faceDetected = true;
		}

		return faceDetected;
	}

	/*@Override
	public ImgNameAndFaceId startFaceRecognition() throws Exception {
		
		return null;
	}*/

	@Override
	public ImgNameAndFaceId startFaceRecognitionBetaFace() throws Exception {
		return null;
	}

	@Override
	public String startFaceRecognition(ProgressBar bar) throws Exception {
		return null;
	}

//	private static void changeImageToGrayScale(File file) {
//		BufferedImage orginalImage;
//		try {
//			orginalImage = ImageIO.read(file);
//			BufferedImage blackAndWhiteImg = new BufferedImage(orginalImage.getWidth(), orginalImage.getHeight(),
//					BufferedImage.TYPE_BYTE_GRAY);
//
//			Graphics2D graphics = blackAndWhiteImg.createGraphics();
//			graphics.drawImage(orginalImage, 0, 0, null);
//
//			ImageIO.write(blackAndWhiteImg, SvmConstants.PNG_FILE_TYPE, file);
//			blackAndWhiteImg.flush();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
}