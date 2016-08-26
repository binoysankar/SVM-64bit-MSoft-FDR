package org.wipro.svm.service.impl;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.wipro.svm.model.ImgNameAndFaceId;
import org.wipro.svm.service.FaceDetectionService;
import org.wipro.svm.utils.MicrosoftCognitiveSvcHelperUtil;
import org.wipro.svm.utils.SvmConstants;
import org.wipro.svm.view.GreetingsLayout;

import com.github.sarxos.webcam.Webcam;
import com.ibm.icu.util.Calendar;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

public class FaceDetectionMSoftSvcImpl implements FaceDetectionService {
	private File capturedImage;
	Properties props = new Properties();
	HttpHost proxy;

	public File getCapturedImage() {
		return capturedImage;
	}
	public void setCapturedImage(File capturedImage) {
		this.capturedImage = capturedImage;
	}

	public FaceDetectionMSoftSvcImpl() {}
	
	public FaceDetectionMSoftSvcImpl(Properties props, HttpHost proxy) {
		this.props = props;
		this.proxy = proxy;
	}

	/*public static void main(String[] args) {
		try {
			String matchingImgName = new FaceDetectionMSoftSvcImpl().startFaceRecognition();
			System.out.println("Matching Image Name: " + matchingImgName);
			
			System.out.println("Finish: " + Calendar.getInstance().getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	public String startFaceRecognition(ProgressBar bar) throws Exception {
		System.out.println("Send Request: " + Calendar.getInstance().getTime());
		
		String matchingImgName = SvmConstants.EMPTY_STRING;
		FaceRecognitionMSoftSvcImpl faceRecognitionUtil = new FaceRecognitionMSoftSvcImpl(props, proxy);

		setCapturedImage(imageCapture());
		Platform.runLater(new Runnable() {
			public void run() {
					bar.setProgress(1 * 0.25);
			}
		});
		
//		capturedImage = imageCapture();
		
//		System.out.println("Stop Camera: " + Calendar.getInstance().getTime());
//		File capturedImage = new File("C:/Vendron/SVM/images/tempFolder/binoy6.png");

		String faceId = uploadImgGetFaceId();
		Platform.runLater(new Runnable() {
			public void run() {
				bar.setProgress(2 * 0.25);
			}
		});
		
		String persistedFaceId = faceRecognitionUtil.findSimilarFaces(faceId);
		Platform.runLater(new Runnable() {
			public void run() {
				bar.setProgress(3 * 0.25);
			}
		});
		
		if (persistedFaceId == null) {// if null - Unknown User
			
		} else {// User
			// Get userdata from a face list
//			matchingImgName = faceRecognitionUtil.getUserDatafromFaceList(persistedFaceId);
			matchingImgName = "John";
		}

		return matchingImgName;
	}
	
	public String uploadImgGetFaceId() {
		MicrosoftCognitiveSvcHelperUtil helpUtil = new MicrosoftCognitiveSvcHelperUtil();
		String faceId = null;
		String url = "https://api.projectoxford.ai/face/v1.0/detect";
		CloseableHttpClient httpClient;
		CloseableHttpResponse response;
		
		try {
			byte[] imageInByte = helpUtil.convertImgToByteArray(getCapturedImage());
			
			if (proxy != null) {
				httpClient = helpUtil.getHttpClient(proxy, props);
			} else {
				httpClient = HttpClients.custom().build();
			}
			
			URIBuilder builder = new URIBuilder(url);
			builder.setParameter("returnFaceId", "true");
			// builder.setParameter("returnFaceLandmarks", "false");
			// builder.setParameter("returnFaceAttributes", "age,gender");

			URI uri = builder.build();
			HttpPost request = new HttpPost(uri);
			request.setHeader("Content-Type", SvmConstants.CONTENT_TYPE_OCTET_STREAM);
			request.setHeader("Ocp-Apim-Subscription-Key", props.getProperty("Ocp-Apim-Subscription-Key"));

			if (proxy != null) { 
				RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
				request.setConfig(config);
			}

			// Request body
			ByteArrayEntity reqEntity = new ByteArrayEntity(imageInByte);
			request.setEntity(reqEntity);

			if (proxy != null) { 
				response = httpClient.execute(proxy, request);
			} else {
				response = httpClient.execute(request);
			}
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				faceId = helpUtil.parseJsonEntityGetFaceId(entity);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		return faceId;
	}

	private File imageCapture() throws InterruptedException {
		File saveImage = null;
		try {
			Webcam webcam = Webcam.getDefault();
			webcam.setViewSize(new Dimension(640, 480));
			webcam.open();

			String randomFileName = RandomStringUtils.randomAlphabetic(5);
			saveImage = new File("C:/Vendron/SVM/images/gallery/" + randomFileName + ".png");
			// saveImage = new File("C:/Vendron/SVM/images/gallery/ppwcshdd.png"); // TO REMOVE

			ImageIO.write(webcam.getImage(), "PNG", saveImage);
			webcam.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return saveImage;
	}

	@Override
	public ImgNameAndFaceId startFaceRecognitionBetaFace() throws Exception {

		return null;
	}

}