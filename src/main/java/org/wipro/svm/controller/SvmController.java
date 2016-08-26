package org.wipro.svm.controller;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.wipro.svm.model.Weather;
import org.wipro.svm.service.impl.FaceDetectionMSoftSvcImpl;
import org.wipro.svm.service.impl.SocketAPIServiceImpl;
import org.wipro.svm.utils.PropertyFileLoader;
import org.wipro.svm.utils.SVMStringUtils;
import org.wipro.svm.utils.SvmConstants;
import org.wipro.svm.utils.WeatherService;
import org.wipro.svm.view.GreetingsLayout;
import org.wipro.svm.view.ProdLayout;
import org.wipro.svm.virtual.keyboard.control.KeyBoardPopup;
import org.wipro.svm.virtual.keyboard.control.KeyBoardPopupBuilder;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SvmController extends Application {
	private final static Logger logger = Logger.getLogger(SvmController.class);

	private static HttpHost proxy = null;
	private static Dimension screenSize;
	public static Socket client;
	private volatile boolean socketThread = false;
	public static MediaPlayer mediaPlayer;
	ReadThread readThread;
	SocketAPIServiceImpl socketAPI = new SocketAPIServiceImpl();
	SVMStringUtils svmStrUtils = new SVMStringUtils();
	public static final Object monitor = new Object();
	public static boolean monitorState = false;
	public static boolean buyProduct = false;
	public static PropertyFileLoader pFL = new PropertyFileLoader();
	public static Properties props = new Properties();
	private static String imageName = "";
	private static String responseMessage = new String();
	private static boolean qrCodeScan = false;
	private static boolean frDone = false;
	private static boolean addHomeBtn = true;
	private static boolean homeBtnClicked = false;
	private static int counter = 0;
	private static int firstTime = 0;
	
	private static double width;
	private static double height;
	private static double heightSplitByTen;
	private static double heightTop;
	private static double heightMiddle;
	private static double heightBottom;

	private static StackPane videoVboxGreetings = new StackPane();
	private static VBox greetingsVBox = new VBox(8);
	private static VBox weatherVboxProducts = new VBox(8);
	private static Stage existingStage;
	public static Map<String, String> prodIdPriceMap = new HashMap<String, String>();


	public static boolean isAddHomeBtn() {
		return addHomeBtn;
	}

	public static void setAddHomeBtn(boolean addHomeBtn) {
		SvmController.addHomeBtn = addHomeBtn;
	}

	public static boolean isHomeBtnClicked() {
		return homeBtnClicked;
	}

	public static void setHomeBtnClicked(boolean homeBtnClicked) {
		SvmController.homeBtnClicked = homeBtnClicked;
	}

	public static double getWidth() {
		return width;
	}

	public static void setWidth(double width) {
		SvmController.width = width;
	}

	public static double getHeight() {
		return height;
	}

	public static void setHeight(double height) {
		SvmController.height = height;
	}

	public static double getHeightSplitByTen() {
		return heightSplitByTen;
	}

	public static void setHeightSplitByTen(double heightSplitByTen) {
		SvmController.heightSplitByTen = heightSplitByTen;
	}

	public static double getHeightTop() {
		return heightTop;
	}

	public static void setHeightTop(double heightTop) {
		SvmController.heightTop = heightTop;
	}

	public static double getHeightMiddle() {
		return heightMiddle;
	}

	public static void setHeightMiddle(double heightMiddle) {
		SvmController.heightMiddle = heightMiddle;
	}

	public static double getHeightBottom() {
		return heightBottom;
	}

	public static void setHeightBottom(double heightBottom) {
		SvmController.heightBottom = heightBottom;
	}

	public static int getFirstTime() {
		return firstTime;
	}

	public static void setFirstTime(int firstTime) {
		SvmController.firstTime = firstTime;
	}

	/*public static MediaPlayer getMediaPlayer() {
		return mediaPlayer;
	}

	public static void setMediaPlayer(MediaPlayer mediaPlayer) {
		SvmController.mediaPlayer = mediaPlayer;
	}*/

	public static Map<String, String> getProdIdPriceMap() {
		return prodIdPriceMap;
	}

	public static void setProdIdPriceMap(Map<String, String> prodIdPriceMap) {
		SvmController.prodIdPriceMap = prodIdPriceMap;
	}

	public boolean isSocketThread() {
		return socketThread;
	}

	public void setSocketThread(boolean socketThread) {
		this.socketThread = socketThread;
	}
	
	public static int getCounter() {
		return counter;
	}

	public static void setCounter(int counter) {
		SvmController.counter = counter;
	}

	public static boolean isFrDone() {
		return frDone;
	}

	public static void setFrDone(boolean frDone) {
		SvmController.frDone = frDone;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public static boolean isQrCodeScan() {
		return qrCodeScan;
	}

	public static void setQrCodeScan(boolean qrCodeScan) {
		SvmController.qrCodeScan = qrCodeScan;
	}

	public static String getImageName() {
		return imageName;
	}

	public static void setImageName(String imageName) {
		SvmController.imageName = imageName;
	}

	public static Socket getClient() {
		return client;
	}

	public static void setClient(Socket client) {
		SvmController.client = client;
	}

	public SvmController() {
	}

	public SvmController(Stage primaryStage, StackPane videoVbox, VBox weatherVbox, VBox messageVbox) {
		existingStage = primaryStage;
		videoVboxGreetings = videoVbox;
		weatherVboxProducts = weatherVbox;
		greetingsVBox = messageVbox;
	}

	public static void main(String[] args) {
		// Get Screen Fit Size
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		// Load Properties File
		props = pFL.loadPropertyFile();
		
		// check proxy server is present
		if (props.getProperty("http.proxyHost") != null && props.getProperty("http.proxyPort") != null) {// Proxy present
			proxy = new HttpHost(props.getProperty("http.proxyHost"), Integer.parseInt(props.getProperty("http.proxyPort")));
		}
		
		// Launch app
		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		// save all products
		new SvmController().saveAllProducts(primaryStage);
	}

	public void showVideoWeatherAndProductsScreen(Stage primaryStage) {
		try {
			setWidth(getScreenWidth(screenSize));
			setHeight(getScreenHeight(screenSize));
			
			setHeightSplitByTen(getHeight() / 10);
			
			setHeightTop(getHeightSplitByTen() * Double.parseDouble(props.getProperty("top.height")));
			
			setHeightMiddle(getHeightSplitByTen() * Double.parseDouble(props.getProperty("middle.height")));
			
			setHeightBottom(getHeightSplitByTen() * Double.parseDouble(props.getProperty("bottom.height")));
			
			StackPane touchAnywherePane = new StackPane();
			VBox productsListTileVBox = new VBox();
			VBox productsListVBoxOne = new VBox();
			VBox productsListVBoxTwo = new VBox();
			
			VBox weatherVbox = new VBox(8);
			
			// Get Video VBox Layout
			createVideoLayout(touchAnywherePane);
//			mediaPlayer.play();

			// Create Weather VBox
			createWeatherLayout(weatherVbox);

			ProdLayout prodLayout = new ProdLayout(primaryStage, touchAnywherePane, weatherVbox, productsListTileVBox, props, proxy);
			
			productsListVBoxOne = prodLayout.showProductsList(primaryStage, 1);
			productsListVBoxTwo = prodLayout.showProductsList(primaryStage, 2);
			
			productsListTileVBox.getChildren().addAll(productsListVBoxOne, productsListVBoxTwo);
			
			showPrimaryStageProductsCarousel(primaryStage, touchAnywherePane, productsListTileVBox, weatherVbox);
		} catch (Exception ex) {
			logger.error(ex);
		}
	}
	
//	Initial Implementation
	/*public void startFDR() {
		try {
			// Get processed image name
			setImageName(new FaceDetectionServiceImpl(props).startFaceRecognition());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
//	BetaFace Implementation
	/*public ImgNameAndFaceId startFDR() {
		ImgNameAndFaceId imgNameAndFaceId = new ImgNameAndFaceId();
		
		try {
			System.out.println("Before Capture: " + Calendar.getInstance().getTime());
			imgNameAndFaceId = new FaceDetectionBetaFaceImpl(props).startFaceRecognition();
			if (imgNameAndFaceId.getMatchingImgName() != null && !imgNameAndFaceId.getMatchingImgName().equals(SvmConstants.EMPTY_STRING)) {
//				setImageName(imgNameAndFaceId.getMatchingImgName());
				setImageName("John");
				System.out.println("Matching Image Name: " + imgNameAndFaceId.getMatchingImgName());
			} else {
				setImageName(SvmConstants.EMPTY_STRING);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Response Received: " + Calendar.getInstance().getTime());
		return imgNameAndFaceId;
	}*/

//	Microsoft Cognitive Services Face Implementation
	public String startFDR(ProgressBar bar) {
		String matchingImgName = SvmConstants.EMPTY_STRING;
		
		try {
			matchingImgName = new FaceDetectionMSoftSvcImpl(props, proxy).startFaceRecognition(bar);
			if (matchingImgName != null && !matchingImgName.equals(SvmConstants.EMPTY_STRING)) {
				setImageName(matchingImgName);
				System.out.println("Matching Image Name: " + matchingImgName);
			} else {
				setImageName(matchingImgName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Response Received: " + Calendar.getInstance().getTime());
		return matchingImgName;
	}
	
//	BetaFace Implementation
	/*public VBox showGreetingsPage(ImgNameAndFaceId imgNameAndFaceId) {
		try {
			String greetingsMessage = "";
			boolean user = false;
			KeyBoardPopup keyBoardPopup = KeyBoardPopupBuilder.create().initLocale(Locale.ENGLISH).build();

			// Create greeting layout
			GreetingsLayout greetingsLayout = new GreetingsLayout(existingStage, videoVboxGreetings,
					weatherVboxProducts, greetingsVBox, props);

			// Create Message VBox
			if (getImageName() != null && getImageName().equals(SvmConstants.EMPTY_STRING)) {
				// for non-existing user
				greetingsMessage = MessageFormat.format((String) props.get("greetings.non.existing.user"), "");
				greetingsVBox = greetingsLayout.showGreetingsLayout(greetingsMessage, user, keyBoardPopup, imgNameAndFaceId);
			} else {
				// for existing user
				user = true;
				new ProdLayout().new PlayAudioThread(props.getProperty("user.greeting.message.path")).start();
				
				// Clean gallery folder
				FileUtils.cleanDirectory(new File(props.getProperty("source.face.images.path")));
				
				greetingsMessage = MessageFormat.format((String) props.get("greetings.existing.user"), getImageName(),
						getImageName());
				greetingsVBox = greetingsLayout.showGreetingsLayout(greetingsMessage, user, keyBoardPopup, imgNameAndFaceId);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return greetingsVBox;
	}*/
	
//	Microsoft Cognitive Services Face Implementation
	public VBox showGreetingsPage(String imgName) {
		try {
			String greetingsMessage = "";
			boolean user = false;
			KeyBoardPopup keyBoardPopup = KeyBoardPopupBuilder.create().initLocale(Locale.ENGLISH).build();

			// Create greeting layout
			GreetingsLayout greetingsLayout = new GreetingsLayout(existingStage, videoVboxGreetings,
					weatherVboxProducts, greetingsVBox, props, proxy);

			// Create Message VBox
			if (getImageName() != null && getImageName().equals(SvmConstants.EMPTY_STRING)) {
				// for non-existing user
				greetingsMessage = MessageFormat.format((String) props.get("greetings.non.existing.user"), "");
				greetingsVBox = greetingsLayout.showGreetingsLayout(greetingsMessage, user, keyBoardPopup, imgName);
			} else {
				// for existing user
				user = true;
				new ProdLayout().new PlayAudioThread(props.getProperty("user.greeting.message.path")).start();

				// Clean gallery folder
				FileUtils.cleanDirectory(new File(props.getProperty("source.face.images.path")));
				
				greetingsMessage = MessageFormat.format((String) props.get("greetings.existing.user"), getImageName(),
						getImageName());
				greetingsVBox = greetingsLayout.showGreetingsLayout(greetingsMessage, user, keyBoardPopup, imgName);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return greetingsVBox;
	}

	public void saveAllProducts(Stage primaryStage) {
		try {
			List<String> productIdList = new ArrayList<String>();

			// Clean destination directory
			FileUtils.cleanDirectory(new File(props.getProperty("products.images.path")));
			
			// Show Products List for Un-Known User and Start Read Thread
			startReadThread();

			socketAPI.getProductsForSale(client);

			waitForThread();

			String respMsgAllProds = getResponseMessage();
			productIdList = svmStrUtils.getProductsForSaleString(respMsgAllProds);

			for (String productId : productIdList) {
				// get each product details
				socketAPI.getProduct(client, productId);

				waitForThread();

				String respMsgprdDetails = getResponseMessage();
				svmStrUtils.getProduct(respMsgprdDetails, prodIdPriceMap, props);
			}
			readThread.shutdown();

			// Don't add Home Btn
			setAddHomeBtn(false);
			showVideoWeatherAndProductsScreen(primaryStage);
		} catch (NumberFormatException | IOException ex) {
			ex.printStackTrace();
		}
	}

//	Old Impl
	/*public void createVideoLayout(StackPane touchAnywherePane) {
		HBox touchAnywhereHBox = new HBox();
		ImageView gifView = null;
		MediaView mediaView = null;

		String filePath = props.getProperty("video.path");
		String[] fileExtension = filePath.split("\\.");

		if (fileExtension[1].equals(SvmConstants.GIF_FILE_TYPE)) {
			gifView = new ImageView(
					new Image(new File(props.getProperty("video.path")).toURI().toString()));
			gifView.setFitWidth(getWidth());
			gifView.setFitHeight(getHeightTop());
//			gifView.setCache(true);
//			gifView.setCacheHint(CacheHint.SPEED);
		} else {
			Media media = new Media(new File(props.getProperty("video.path")).toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			mediaPlayer.setAutoPlay(true);
			mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);

			mediaView = new MediaView(mediaPlayer);
			mediaView.setPreserveRatio(false);
			mediaView.setFitWidth(getWidth());
			mediaView.setFitHeight(getHeightTop());
		}

		if (getFirstTime() == 0) {
			touchAnywhereHBox.setAlignment(Pos.BASELINE_CENTER);

			ImageView touchAnywhereView = new ImageView(
					new Image(new File(props.getProperty("touch.anywhere.path")).toURI().toString()));
			touchAnywhereView.setFitWidth(width / 2);
			touchAnywhereView.setFitHeight(heightTop);
			touchAnywhereView.setOpacity(0.9);

			touchAnywhereHBox.getChildren().add(touchAnywhereView);

			if (fileExtension[1].equals(SvmConstants.GIF_FILE_TYPE)) {
				touchAnywherePane.getChildren().addAll(gifView, touchAnywhereHBox);
			} else {
				touchAnywherePane.getChildren().addAll(mediaView, touchAnywhereHBox);	
			}
			setFirstTime(1);
		} else {
			if (fileExtension[1].equals(SvmConstants.GIF_FILE_TYPE)) {
				touchAnywherePane.getChildren().addAll(gifView);
			} else {
				touchAnywherePane.getChildren().addAll(mediaView);	
			}
		}

		svmStrUtils.applyCachingPropToPane(touchAnywherePane);
	}*/

//	New Impl
	public void createVideoLayout(StackPane touchAnywherePane) {
		HBox touchAnywhereHBox = new HBox();
		ImageView gifView = null;
		MediaView mediaView = null;

		String filePath = props.getProperty("videos.paths");
		
//		Get filenames
		String[] filesPaths = filePath.split(SvmConstants.COMMA);
		String randomFilePath = (filesPaths[new Random().nextInt(filesPaths.length)]);
		
		Media media = new Media(new File(randomFilePath).toURI().toString());
		mediaPlayer = new MediaPlayer(media);
		mediaPlayer.setAutoPlay(true);
		mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);

		mediaView = new MediaView(mediaPlayer);
		mediaView.setPreserveRatio(false);
		mediaView.setFitWidth(getWidth());
		mediaView.setFitHeight(getHeightTop());

		if (getFirstTime() == 0) {
			touchAnywhereHBox.setAlignment(Pos.BASELINE_CENTER);

			ImageView touchAnywhereView = new ImageView(
					new Image(new File(props.getProperty("touch.anywhere.path")).toURI().toString()));
			touchAnywhereView.setFitWidth(width / 2);
			touchAnywhereView.setFitHeight(heightTop);
			touchAnywhereView.setOpacity(0.9);

			touchAnywhereHBox.getChildren().add(touchAnywhereView);
			touchAnywherePane.getChildren().addAll(mediaView, touchAnywhereHBox);
			setFirstTime(1);
		} else {
			touchAnywherePane.getChildren().addAll(mediaView);
		}

		svmStrUtils.applyCachingPropToPane(touchAnywherePane);
	}
	
	private void createWeatherLayout(VBox weatherVbox) {
		Weather weather = null;

		Label locationText = new Label();
		Label dateText = new Label();
		Label temperatureCelcius = new Label();
		Label condition = new Label();
		Label humidity = new Label();
		Label pressure = new Label();
//		Label windChill = new Label();

		try {
			File weatherBackgrd = new File(props.getProperty("weather.background"));
			Image image = new Image(weatherBackgrd.toURI().toString());
			BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, false);

			String weatherUrl = String.format(Locale.ROOT, SvmConstants.WEATHER_SUB_URL,
					props.getProperty("weather.city.id"));
			
			// Retrieve Weather Data
			weather = new WeatherService(props).doQuery(weatherUrl);

			getWeatherDetails(weather, locationText, dateText, temperatureCelcius, condition, humidity, pressure);

			VBox locationVBox = new VBox(15);
			locationVBox.getChildren().addAll(locationText, dateText, condition, humidity, pressure);

			final ImageView weatherImv = new ImageView();
			File weatherCloud = new File(props.getProperty("weather.clouds"));
			Image weatherCloudImage = new Image(weatherCloud.toURI().toString());
			weatherImv.setImage(weatherCloudImage);

			HBox locationHBox = new HBox(20);
			locationHBox.setAlignment(Pos.TOP_CENTER);
			locationHBox.getChildren().add(locationVBox);
			
			HBox weatherImvHBox = new HBox(20);
			locationHBox.setAlignment(Pos.CENTER);
			locationHBox.getChildren().add(weatherImv);
			
			HBox temperatureCelciusHBox = new HBox(20);
			locationHBox.setAlignment(Pos.CENTER);
			locationHBox.getChildren().add(temperatureCelcius);
					
			weatherVbox.setBackground(new Background(new BackgroundImage(image, BackgroundRepeat.ROUND, BackgroundRepeat.ROUND,
					BackgroundPosition.DEFAULT, backgroundSize)));
			weatherVbox.getChildren().addAll(locationHBox, weatherImvHBox, temperatureCelciusHBox);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void getWeatherDetails(Weather weather, Label locationText, Label dateText, Label temperatureCelcius,
			Label condition, Label humidity, Label pressure) {

		// Location
		if (weather != null && (weather.getCity() != null || weather.getCountry() != null)) {
			locationText.setText(weather.getCity() + ", " + weather.getCountry());
		} else {
			locationText.setText(props.getProperty("weather.location"));
		}
		locationText.setPadding(new Insets(0, 0, 0, 20));
		locationText.setTextFill(Color.WHITESMOKE);
		locationText.setFont(Font.font(null, FontWeight.BOLD, 30));

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd");
		Calendar calendar = new GregorianCalendar();

		// Current System Date
		dateText.setText(sdf.format(calendar.getTime()));
		dateText.setPadding(new Insets(0, 0, 0, 20));
		dateText.setTextFill(Color.WHEAT);
		dateText.setFont(Font.font(null, FontWeight.BOLD, 30));

		// Temperature in Celsius
		if (weather != null && weather.getTemp() != null) {
			temperatureCelcius.setText(weather.getTemp() + SvmConstants.DEGREE_CELCIUS);
		} else {
			temperatureCelcius.setText(props.getProperty("weather.temperature") + SvmConstants.DEGREE_CELCIUS);
		}
		temperatureCelcius.setPadding(new Insets(0, 0, 0, 20));
		temperatureCelcius.setTextFill(Color.WHITE);
		temperatureCelcius.setFont(Font.font(null, FontWeight.BOLD, 40));

		// Condition
		if (weather != null && weather.getCondition() != null) {
			condition.setText(SvmConstants.WEATHER_CONDITION + weather.getCondition());
		} else {
			condition.setText(props.getProperty("weather.condition"));
		}
		condition.setPadding(new Insets(0, 0, 0, 20));
		condition.setTextFill(Color.WHITE);
		condition.setFont(Font.font(null, FontWeight.BOLD, 30));

		// Humidity
		if (weather != null && weather.getHumidity() != null) {
			humidity.setText(SvmConstants.WEATHER_HUMIDITY + weather.getHumidity());
		} else {
			humidity.setText(props.getProperty("weather.humidity"));
		}
		humidity.setPadding(new Insets(0, 0, 0, 20));
		humidity.setTextFill(Color.WHITE);
		humidity.setFont(Font.font(null, FontWeight.BOLD, 30));

		// Pressure
		/*if (weather != null && weather.getPressure() != null) {
			pressure.setText(SvmConstants.WEATHER_PRESSURE + weather.getPressure());
		} else {
			pressure.setText(props.getProperty("weather.pressure"));
		}
		pressure.setPadding(new Insets(0, 0, 0, 20));
		pressure.setTextFill(Color.WHITE);
		pressure.setFont(Font.font(null, FontWeight.BOLD, 30));*/

		// Wind Chill
		/* if (weather != null && weather.getChill()!= null) {
		 windChill.setText(SvmConstants.WEATHER_WIND_CHILL +
		 weather.getChill());
		 } else {
		 windChill.setText(props.getProperty("weather.windchill"));
		 }
		 windChill.setPadding(new Insets(0, 0, 0, 20));
		 windChill.setTextFill(Color.WHITE);
		 windChill.setFont(Font.font(null, FontWeight.BOLD, 30));*/
	}

	public void showVideoLayout(Stage primaryStage, FlowPane videoVbox, double width, double height) {
		// Create Main VBox
		VBox mainVbox = new VBox(8);
		mainVbox.setMaxSize(width, height);
		mainVbox.getChildren().addAll(videoVbox);

		// Create Scene
		Scene messageScene = new Scene(mainVbox, width, height, Color.WHITESMOKE);
		messageScene.getStylesheets().addAll("css/style_button.css", "css/style_text.css",
				"css/style_keyboard_Button.css", "css/style_combobox.css");

		primaryStage.centerOnScreen();
		primaryStage.setScene(messageScene);
		// primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
		// primaryStage.setFullScreen(true);
		primaryStage.show();
	}

	public void showPrimaryStageGreetings(Stage primaryStage, StackPane videoVbox, VBox messageVbox, VBox weatherVbox, KeyBoardPopup keyBoardPopup) {
		try {
			// Add home btn in carousel screen
			setAddHomeBtn(true);
			
			Font.loadFont(new File(props.getProperty("keyboard.font")).toURL().toString(), 10);

			File weatherBackgrd = new File(props.getProperty("greetings.background"));
			Image image = new Image(weatherBackgrd.toURI().toString());
			BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, false); 
			messageVbox.setBackground(new Background(
					new BackgroundImage(image, BackgroundRepeat.ROUND,
							BackgroundRepeat.ROUND,BackgroundPosition.DEFAULT, backgroundSize)));
			
			videoVbox.setPrefSize(getWidth(), getHeightTop());
			videoVbox.autosize();
			messageVbox.setPrefSize(getWidth(), getHeightMiddle());
			messageVbox.autosize();
			weatherVbox.setPrefSize(getWidth(), getHeightBottom());
			weatherVbox.autosize();
			
			// Create Main VBox
			VBox mainVbox = new VBox(8);
			mainVbox.setMaxSize(getWidth(), getHeight());
			mainVbox.getChildren().addAll(videoVbox, messageVbox, weatherVbox);

			// Create Scene
			Scene messageScene = new Scene(mainVbox, width, height, Color.WHITESMOKE);
			messageScene.setCursor(Cursor.NONE);
			messageScene.getStylesheets().addAll("css/style_button.css", "css/style_text.css",
					"css/style_keyboard_Button.css");
			
			primaryStage.centerOnScreen();
			primaryStage.setScene(messageScene);
			keyBoardPopup.addFocusListener(messageScene);

			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void showPrimaryStageProductsCarousel(Stage existingStage, StackPane videoVboxProducts,
			VBox productsVBox, VBox weatherVboxProducts) throws IOException {
		
		// Clear gallery directory
		File source = new File(props.getProperty("source.face.images.path"));
		FileUtils.cleanDirectory(source);
		
		File weatherBackgrd = new File(props.getProperty("carousel.background"));
		Image image = new Image(weatherBackgrd.toURI().toString());
		BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, false); 
		productsVBox.setBackground(new Background(
				new BackgroundImage(image, BackgroundRepeat.ROUND,
						BackgroundRepeat.ROUND, BackgroundPosition.CENTER, backgroundSize)));
		
		videoVboxProducts.setPrefSize(getWidth(), getHeightTop());
		videoVboxProducts.autosize();
		productsVBox.setPrefSize(getWidth(), getHeightMiddle());
		productsVBox.autosize();
		weatherVboxProducts.setPrefSize(getWidth(), getHeightBottom());
		weatherVboxProducts.autosize();
		
		// Create Main VBox
		VBox mainVbox = new VBox(8);
		mainVbox.setMaxSize(getWidth(), getHeight());
		mainVbox.getChildren().addAll(videoVboxProducts, productsVBox, weatherVboxProducts);

		// Create Scene
		Scene productsScene = new Scene(mainVbox, width, height, Color.WHITESMOKE);
		productsScene.setCursor(Cursor.NONE);
		productsScene.getStylesheets().addAll("css/style_button.css", "css/style_text.css", "css/carousel.css");
		productsScene.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (!isFrDone() && !isHomeBtnClicked()) {
					// Start Face Detection & Recognition
					setFrDone(true);

					// show loading... splash screen
					VBox messageVbox = new VBox(8);
					String frOrProd = SvmConstants.FR;

					new ProdLayout().new PlayAudioThread(props.getProperty("new.user.greeting.message.path")).start();
					
					new GreetingsLayout(existingStage, videoVboxProducts, weatherVboxProducts, messageVbox, props, proxy)
							.showPleaseWaitStage(frOrProd);
				}
				try {
					// Clean gallery directory
					File source = new File(props.getProperty("source.face.images.path"));
					FileUtils.cleanDirectory(source);
					
					setHomeBtnClicked(false);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		if (existingStage.getStyle().equals(StageStyle.DECORATED)) {
			existingStage.initStyle(StageStyle.UNDECORATED);
		}
		existingStage.centerOnScreen();
		existingStage.setScene(productsScene);
		existingStage.show();
	}
	
	public void showPrimaryStageAddOnServiceCarousel(Stage existingStage, StackPane videoVboxProducts,
			VBox productsVBox, VBox weatherVboxProducts, boolean buyProduct) {
		
		File weatherBackgrd = new File(props.getProperty("carousel.background"));
		Image image = new Image(weatherBackgrd.toURI().toString());
		BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, false); 
		productsVBox.setBackground(new Background(
				new BackgroundImage(image, BackgroundRepeat.ROUND,
						BackgroundRepeat.ROUND, BackgroundPosition.CENTER, backgroundSize)));
		
		
		videoVboxProducts.setPrefSize(getWidth(), getHeightTop());
		videoVboxProducts.autosize();
		productsVBox.setPrefSize(getWidth(), getHeightMiddle());
		productsVBox.autosize();
		weatherVboxProducts.setPrefSize(getWidth(), getHeightBottom());
		weatherVboxProducts.autosize();
		
		// Create Main VBox
		VBox mainVbox = new VBox(8);
		mainVbox.setMaxSize(getWidth(), getHeight());
		mainVbox.getChildren().addAll(videoVboxProducts, productsVBox, weatherVboxProducts);

		// Create Scene
		Scene productsScene = new Scene(mainVbox, width, height, Color.WHITESMOKE);
		productsScene.setCursor(Cursor.NONE);
		productsScene.getStylesheets().addAll("css/style_button.css", "css/style_text.css", "css/carousel.css");
		productsScene.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (!isFrDone()) {
					// Start Face Detection & Recognition
					setFrDone(true);

					// show loading... splash screen
					VBox messageVbox = new VBox(8);
					String frOrProd = SvmConstants.FR;

					new ProdLayout().new PlayAudioThread(props.getProperty("new.user.greeting.message.path")).start();
					
					new GreetingsLayout(existingStage, videoVboxProducts, weatherVboxProducts, messageVbox, props, proxy)
							.showPleaseWaitStage(frOrProd);
				}
			}
		});
		
		existingStage.centerOnScreen();
		existingStage.setScene(productsScene);
		existingStage.show();
	}

	public void showPrimaryStageProductNutrition(Stage existingStage, StackPane videoVboxProducts,
			BorderPane productNutritionQtyPane, VBox weatherVboxProducts) {

		File weatherBackgrd = new File(props.getProperty("greetings.background"));
		Image image = new Image(weatherBackgrd.toURI().toString());
		BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, false); 
		productNutritionQtyPane.setBackground(new Background(
				new BackgroundImage(image, BackgroundRepeat.ROUND,
						BackgroundRepeat.ROUND,BackgroundPosition.DEFAULT, backgroundSize)));
		
		videoVboxProducts.setPrefSize(width, heightTop);
		videoVboxProducts.autosize();
		productNutritionQtyPane.setPrefSize(width, heightMiddle);
		productNutritionQtyPane.autosize();
		weatherVboxProducts.setPrefSize(width, heightBottom);
		weatherVboxProducts.autosize();
		
		// Create Main VBox
		VBox mainVbox = new VBox(8);
		mainVbox.setMaxSize(width, height);
		mainVbox.getChildren().addAll(videoVboxProducts, productNutritionQtyPane, weatherVboxProducts);

		// Create Scene
		Scene productsScene = new Scene(mainVbox, width, height, Color.WHITESMOKE);
		productsScene.setCursor(Cursor.NONE);
		productsScene.getStylesheets().addAll("css/style_button.css", "css/style_text.css", "css/carousel.css", "css/style_combobox.css");

		existingStage.centerOnScreen();
		existingStage.setScene(productsScene);
		existingStage.show();

	}

	public void showPleaseWaitPrimaryStage(Stage existingStage, StackPane videoVboxProducts, VBox pleaseWaitTilePane,
			VBox weatherVboxProducts) {

		File weatherBackgrd = new File(props.getProperty("greetings.background"));
		Image image = new Image(weatherBackgrd.toURI().toString());
		BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, false); 
		pleaseWaitTilePane.setBackground(new Background(
				new BackgroundImage(image, BackgroundRepeat.ROUND,
						BackgroundRepeat.ROUND,BackgroundPosition.DEFAULT, backgroundSize)));
		
		videoVboxProducts.setPrefSize(width, heightTop);
		videoVboxProducts.autosize();
		pleaseWaitTilePane.setPrefSize(width, heightMiddle);
		pleaseWaitTilePane.autosize();
		weatherVboxProducts.setPrefSize(width, heightBottom);
		weatherVboxProducts.autosize();
		
		// Create Main VBox
		VBox mainVbox = new VBox(8);
		mainVbox.setMaxSize(width, height);
		mainVbox.getChildren().addAll(videoVboxProducts, pleaseWaitTilePane, weatherVboxProducts);

		// Create Scene
		Scene productsScene = new Scene(mainVbox, width, height);
		productsScene.setCursor(Cursor.NONE);
		productsScene.setFill(Color.TRANSPARENT);
		productsScene.getStylesheets().addAll("css/style_button.css", "css/style_text.css",
				"css/style_keyboard_Button.css");

		existingStage.centerOnScreen();
		existingStage.setScene(productsScene);
		existingStage.show();
	}

	public void showAddOnServiceConfirmationStage(Stage primaryStage, StackPane videoVbox, VBox messageVbox,
			VBox weatherVbox) {
		
		File weatherBackgrd = new File(props.getProperty("greetings.background"));
		Image image = new Image(weatherBackgrd.toURI().toString());
		BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, false); 
		messageVbox.setBackground(new Background(
				new BackgroundImage(image, BackgroundRepeat.ROUND,
						BackgroundRepeat.ROUND,BackgroundPosition.DEFAULT, backgroundSize)));
		
		videoVbox.setPrefSize(width, heightTop);
		videoVbox.autosize();
		messageVbox.setPrefSize(width, heightMiddle);
		messageVbox.autosize();
		weatherVbox.setPrefSize(width, heightBottom);
		weatherVbox.autosize();
		
		// Create Main VBox
		VBox mainVbox = new VBox(8);
		mainVbox.setMaxSize(width, height);
		mainVbox.getChildren().addAll(videoVbox, messageVbox, weatherVbox);

		// Create Scene
		Scene messageScene = new Scene(mainVbox, width, height, Color.WHITESMOKE);
		messageScene.setCursor(Cursor.NONE);
		messageScene.getStylesheets().addAll("css/style_button.css", "css/style_text.css",
				"css/style_keyboard_Button.css");

		primaryStage.centerOnScreen();
		primaryStage.setScene(messageScene);
		primaryStage.show();
	}
	
	public void showThankYouStage(Stage existingStage, StackPane videoVboxProducts, VBox pleaseWaitTilePane,
			VBox weatherVboxProducts) {

		File weatherBackgrd = new File(props.getProperty("thank.you"));
		Image image = new Image(weatherBackgrd.toURI().toString());
		BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, false); 
		pleaseWaitTilePane.setBackground(new Background(
				new BackgroundImage(image, BackgroundRepeat.ROUND,
						BackgroundRepeat.ROUND,BackgroundPosition.DEFAULT, backgroundSize)));
		
		videoVboxProducts.setPrefSize(width, heightTop);
		videoVboxProducts.autosize();
		pleaseWaitTilePane.setPrefSize(width, heightMiddle);
		pleaseWaitTilePane.autosize();
		weatherVboxProducts.setPrefSize(width, heightBottom);
		weatherVboxProducts.autosize();
		
		// Create Main VBox
		VBox mainVbox = new VBox(8);
		mainVbox.setMaxSize(width, height);
		mainVbox.getChildren().addAll(videoVboxProducts, pleaseWaitTilePane, weatherVboxProducts);

		// Create Scene
		Scene productsScene = new Scene(mainVbox, width, height);
		productsScene.setCursor(Cursor.NONE);
		productsScene.setFill(Color.TRANSPARENT);
		productsScene.getStylesheets().addAll("css/style_button.css", "css/style_text.css",
				"css/style_keyboard_Button.css");

		existingStage.centerOnScreen();
		existingStage.setScene(productsScene);
		existingStage.show();
	}
	
	public void showCreditCardStage(Stage existingStage, StackPane videoVboxProducts, BorderPane cCBorderPane,
			VBox weatherVboxProducts, KeyBoardPopup keyBoardPopup) {
		try {
			Font.loadFont(new File(props.getProperty("keyboard.font")).toURL().toString(), 10);

			File weatherBackgrd = new File(props.getProperty("greetings.background"));
			Image image = new Image(weatherBackgrd.toURI().toString());
			BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, false);
			cCBorderPane.setBackground(new Background(new BackgroundImage(image, BackgroundRepeat.ROUND,
					BackgroundRepeat.ROUND, BackgroundPosition.DEFAULT, backgroundSize)));

			videoVboxProducts.setPrefSize(width, heightTop);
			videoVboxProducts.autosize();
			cCBorderPane.setPrefSize(width, heightMiddle);
			cCBorderPane.autosize();
			weatherVboxProducts.setPrefSize(width, heightBottom);
			weatherVboxProducts.autosize();

			// Create Main VBox
			VBox mainVbox = new VBox(8);
			mainVbox.setMaxSize(width, height);
			mainVbox.getChildren().addAll(videoVboxProducts, cCBorderPane, weatherVboxProducts);

			// Create Scene
			Scene productsScene = new Scene(mainVbox, width, height);
			productsScene.setCursor(Cursor.NONE);
			productsScene.setFill(Color.TRANSPARENT);
			productsScene.getStylesheets().addAll("css/style_button.css", "css/style_text.css",
					"css/style_keyboard_Button.css");

			existingStage.centerOnScreen();
			existingStage.setScene(productsScene);
			keyBoardPopup.addFocusListener(productsScene);
			
			existingStage.show();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public ReadThread startReadThread() {
		try {
			client = new Socket(props.getProperty("socket.ip.address"), Integer.parseInt(props.getProperty("socket.port")));
			readThread = new SvmController().new ReadThread();
			readThread.start();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
		
		return readThread;
	}

	public class ReadThread extends Thread {
		public synchronized void run() {
			InputStream in = null;
			try {
				in = client.getInputStream();

				while (!isSocketThread()) {
					String msg = SvmConstants.EMPTY_STRING;
					byte tmp[] = new byte[1024];

					// read all received bytes
					while (in.available() > 0) {
						int i = in.read(tmp, 0, 1024);
						if (i < 0) {
							break;
						}
						msg += new String(tmp, 0, i);
					}

					// show received event in UI
					if (!msg.isEmpty() && msg.length() >= 3) {
						responseMessage = new String();

//						System.out.println("Server Response: " + msg);
						if (msg.startsWith(SvmConstants.ONE_HASH)) {
							responseMessage = msg.replace(SvmConstants.ONE_HASH, SvmConstants.EMPTY_STRING);
							unlockWaiter();
						} else if (msg.startsWith(SvmConstants.MINUS_ONE_HASH)) {
							responseMessage = msg.replace(SvmConstants.MINUS_ONE_HASH, SvmConstants.EMPTY_STRING);
							unlockWaiter();
						} else if (msg.contains(SvmConstants.CAPTURING_STATUS)) {
							// wait for success status
//							responseMessage = msg;
						} else if (msg.contains(SvmConstants.SUCCESS_STATUS)) {
							responseMessage = msg;
							unlockWaiter();
						} else if (msg.contains(SvmConstants.TIMEOUT_STATUS)) {
							responseMessage = msg;
							unlockWaiter();
						} else {
							responseMessage = msg;
							unlockWaiter();
						}
					}
				}
			} catch (IOException ex) {
				logger.error(ex);
			} finally {
				try {
					in.close();
				} catch (IOException ex) {
					logger.error(ex);
				}
			}
		}
		
		public void shutdown() {
			setSocketThread(true);
		}
	}
	
	public static void waitForThread() {
		monitorState = true;
		while (monitorState) {
			synchronized (monitor) {
				try {
					monitor.wait(); // wait until notified
				} catch (Exception e) {
				}
			}
		}
	}

	public static void unlockWaiter() {
		synchronized (monitor) {
			monitorState = false;
			monitor.notifyAll(); // unlock again
		}
	}

	public double getScreenWidth(Dimension screenSize) {
		double width = screenSize.getWidth();

		return width;
	}

	public double getScreenHeight(Dimension screenSize) {
		double height = screenSize.getHeight();

		return height;
	}
	
}
