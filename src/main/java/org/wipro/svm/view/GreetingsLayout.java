package org.wipro.svm.view;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.wipro.svm.controller.SvmController;
import org.wipro.svm.model.ImgNameAndFaceId;
import org.wipro.svm.service.impl.FaceDetectionMSoftSvcImpl;
import org.wipro.svm.service.impl.FaceRecognitionBetaFaceImpl;
import org.wipro.svm.service.impl.FaceRecognitionMSoftSvcImpl;
import org.wipro.svm.service.impl.PrintServiceImpl;
import org.wipro.svm.service.impl.PrintServiceRXTXImpl;
import org.wipro.svm.utils.LocationUtils;
import org.wipro.svm.utils.SvmConstants;
import org.wipro.svm.virtual.keyboard.control.KeyBoardPopup;
import org.wipro.svm.virtual.keyboard.control.KeyBoardPopupBuilder;
import org.wipro.svm.virtual.keyboard.control.VkProperties;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class GreetingsLayout implements VkProperties {

	private final static Logger logger = Logger.getLogger(GreetingsLayout.class);

	HttpHost proxy;
	SvmController svmControllerParams;
	private static ProgressBarThread progressBarThread;
	String frOrProd;
	private static int fdrCount = 1;
	private static boolean progressThread = false;
	private static boolean fRCompleted = false;
	private static StackPane videoVboxProducts = new StackPane();
	private static VBox messageVboxProducts = new VBox(8);
	private static VBox weatherVboxProducts = new VBox(8);
	private static String randomString = null;
	private static Stage existingStage = new Stage();
	private static Properties properties = new Properties();
	private static ProdLayout prodLayout;
	private VBox productsListVBox = new VBox();
	PrintServiceRXTXImpl printServiceRXTXImpl = new PrintServiceRXTXImpl();

	public static boolean isProgressThread() {
		return progressThread;
	}

	public static void setProgressThread(boolean progressThread) {
		GreetingsLayout.progressThread = progressThread;
	}

//	public static ProgressBarThread getProgressBarThread() {
//		return progressBarThread;
//	}

	public GreetingsLayout() {
	}

	public GreetingsLayout(Stage primaryStage, StackPane videoVbox, VBox weatherVbox, VBox messageVbox,
			Properties props, HttpHost proxy) {
		existingStage = primaryStage;
		videoVboxProducts = videoVbox;
		weatherVboxProducts = weatherVbox;
		messageVboxProducts = messageVbox;
		properties = props;
		this.proxy = proxy;
	}

	public VBox showGreetingsLayout(String greetingsMsg, boolean user, KeyBoardPopup keyBoardPopup, String imgName) {
		VBox productsVBox = new VBox();
		prodLayout = new ProdLayout(existingStage, videoVboxProducts, weatherVboxProducts, productsVBox,
				properties, proxy);
		HBox hBox = new HBox(50);
		Label textMessage = new Label();
		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(10, 10, 10, 10));
		borderPane.setPrefSize(SvmController.getWidth(), SvmController.getHeightMiddle());
		
		Button okBtn = new Button();
		Button noThanksBtn = new Button();
		final Button myFavoritesBtn = new Button();
		final Button suggestionsBtn = new Button();

		DropShadow ds = new DropShadow();
		ds.setOffsetY(1.5f);
		ds.setColor(Color.color(0.4f, 0.4f, 0.4f));

		textMessage.setAlignment(Pos.CENTER);
		textMessage.setPadding(new Insets(0, 20, 0, 30));
		textMessage.setText(greetingsMsg);
		textMessage.setEffect(ds);
		textMessage.setCache(true);
		textMessage.setWrapText(true);
		textMessage.setTextFill(Color.BLACK);
		textMessage.setFont(Font.font(SvmConstants.FONT_GABRIOLA, FontWeight.BOLD, 40));

		if (user) {
			myFavoritesBtn.setText(properties.getProperty("my.regular.favorites"));
			myFavoritesBtn.setId(SvmConstants.RECORD_SALES);
			myFavoritesBtn.setPrefWidth(SvmController.getWidth() / 2.5);
			myFavoritesBtn.setPrefHeight(SvmController.getHeightMiddle() / 7);
			myFavoritesBtn.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					try {
						myFavoritesBtn.setDisable(true);
						suggestionsBtn.setDisable(true);
						
						// show products carousel screen
						prodLayout = new ProdLayout(existingStage, videoVboxProducts,
								weatherVboxProducts, productsListVBox, properties, proxy);
						productsListVBox = prodLayout.showMyFavoritesProductsList(existingStage);
						
						// Show Products Stage
						new SvmController().showPrimaryStageProductsCarousel(existingStage,
								videoVboxProducts, productsListVBox, weatherVboxProducts);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			suggestionsBtn.setText(properties.getProperty("show.me.suggestions"));
			suggestionsBtn.setId(SvmConstants.RECORD_SALES);
			suggestionsBtn.setPrefWidth(SvmController.getWidth() / 2.5);
			suggestionsBtn.setPrefHeight(SvmController.getHeightMiddle() / 7);
			suggestionsBtn.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					try {
						myFavoritesBtn.setDisable(true);
						suggestionsBtn.setDisable(true);

						// show products carousel screen
						prodLayout = new ProdLayout(existingStage, videoVboxProducts,
								weatherVboxProducts, productsListVBox, properties, proxy);
						productsListVBox = prodLayout.showSuggestionsProductsList(existingStage);
						
						// Show Products Stage
						new SvmController().showPrimaryStageProductsCarousel(existingStage,
								videoVboxProducts, productsListVBox, weatherVboxProducts);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			hBox.setAlignment(Pos.CENTER);
			hBox.getChildren().addAll(myFavoritesBtn, suggestionsBtn);
			
			// Home navigation
			ImageView homeView = new ImageView(
					new Image(new File(properties.getProperty("home.navigation.path")).toURI().toString()));
			homeView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					SvmController.setAddHomeBtn(false);
					SvmController.setHomeBtnClicked(true);
					SvmController.setFirstTime(0);
					SvmController.setFrDone(false);

					new SvmController().showVideoWeatherAndProductsScreen(existingStage);
				}
			});
			
			borderPane.setTop(homeView);
			borderPane.setCenter(textMessage);
			borderPane.setBottom(hBox);

			messageVboxProducts.getChildren().addAll(borderPane);

		} else {
			// Get Country phone code
			String phoneCode = new LocationUtils().getPhone(properties);

			TilePane tPMobile = new TilePane();
			
			tPMobile.setAlignment(Pos.CENTER);
			Label label = new Label(properties.getProperty("enter.mobile.number"));
			label.setEffect(ds);
			label.setTextFill(Color.BLACK);
			label.setFont(Font.font(SvmConstants.FONT_GABRIOLA, FontWeight.BOLD, 40));

			TextField mobileNumber = new TextField();
			mobileNumber.getStyleClass().add(SvmConstants.TEXT_INPUT);
			mobileNumber.setText(phoneCode);
			mobileNumber.setPromptText(SvmConstants.ZERO_TO_NINE);
			mobileNumber.setFocusTraversable(false);
			mobileNumber.getProperties().put(VK_TYPE, SvmConstants.NUMERIC);
			mobileNumber.setMaxSize(250, 50);

			// Accept numbers only
			TextFormatter<String> formatter = new TextFormatter<String>(change -> {
				change.setText(change.getText().replaceAll(SvmConstants.STRING_REPLACE_ONE, SvmConstants.EMPTY_STRING));
				return change;

			});
			mobileNumber.setTextFormatter(formatter);

			tPMobile.getChildren().addAll(label, mobileNumber);

			okBtn.setText(properties.getProperty("ok.button"));
			okBtn.setId(SvmConstants.RECORD_SALES);
			okBtn.setPrefWidth(SvmController.getWidth() / 3);
			okBtn.setPrefHeight(SvmController.getHeightMiddle() / 7);
			okBtn.setOnAction(new EventHandler<ActionEvent>() {
				public void handle(ActionEvent event) {
					String getText = mobileNumber.getText();
					Pattern pattern = Pattern.compile(SvmConstants.PATTERN);
					Matcher matcher = pattern.matcher(getText);

					if (!matcher.matches()) {
						getErrorDialog();
					} else if (!getText.startsWith(SvmConstants.PLUS)) {
						getText = phoneCode + getText;

						randomString = RandomStringUtils.randomAlphanumeric(11);
						if (properties.getProperty("show.print.popup") != null
								&& properties.getProperty("show.print.popup").equals(SvmConstants.YES)) {
							showConfirmationMessage(keyBoardPopup, imgName);
						} else {
							// Print registration id directly...
//							PrintServiceImpl.printText(MessageFormat.format((String) properties.get("print.message"), randomString));
							printServiceRXTXImpl.printText(MessageFormat.format((String) properties.get("print.message"), randomString));

							// show loading... splash screen
							showPleaseWaitStage(SvmConstants.PROD);
						}
					} else {
						randomString = RandomStringUtils.randomAlphanumeric(11);
						if (properties.getProperty("show.print.popup") != null
								&& properties.getProperty("show.print.popup").equals(SvmConstants.YES)) {
							showConfirmationMessage(keyBoardPopup, imgName);
						} else {
							// Print registration id directly...
//							PrintServiceImpl.printText(MessageFormat.format((String) properties.get("print.message"), randomString));
							printServiceRXTXImpl.printText(MessageFormat.format((String) properties.get("print.message"), randomString));

							// show loading... splash screen
							showPleaseWaitStage(SvmConstants.PROD);
						}
					}
				}

				private void getErrorDialog() {
					Alert alert = new Alert(AlertType.ERROR);

					alert.setHeaderText(properties.getProperty("mobile.alert.message"));
					alert.showAndWait();
				}
			});

			noThanksBtn.setText(properties.getProperty("no.thanks"));
			noThanksBtn.setId(SvmConstants.RECORD_SALES);
			noThanksBtn.setPrefWidth(SvmController.getWidth() / 3);
			noThanksBtn.setPrefHeight(SvmController.getHeightMiddle() / 7);
			noThanksBtn.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					try {
//						prodLayout.showPaymentOptionsPage();
//						prodLayout.showThankYouScreen();
						
						VBox productsListVBoxOne = new VBox();
						VBox productsListVBoxTwo = new VBox();
						
						// Clean gallery folder
						FileUtils.cleanDirectory(new File(properties.getProperty("source.face.images.path")));
						
						okBtn.setDisable(true);
						noThanksBtn.setDisable(true);

						// show products carousel screen
						prodLayout = new ProdLayout(existingStage, videoVboxProducts,
								weatherVboxProducts, productsListVBox, properties, proxy);
						productsListVBoxOne = prodLayout.showProductsList(existingStage, 1);
						productsListVBoxTwo = prodLayout.showProductsList(existingStage, 2);
						productsListVBox.getChildren().addAll(productsListVBoxOne, productsListVBoxTwo);
						
						// Show Products Stage
						new SvmController().showPrimaryStageProductsCarousel(existingStage,
								videoVboxProducts, productsListVBox, weatherVboxProducts);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			hBox.setAlignment(Pos.CENTER);
			hBox.getChildren().addAll(okBtn, noThanksBtn);

			// Home navigation
			ImageView homeView = new ImageView(
					new Image(new File(properties.getProperty("home.navigation.path")).toURI().toString()));
			homeView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					SvmController.setAddHomeBtn(false);
					SvmController.setHomeBtnClicked(true);
					SvmController.setFirstTime(0);
					SvmController.setFrDone(false);

					new SvmController().showVideoWeatherAndProductsScreen(existingStage);
				}
			});

			VBox vBox = new VBox(20);
			vBox.getChildren().addAll(textMessage, tPMobile);
			
			borderPane.setTop(homeView);
			borderPane.setCenter(vBox);
			
