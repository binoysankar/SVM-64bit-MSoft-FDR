package org.wipro.svm.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MicrosoftCognitiveSvcHelperUtil {
	public byte[] convertImgToByteArray(File capturedImage) {
		byte[] imageInByte = null;
		
		try {
//			capturedImage = new File("C:/Vendron/SVM/images/gallery/ionlq.png");
//			capturedImage = new File("C:/Vendron/SVM/images/tempFolder/arun1.png");
			BufferedImage originalImage = ImageIO.read(capturedImage);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(originalImage, "png", baos);
			imageInByte = baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return imageInByte;
	}
	
	public CloseableHttpClient getHttpClient(HttpHost proxy, Properties props) {
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(
				props.getProperty("http.proxyUsername"), props.getProperty("http.proxyPassword")));
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();

		return httpClient;
	}
	
	public String parseJsonEntityGetFaceId(HttpEntity entity) throws ParseException, IOException {
		String faceId = null;
		
		try {
			String entityStr = EntityUtils.toString(entity);
			JSONArray arr = new JSONArray(entityStr);
			for (int i = 0; i < arr.length(); i++) {
				faceId = arr.getJSONObject(i).getString("faceId");
				break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return faceId;
	}
	
	public String parseJsonEntityGetpersistedFaceId(HttpEntity entity) throws ParseException, IOException {
		String persistedFaceId = null;
		
		try {
			String entityStr = EntityUtils.toString(entity);
			String startChar = String.valueOf(entityStr.charAt(0));
			
			if (startChar.startsWith("[")) {
				JSONArray arr = new JSONArray(entityStr);
				for (int i = 0; i < arr.length(); i++) {
					double confidence = Double.parseDouble(arr.getJSONObject(i).getString("confidence"));
					System.out.println("Confidence: " + confidence);
//					if (confidence >= 0.50) {
					persistedFaceId = arr.getJSONObject(i).getString("persistedFaceId");
//					}
					break;
				}
			} else {
				JSONObject obj = new JSONObject(entityStr);
				if (!obj.getString("error").isEmpty() || obj.getString("error") != null) {
//					System.out.println("Error");
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return persistedFaceId;
	}
	
	public String parseJsonEntityGetUserData(HttpEntity entity, String persistedFaceId) throws ParseException, IOException {
		String userName = "";
		
		try {
			JSONObject obj = new JSONObject(EntityUtils.toString(entity));
			String persistedFaces = obj.getString("persistedFaces");
			
			JSONArray arr = new JSONArray(persistedFaces);
			for (int i = 0; i < arr.length(); i++) {
				if (persistedFaceId.equals(arr.getJSONObject(i).getString("persistedFaceId"))) {
					userName = arr.getJSONObject(i).getString("userData");
					break;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return userName;
	}

}
