package org.wipro.svm.utils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javafx.scene.CacheHint;
import javafx.scene.layout.Pane;

public class SVMStringUtils {

	public static void main(String[] args) {
//		Map<String, String> prodIdPriceMap = new HashMap<String, String>();
//		String responseMessage = "External Sale Ui Plugin;Product;result=<?xml version%3D"+"\"1.0\""+ " encoding%3D"+"\"UTF-8\""+"?><Product><id>6</id><unique_name>61</unique_name><product_combo_code></product_combo_code><product_age_suggest_child>true</product_age_suggest_child><product_type>1</product_type><qty_to_maintain>0</qty_to_maintain><product_for_sale>true</product_for_sale><product_name>Coco Cola</product_name><product_age_suggest_senior>true</product_age_suggest_senior><product_remark></product_remark><product_category>7</product_category><product_age_suggest_adult>true</product_age_suggest_adult><product_details_photo></product_details_photo><product_cost>35</product_cost><product_photo>C:/Vendron/products_images/coco_cola.png</product_photo><product_gender_suggest_female>true</product_gender_suggest_female><product_gender_suggest_male>true</product_gender_suggest_male><unique_id></unique_id><product_price>1</product_price><product_age_suggest_young_adult>true</product_age_suggest_young_adult><product_description>Coco Cola 220ml</product_description><product_non_inventory>false</product_non_inventory></Product>#";
//		new SVMStringUtils().getProduct(responseMessage, prodIdPriceMap, null);
		
//		String responseMessage = "QR Reader Plugin;qr_status;status=success&read_tag=000000000000421#";
//		String responseMessage = "QR Reader Plugin;qr_status;read_tag=000000000000421&status=success#";
//		String readTag = new SVMStringUtils().getReadTag(responseMessage);
//		System.out.println(readTag);
	}

	public List<String> getProductsForSaleString(String responseMessage) {
		List<String> stringList = new ArrayList<String>();
		int count = 1;
		String[] responseStrArr = null;

		String[] responseMessageArr = responseMessage.split(SvmConstants.EQUALS);
		for (String responseStr : responseMessageArr) {
			if (count > 1) {
				responseStrArr = responseStr.split(SvmConstants.COMMA);
			}
			count++;
		}

		for (String productIds : responseStrArr) {
			stringList.add(productIds.replaceAll(SvmConstants.STRING_REPLACE_ONE, SvmConstants.EMPTY_STRING));
		}

		return stringList;
	}

	public Map<String, String> getProduct(String responseMessage, Map<String, String> prodIdPriceMap, Properties props) {
		String unitPrice = null, productId = null;
		File srcFile = null;

		try {
			// Replace header characters and build proper xml string
			if (responseMessage.contains(SvmConstants.HEADER_STRING)) {
				responseMessage = responseMessage.replace(SvmConstants.HEADER_STRING, SvmConstants.EMPTY_STRING);
			}
			if (responseMessage.contains(SvmConstants.HASH)) {
				responseMessage = responseMessage.replace(SvmConstants.HASH, SvmConstants.EMPTY_STRING);
			}

			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(responseMessage));

			Document doc = db.parse(is);
			NodeList nodes = doc.getElementsByTagName(SvmConstants.PRODUCT);

			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);

				NodeList id = element.getElementsByTagName(SvmConstants.ID);
				Element line = (Element) id.item(0);
				productId = getCharacterDataFromElement(line);

				NodeList productCost = element.getElementsByTagName(SvmConstants.PRODUCT_COST);
				line = (Element) productCost.item(0);
				unitPrice = getCharacterDataFromElement(line) + SvmConstants.TILDA;

				NodeList productPhoto = element.getElementsByTagName(SvmConstants.PRODUCT_PHOTO);
				line = (Element) productPhoto.item(0);
				srcFile = new File(getCharacterDataFromElement(line));
			}

			// Copy product image to products folder
			File destDir = new File(props.getProperty("products.images.path"));
			FileUtils.copyFileToDirectory(srcFile, destDir);
			
			// Rename the file with ProductID
			File getSavedFile = new File(props.getProperty("products.images.path") + srcFile.getName());
			File renameFile = new File(props.getProperty("products.images.path") + productId + SvmConstants.PNG_FILE_EXTENSION);
			getSavedFile.renameTo(renameFile);
			
			// get Product Id and Unit Price
			prodIdPriceMap.put(productId, unitPrice);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return prodIdPriceMap;
	}
	
	public String getCharacterDataFromElement(Element e) {
		Node child = e.getFirstChild();
		if (child instanceof org.w3c.dom.CharacterData) {
			org.w3c.dom.CharacterData cd = (org.w3c.dom.CharacterData) child;
			return cd.getData();
		}
		return SvmConstants.EMPTY_STRING;
	}
	
	public String getProductBalance(String responseMessage) {
		responseMessage = responseMessage.split(SvmConstants.SPLIT_PATTERN_THREE)[3];

		return responseMessage;
	}

	public String getReadTag(String responseMessage) {
		responseMessage = responseMessage.split(SvmConstants.READ_TAG)[1].replaceAll(SvmConstants.STRING_REPLACE_THREE, SvmConstants.EMPTY_STRING);

		return responseMessage;
	}

	public Pane applyCachingPropToPane(Pane pane) {
		pane.setCache(true);
		pane.setCacheShape(true);
		pane.setCacheHint(CacheHint.SPEED);
		
		return pane;
	}
	
}