//			borderPane.setTop(textMessage);
//			borderPane.setCenter(tPMobile);
			borderPane.setBottom(hBox);
			
			messageVboxProducts.getChildren().addAll(borderPane);
		}

		
		return messageVboxProducts;
	}

	private void showConfirmationMessage(KeyBoardPopup keyBoardPopup, String imgName) {
		try {
			File source = new File(properties.getProperty("source.face.images.path"));
//			File destination = new File(properties.getProperty("destination.face.images.path"));
//			FileUtils.copyDirectory(source, destination);
			FileUtils.cleanDirectory(source);
			
			// Save the new customer face using betaface api(SetPerson)
//			new FaceRecognitionBetaFaceImpl(properties).setPersonName(imgNameAndFaceId);
			
			// Add new user face to face list - Microsoft Cognitive Services Face API
			new FaceRecognitionMSoftSvcImpl(properties, proxy).addNewUserFaceToFaceList(new FaceDetectionMSoftSvcImpl().getCapturedImage());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		messageVboxProducts = new VBox(8);
		Button printBtn = new Button();
		Button noPrintBtn = new Button();

		DropShadow ds = new DropShadow();
		ds.setOffsetY(1.5f);
		ds.setColor(Color.color(0.4f, 0.4f, 0.4f));
		
		Label textMessage = new Label();
		textMessage.setText(MessageFormat.format((String) properties.get("print.message.confirmation"), randomString));
		textMessage.setPadding(new Insets(SvmController.getHeightMiddle() / 15, 10, 0, 10));
		textMessage.setEffect(ds);
		textMessage.setCache(true);
		textMessage.setWrapText(true);
		textMessage.setMinHeight(SvmController.getHeightMiddle() / 7);
		textMessage.setMinWidth(SvmController.getWidth() / 2);
		textMessage.setTextFill(Color.BLACK);
		textMessage.setFont(Font.font(SvmConstants.FONT_GABRIOLA, FontWeight.BOLD, 40));

		HBox hBox = new HBox(50);
		hBox.setAlignment(Pos.CENTER);

		printBtn.setText(properties.getProperty("print"));
		printBtn.setId(SvmConstants.RECORD_SALES);
		printBtn.setPrefWidth(SvmController.getWidth() / 3);
		printBtn.setPrefHeight(SvmController.getHeightMiddle() / 7);
		printBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					VBox productsListVBoxOne = new VBox();
					VBox productsListVBoxTwo = new VBox();
					
//					PrintServiceImpl.printText(MessageFormat.format((String) properties.get("print.message"), randomString));
					printServiceRXTXImpl.printText(MessageFormat.format((String) properties.get("print.message"), randomString));

					// show products carousel screen
					prodLayout = new ProdLayout(existingStage, videoVboxProducts,
							weatherVboxProducts, productsListVBox, properties, proxy);
					productsListVBoxOne = prodLayout.showProductsList(existingStage, 1);
					productsListVBoxTwo = prodLayout.showProductsList(existingStage, 2);
					productsListVBox.getChildren().addAll(productsListVBoxOne, productsListVBoxTwo);
					
					// Show Products Stage
					new SvmController().showPrimaryStageProductsCarousel(existingStage,
							videoVboxProducts, productsListVBox, weatherVboxProducts);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		noPrintBtn.setText(properties.getProperty("no"));
		noPrintBtn.setId(SvmConstants.RECORD_SALES);
		noPrintBtn.setPrefWidth(SvmController.getWidth() / 3);
		noPrintBtn.setPrefHeight(SvmController.getHeightMiddle() / 7);
		noPrintBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					VBox productsListVBoxOne = new VBox();
					VBox productsListVBoxTwo = new VBox();
					
					// show products carousel screen
					prodLayout = new ProdLayout(existingStage, videoVboxProducts, weatherVboxProducts,
							productsListVBox, properties, proxy);
					productsListVBoxOne = prodLayout.showProductsList(existingStage, 1);
					productsListVBoxTwo = prodLayout.showProductsList(existingStage, 2);
					productsListVBox.getChildren().addAll(productsListVBoxOne, productsListVBoxTwo);
					
					// Show Products Stage
					new SvmController().showPrimaryStageProductsCarousel(existingStage,
							videoVboxProducts, productsListVBox, weatherVboxProducts);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		hBox.setPadding(new Insets(SvmController.getHeightMiddle() / 8, 0, 0, 0));
		hBox.getChildren().addAll(printBtn, noPrintBtn);

		// Home navigation
		HBox homeViewHBox = new HBox();
		homeViewHBox.setAlignment(Pos.TOP_LEFT);
		ImageView homeView = new ImageView(
				new Image(new File(properties.getProperty("home.navigation.path")).toURI().toString()));
		homeView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				SvmController.setAddHomeBtn(false);
				SvmController.setHomeBtnClicked(true);
				SvmController.setFirstTime(0);
				SvmController.setFrDone(false);

				new SvmController().showVideoWeatherAndProductsScreen(existingStage);
			}
		});
		homeViewHBox.getChildren().add(homeView);
				
		messageVboxProducts.setAlignment(Pos.TOP_CENTER);
		messageVboxProducts.getChildren().addAll(homeViewHBox, textMessage, hBox);

		new SvmController().showPrimaryStageGreetings(existingStage, videoVboxProducts, messageVboxProducts,
				weatherVboxProducts, keyBoardPopup);
	}

	public void showPleaseWaitStage(String frOrProd) {
		this.frOrProd = frOrProd;
		DropShadow ds = new DropShadow();
		ds.setOffsetY(1.5f);
		ds.setColor(Color.color(0.4f, 0.4f, 0.4f));

		Label loadingMessage = new Label();
		loadingMessage.setText(properties.getProperty("loading.please.wait"));
		loadingMessage.setEffect(ds);
		loadingMessage.setCache(true);
		loadingMessage.setMinHeight(SvmController.getHeightMiddle() / 7);
		loadingMessage.setMinWidth(SvmController.getWidth() / 2);
		loadingMessage.setTextFill(Color.BLACK);
		loadingMessage.setFont(Font.font(SvmConstants.FONT_GABRIOLA, FontWeight.BOLD, 36));

		ProgressBar bar = new ProgressBar(0);
//		bar.progressProperty().bind(fDRTask.progressProperty());
		bar.setMaxSize(SvmController.getWidth() / 2, SvmController.getHeightMiddle() / 10);
		
		ColorAdjust adjust = new ColorAdjust();
		adjust.setHue(0.9);
		bar.setEffect(adjust);
		bar.setCache(true);
		bar.setCacheShape(true);
		bar.setCacheHint(CacheHint.SPEED);

		VBox pleaseWaitTilePane = new VBox();
		pleaseWaitTilePane.setPadding(new Insets(SvmController.getHeightMiddle() / 3, 0, SvmController.getHeightMiddle() / 2, SvmController.getWidth() / 3));
		pleaseWaitTilePane.getChildren().addAll(loadingMessage, bar);

//		new Thread(fDRTask).start();
		
		// start progress bar thread
		startProgressThread(frOrProd, bar, SvmController.getWidth(), SvmController.getHeightMiddle());
		
		VBox messageVbox = new VBox(8);
		svmControllerParams = new SvmController(existingStage, videoVboxProducts,
				weatherVboxProducts, messageVbox);		
		svmControllerParams.createVideoLayout(videoVboxProducts);
		
		new SvmController(existingStage, videoVboxProducts, weatherVboxProducts, messageVbox)
				.showPleaseWaitPrimaryStage(existingStage, videoVboxProducts, pleaseWaitTilePane, weatherVboxProducts);
	}
	
	/*Task fDRTask = new Task<Void>() {
		KeyBoardPopup keyBoardPopup = KeyBoardPopupBuilder.create().initLocale(Locale.ENGLISH).build();
		
		@Override
		public Void call() throws Exception {
			for (int i = 0; i < 4; i++) {
				if (frOrProd.equals(SvmConstants.FR)) {
					if (i == 1) {
						VBox messageVbox = new VBox(8);
						svmControllerParams = new SvmController(existingStage, videoVboxProducts,
								weatherVboxProducts, messageVbox);
						
						String imageName = svmControllerParams.startFDR(bar);
					}
				}
				updateProgress(i, 4);
			}
			
			return null;
		}
			
	};*/
	
	public ProgressBarThread startProgressThread(String frOrProd, ProgressBar bar, double width, double heightMiddle) {
		try {
			progressBarThread = new GreetingsLayout().new ProgressBarThread(frOrProd, bar, width, heightMiddle);
			progressBarThread.start();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		return progressBarThread;
	}
	
	public class ProgressBarThread extends Thread {
		String imageName;
		ProgressBar bar;
		String frOrProd;
		double width;
		double heightMiddle;

		KeyBoardPopup keyBoardPopup = KeyBoardPopupBuilder.create().initLocale(Locale.ENGLISH).build();
		int count = 1;
		VBox messageVbox;

		public ProgressBarThread(String frOrProd, ProgressBar bar, double width, double heightMiddle) {
			this.bar = bar;
			this.frOrProd = frOrProd;
			this.width = width;
			this.heightMiddle = heightMiddle;
		}

		public void run() {
			for (int i = 1; i <= 4; i++) {
				try {
					if (frOrProd.equals(SvmConstants.FR)) {
						if (count == 1 && isProgressThread() == false) {
							messageVbox = new VBox(8);
							svmControllerParams = new SvmController(existingStage, videoVboxProducts,
									weatherVboxProducts, messageVbox);

							// BetaFace Implementation
							// imgNameAndFaceId = svmControllerParams.startFDR();

							// Microsoft Cognitive Services Face Implementation
							imageName = svmControllerParams.startFDR(bar);

							count++;
							fRCompleted = true;
						}
					}
					Thread.sleep(new Random().nextInt(1000));
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}

				final double progress = i * 0.25;

				// update ProgressIndicator on FX thread
				Platform.runLater(new Runnable() {
					public void run() {
						try {
							if (frOrProd.equals(SvmConstants.PROD)) {
								if (count == 1 && isProgressThread() == false) {
									VBox productsListVBoxOne = new VBox();
									VBox productsListVBoxTwo = new VBox();

									bar.setProgress(4 * 0.25);

									// show products carousel screen
									prodLayout = new ProdLayout(existingStage, videoVboxProducts, weatherVboxProducts,
											productsListVBox, properties, proxy);
									productsListVBoxOne = prodLayout.showProductsList(existingStage, 1);
									productsListVBoxTwo = prodLayout.showProductsList(existingStage, 2);
									productsListVBox.getChildren().addAll(productsListVBoxOne, productsListVBoxTwo);

									count++;
								}
							}

							if (frOrProd.equals(SvmConstants.FR) && progress >= 1.00 && fRCompleted) {
								if (isProgressThread() == false) {
									bar.setProgress(4 * 0.25);

									// BetaFace Implementation
									// messageVbox = svmControllerParams.showGreetingsPage(imgNameAndFaceId);

									// Microsoft Cognitive Services Face Implementation
									messageVbox = svmControllerParams.showGreetingsPage(imageName);

									// Show Primary Stage
									svmControllerParams.showPrimaryStageGreetings(existingStage, videoVboxProducts,
											messageVbox, weatherVboxProducts, keyBoardPopup);
								}
							} else if (frOrProd.equals(SvmConstants.PROD) && progress >= 1.00) {
								if (isProgressThread() == false) {
									bar.setProgress(4 * 0.25);

									// Show Products Stage
									new SvmController().showPrimaryStageProductsCarousel(existingStage,
											videoVboxProducts, productsListVBox, weatherVboxProducts);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		}
	}

}
