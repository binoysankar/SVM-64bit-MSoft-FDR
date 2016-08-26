package org.wipro.svm.service.impl;

import java.io.File;
import java.net.URI;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.wipro.svm.utils.MicrosoftCognitiveSvcHelperUtil;
import org.wipro.svm.utils.SvmConstants;

public class FaceRecognitionMSoftSvcImpl {
	MicrosoftCognitiveSvcHelperUtil helpUtil = new MicrosoftCognitiveSvcHelperUtil();
	HttpHost proxy;
	Properties props;
	
	public FaceRecognitionMSoftSvcImpl(Properties props, HttpHost proxy) {
		this.props = props;
		this.proxy = proxy;
	}
	
	public String findSimilarFaces(String faceId) {
		String persistedFaceId = null;
		String url = "https://api.projectoxford.ai/face/v1.0/findsimilars";
		CloseableHttpClient httpClient;
		CloseableHttpResponse response;
		
		try {
			String requestJson = "{\n"+
					" \"faceId\":\"" + faceId + "\", \n"+
					" \"faceListId\":\"" + props.getProperty("face.id.list") + "\",\n"+
					" \"maxNumOfCandidatesReturned\":1 \n"+
					"}";
			
			if (proxy != null) {
				httpClient = helpUtil.getHttpClient(proxy, props);
			} else {
				httpClient = HttpClients.custom().build();
			}

			URIBuilder builder = new URIBuilder(url);
			URI uri = builder.build();
			HttpPost request = new HttpPost(uri);
			request.setHeader("Content-Type", SvmConstants.CONTENT_TYPE_JSON);
			request.setHeader("Ocp-Apim-Subscription-Key", props.getProperty("Ocp-Apim-Subscription-Key"));

			if (proxy != null) {
				RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
				request.setConfig(config);
			}
			
			// Request body
			StringEntity reqEntity = new StringEntity(requestJson);
			request.setEntity(reqEntity);

			if (proxy != null) { 
				response = httpClient.execute(proxy, request);
			} else {
				response = httpClient.execute(request);
			}

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				persistedFaceId = helpUtil.parseJsonEntityGetpersistedFaceId(entity);
			}
		} catch (Exception e) {}
		
		return persistedFaceId;
		
	}
	
	public void addNewUserFaceToFaceList(File capturedImage) {
		String url = "https://api.projectoxford.ai/face/v1.0/facelists/" + props.getProperty("face.id.list") + "/persistedFaces";
		CloseableHttpClient httpClient;
		
		try {
			if (proxy != null) {
				httpClient = helpUtil.getHttpClient(proxy, props);
			} else {
				httpClient = HttpClients.custom().build();
			}

			byte[] imageInByte = helpUtil.convertImgToByteArray(capturedImage);

			URIBuilder builder = new URIBuilder(url);
			builder.setParameter("userData", capturedImage.getName().split("\\.")[0]);
//            builder.setParameter("targetFace", "{string}");

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
				httpClient.execute(proxy, request);
			} else {
				httpClient.execute(request);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public String getUserDatafromFaceList(String persistedFaceId) {
		String userName = "";
		String url = "https://api.projectoxford.ai/face/v1.0/facelists/" + props.getProperty("face.id.list") + "";
		CloseableHttpClient httpClient;
		CloseableHttpResponse response;
		
		try {
			if (proxy != null) {
				httpClient = helpUtil.getHttpClient(proxy, props);
			} else {
				httpClient = HttpClients.custom().build();
			}
			
			URIBuilder builder = new URIBuilder(url);
			URI uri = builder.build();
			HttpGet request = new HttpGet(uri);
			request.setHeader("Ocp-Apim-Subscription-Key", props.getProperty("Ocp-Apim-Subscription-Key"));

			if (proxy != null) {
				RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
				request.setConfig(config);
			}

			if (proxy != null) { 
				response = httpClient.execute(proxy, request);
			} else {
				response = httpClient.execute(request);
			}

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				userName = helpUtil.parseJsonEntityGetUserData(entity, persistedFaceId);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		return userName;
	}
	
}
