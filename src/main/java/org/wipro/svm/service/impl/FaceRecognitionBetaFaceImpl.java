package org.wipro.svm.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wipro.svm.model.ImageNameUID;
import org.wipro.svm.model.ImgNameAndFaceId;
import org.wipro.svm.model.RecognizeResult;
import org.wipro.svm.service.FaceRecognitionService;
import org.wipro.svm.utils.SvmConstants;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class FaceRecognitionBetaFaceImpl implements FaceRecognitionService {
	
	Properties properties;
	HttpHost proxy;
	
	public FaceRecognitionBetaFaceImpl(Properties props) {
		this.properties = props;
		proxy = new HttpHost(properties.getProperty("http.proxyHost"),
				Integer.parseInt(properties.getProperty("http.proxyPort")));
	}

	@Override
	public String uploadCapturedImage(String imgNameByte64DataStr, ImageNameUID imageNameUID) throws JSONException {
		String url = SvmConstants.BETAFACE_UPLOAD_NEW_IMAGE;
		String imgUid = SvmConstants.EMPTY_STRING;
		String responseBody = null;
		String byte64ImgNameStr = SvmConstants.EMPTY_STRING;

		try {
			// Get byte64Str and image name
			String requestXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<ImageRequestBinary xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
					+ "<api_key>" + properties.getProperty("betaface.api.key") + "</api_key>\n" + "<api_secret>"
					+ properties.getProperty("betaface.api.secret") + "</api_secret>\n"
					+ "<detection_flags>bestface</detection_flags>\n" + "<imagefile_data>"
					+ imgNameByte64DataStr.split("\\:")[1] + "</imagefile_data>\n" + "<original_filename>"
					+ imgNameByte64DataStr.split("\\:")[0] + "</original_filename>\n</ImageRequestBinary>";

			responseBody = sendWebServiceRequest(requestXml, url);

			// Parse the response xml string
			imgUid = parseBetaFaceImageResponseXMLString(responseBody);

			// Create imageName & imageUID List
			imageNameUID.setImageName(byte64ImgNameStr.split("\\:")[0]);
			imageNameUID.setImageUid(imgUid);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return imgUid;
	}

	@Override
	public String getFaceUId(String imgUid, ImageNameUID imageNameUID) throws JSONException, IOException {
		String url = SvmConstants.BETAFACE_GET_IMAGE_INFO;
		String requestXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
				+ "<ImageInfoRequestUid xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
				+ " <api_key>" + properties.getProperty("betaface.api.key") + "</api_key>\n" + " <api_secret>"
				+ properties.getProperty("betaface.api.secret") + "</api_secret>\n" + " <img_uid>" + imgUid
				+ "</img_uid>\n" + "</ImageInfoRequestUid>";
		String faceUid = SvmConstants.EMPTY_STRING;
		String responseBody = null;
		int intResponse = 1;

		try {
			while (intResponse != 0) {
				responseBody = sendWebServiceRequest(requestXml, url);

				// Check the int_response value
				intResponse = checkIntResponseValue(responseBody);
				if (intResponse == 1) {
					Thread.sleep(500);
				} else if (intResponse == 0) {
					// Parse the response xml string
					faceUid = parseFaceRequestIdXMLString(responseBody);
					if (!faceUid.equals(SvmConstants.EMPTY_STRING)) {
						UpdateImageNameUIDWithFaceUID(imgUid, faceUid, imageNameUID);
					}
				}
			}
			
			// reset value
			intResponse = 1;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return faceUid;
	}

	@Override
	public String getrecognizeUid(String faceUid) throws IOException {
		String url = SvmConstants.BETAFACE_RECOGNIZE_FACES;
		String recognizeUid = SvmConstants.EMPTY_STRING;
		String responseBody = null;

		try {
			String requestXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
					+ "<RecognizeFacesRequest xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
					+ " <api_key>" + properties.getProperty("betaface.api.key") + "</api_key>\n" + " <api_secret>"
					+ properties.getProperty("betaface.api.secret") + "</api_secret>\n" + " <faces_uids>" + faceUid
					+ "</faces_uids>\n" + " <parameters></parameters>\n" + " <targets>all@wiprosvm.com</targets>\n"
					+ "</RecognizeFacesRequest>";

			responseBody = sendWebServiceRequest(requestXml, url);
			
			// Parse the response xml string
			recognizeUid = parseBetaFaceRecognizeFacesResponseXMLString(responseBody);
		} catch (IOException e) {
			throw e;
		}

		return recognizeUid;
	}

	@Override
	public List<RecognizeResult> getRecognizeResult(String recognizeUid) throws IOException {
		String url = SvmConstants.BETAFACE_GET_RECOGNIZE_RESULT;
		String requestXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
				+ "<RecognizeResultRequest xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
				+ " <api_key>" + properties.getProperty("betaface.api.key") + "</api_key>\n" + " <api_secret>"
				+ properties.getProperty("betaface.api.secret") + "</api_secret>\n" + " <recognize_uid>" + recognizeUid
				+ "</recognize_uid>\n" + "</RecognizeResultRequest>";
		List<RecognizeResult> recognizeResultList = new ArrayList<>();
		String responseBody = null;
		int intResponse = 1;

		try {
			while (intResponse != 0) {
				responseBody = sendWebServiceRequest(requestXml, url);

				// Check the int_response value
				intResponse = checkIntResponseValue(responseBody);
				if (intResponse == 1) {
					Thread.sleep(500);
				} else if (intResponse == 0) {
					// Parse the response xml string
					recognizeResultList = parseBetaFaceRecognizeInfoResponseXMLString(responseBody);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return recognizeResultList;
	}

	public void setPersonName(ImgNameAndFaceId imgNameAndFaceId) {
		try {
			String url = SvmConstants.BETAFACE_SET_PERSON;
			if (imgNameAndFaceId != null && !imgNameAndFaceId.getFaceUid().equals(SvmConstants.EMPTY_STRING)
					&& !imgNameAndFaceId.getCapturedImageName().equals(SvmConstants.EMPTY_STRING)) {
				String requestXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
						+ "<SetPersonRequest xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
						+ " <api_key>" + properties.getProperty("betaface.api.key") + "</api_key>\n" + " <api_secret>"
						+ properties.getProperty("betaface.api.secret") + "</api_secret>\n" + " <faces_uids>"
						+ imgNameAndFaceId.getFaceUid() + "</faces_uids>\n" + " <person_id>"
						+ imgNameAndFaceId.getCapturedImageName() + "@wiprosvm.com" + "</person_id>\n"
						+ "</SetPersonRequest>";
				
				sendWebServiceRequest(requestXml, url);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void deleteFace(String faceUid) {
		try {
			String url = SvmConstants.BETAFACE_DELETE_FACE;
			String requestXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
					+ "<FaceRequestId xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
					+ " <api_key>" + properties.getProperty("betaface.api.key") + "</api_key>\n" + " <api_secret>"
					+ properties.getProperty("betaface.api.secret") + "</api_secret>\n" + " <face_uid>" + faceUid
					+ "</face_uid>\n" + "</FaceRequestId>";

			sendWebServiceRequest(requestXml, url);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String sendWebServiceRequest(String requestXml, String url) throws IOException {
		String responseBody = null;
		HttpPost httpPost = null;
		
		try {
			CloseableHttpClient httpClient = getHttpClient();

			httpPost = new HttpPost(url);
			httpPost.setHeader(SvmConstants.CONTENT_TYPE, SvmConstants.APPLICATION_XML);

			RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
			httpPost.setConfig(config);

			StringEntity xmlEntity = new StringEntity(requestXml);
			httpPost.setEntity(xmlEntity);

			CloseableHttpResponse response = httpClient.execute(proxy, httpPost);

			StatusLine statusLine = response.getStatusLine();
			if (statusLine == null) {
				throw new IOException(String.format("Unable to get a response from OWM server"));
			}
			
			int statusCode = statusLine.getStatusCode();
			if (statusCode < 200 && statusCode >= 300) {
				throw new IOException(
						String.format("OWM server responded with status code %d: %s", statusCode, statusLine));
			}

			// Read the response content
			HttpEntity responseEntity = response.getEntity();
			responseBody = EntityUtils.toString(responseEntity, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return responseBody;
	}
	
	private CloseableHttpClient getHttpClient() {
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(proxy),
				new UsernamePasswordCredentials(properties.getProperty("http.proxyUsername"), properties.getProperty("http.proxyPassword")));
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider)
				.build();
		
//		DefaultProxyRoutePlanner  routePlanner = new DefaultProxyRoutePlanner(proxy);
//		CloseableHttpClient httpClient = HttpClients.custom()
//		    .setRoutePlanner(routePlanner)
//		    .build();
		
		return httpClient;
	}
	
	private int checkIntResponseValue(String responseMessage) {
		int intResponse = 1;
		DocumentBuilder db;
		String tagName1 = "BetafaceImageInfoResponse";
		String tagName2 = "BetafaceRecognizeResponse";
		NodeList nodes = null;

		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(responseMessage));

			Document doc = db.parse(is);
			if (doc.getElementsByTagName(tagName1).getLength() > 0) {
				nodes = doc.getElementsByTagName(tagName1);
			} else if (doc.getElementsByTagName(tagName2).getLength() > 0) {
				nodes = doc.getElementsByTagName(tagName2);
			}

			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);

				NodeList imgUid = element.getElementsByTagName("int_response");
				Element line = (Element) imgUid.item(0);
				intResponse = Integer.parseInt(getCharacterDataFromElement(line));
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}

		return intResponse;
	}

	private String parseBetaFaceImageResponseXMLString(String responseMessage) {
		String imgUidStr = SvmConstants.EMPTY_STRING;
		DocumentBuilder db;
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(responseMessage));

			Document doc = db.parse(is);
			NodeList nodes = doc.getElementsByTagName("BetafaceImageResponse");

			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);

				NodeList imgUid = element.getElementsByTagName("img_uid");
				Element line = (Element) imgUid.item(0);
				imgUidStr = getCharacterDataFromElement(line);
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}

		return imgUidStr;
	}

	private String parseBetaFaceRecognizeFacesResponseXMLString(String responseMessage) {
		String recognizeUidStr = "";
		DocumentBuilder db;
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(responseMessage));

			Document doc = db.parse(is);
			NodeList nodes = doc.getElementsByTagName("BetafaceRecognizeRequestResponse");

			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);

				NodeList recognizeUid = element.getElementsByTagName("recognize_uid");
				Element line = (Element) recognizeUid.item(0);
				recognizeUidStr = getCharacterDataFromElement(line);
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}

		return recognizeUidStr;
	}

	private String parseFaceRequestIdXMLString(String responseMessage) {
		String faceUidStr = "";
		DocumentBuilder db;
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(responseMessage));

			Document doc = db.parse(is);
			NodeList nodes = doc.getElementsByTagName("FaceInfo");

			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);

				NodeList faceUid = element.getElementsByTagName("uid");
				Element line = (Element) faceUid.item(0);
				faceUidStr = getCharacterDataFromElement(line);
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}

		return faceUidStr;
	}

	private List<RecognizeResult> parseBetaFaceRecognizeInfoResponseXMLString(String responseMessage) {
		String confidenceStr = "";
		String faceUidStr = "";
		String personNameStr = "";
		boolean isMatchBool = false;
		DocumentBuilder db;
		List<RecognizeResult> recognizeResultList = new ArrayList<>();

		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(responseMessage));

			Document doc = db.parse(is);
			NodeList mainNode = doc.getElementsByTagName("FaceRecognizeInfo");

			for (int i = 0; i < mainNode.getLength(); i++) {
				NodeList childNodes = doc.getElementsByTagName("PersonMatchInfo");

				for (int j = 0; j < childNodes.getLength(); j++) {
					RecognizeResult recognizeResult = new RecognizeResult();

					Element element = (Element) childNodes.item(j);

					NodeList confidence = element.getElementsByTagName("confidence");
					Element confidenceEle = (Element) confidence.item(0);
					confidenceStr = getCharacterDataFromElement(confidenceEle);
					recognizeResult.setConfidence(confidenceStr);

					NodeList faceUid = element.getElementsByTagName("face_uid");
					Element faceUidEle = (Element) faceUid.item(0);
					faceUidStr = getCharacterDataFromElement(faceUidEle);
					recognizeResult.setFaceUid(faceUidStr);

					NodeList isMatch = element.getElementsByTagName("is_match");
					Element isMatchEle = (Element) isMatch.item(0);
					isMatchBool = Boolean.parseBoolean(getCharacterDataFromElement(isMatchEle));
					recognizeResult.setMatch(isMatchBool);

					NodeList personName = element.getElementsByTagName("person_name");
					Element personNameEle = (Element) personName.item(0);
					personNameStr = getCharacterDataFromElement(personNameEle);
					recognizeResult.setImgName(personNameStr);
					
					recognizeResultList.add(recognizeResult);
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}

		return recognizeResultList;
	}

	private String getCharacterDataFromElement(Element e) {
		Node child = e.getFirstChild();
		if (child instanceof org.w3c.dom.CharacterData) {
			org.w3c.dom.CharacterData cd = (org.w3c.dom.CharacterData) child;
			return cd.getData();
		}

		return SvmConstants.EMPTY_STRING;
	}

	@Override
	public String getMatchingImageNameList(List<RecognizeResult> recognizeResultList,
			ImageNameUID imageNameUID) {
		String imageName = SvmConstants.EMPTY_STRING;
		List<RecognizeResult> recognizeResultListTemp = new ArrayList<>();

		for (RecognizeResult recognizeResult : recognizeResultList) {
			if (recognizeResult.isMatch()) {
				recognizeResultListTemp.add(recognizeResult);
			}
		}

		// Iterate the new temp list and take the first record's face UID
		for (RecognizeResult recognizeResult : recognizeResultListTemp) {
			imageName = recognizeResult.getImgName().split(SvmConstants.AT_RATE)[0];
//			System.out.println("Confidence Value: " + recognizeResult.getConfidence());
			break;
		}

		return imageName;
	}
	
	private void UpdateImageNameUIDWithFaceUID(String imgIdStr, String faceUid, ImageNameUID imageNameUID) {
		if (imageNameUID.getImageUid().equals(imgIdStr)) {
			imageNameUID.setFaceUid(faceUid);
		}
	}
	
}

