package org.wipro.svm.view;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.wipro.svm.carousel.CarouselOne;
import org.wipro.svm.carousel.CarouselTwo;
import org.wipro.svm.controller.SvmController;
import org.wipro.svm.controller.SvmController.ReadThread;
import org.wipro.svm.model.ProductDetails;
import org.wipro.svm.service.impl.SocketAPIServiceImpl;
import org.wipro.svm.utils.SVMStringUtils;
import org.wipro.svm.utils.SimplePlayer;
import org.wipro.svm.utils.SvmConstants;
import org.wipro.svm.virtual.keyboard.control.KeyBoardPopup;
import org.wipro.svm.virtual.keyboard.control.KeyBoardPopupBuilder;
import org.wipro.svm.virtual.keyboard.control.VkProperties;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ProdLayout implements VkProperties {

	private final static Logger logger = Logger.getLogger(ProdLayout.class);

	HttpHost proxy;
	private static String userName = SvmConstants.EMPTY_STRING;
	private static TreeCell<ImageHandle> productCarouselCell = new TreeCell<ImageHandle>();
	private static SvmController svmController = new SvmController();
	ReadThread readThread;
	private static CounterThread counterThread;
	public static boolean stopCounter = false;
	
	Map<String, Image> favoritesImages = null;
	Map<String, Image> suggessionsImages = null;
	private static VBox productsVBox = new VBox();
	private static VBox weatherVboxProducts = new VBox(8);
	private static StackPane videoVboxProducts = new StackPane();
	private static Stage existingStage = new Stage();
	private static Properties properties = new Properties();
	StringProperty prodUnitPrice = new SimpleStringProperty();
	StringProperty prodAddOnServicePriceProp = new SimpleStringProperty();
	public static StringProperty counterProp = new SimpleStringProperty();
	private static StringProperty prodUnitPriceStatic = new SimpleStringProperty();
	private static Integer qtyToDispense = new Integer(1);
	String productUnitPrice = null;
	SVMStringUtils svmStrUtils = new SVMStringUtils();
	SocketAPIServiceImpl socketAPI = new SocketAPIServiceImpl();
	private static ProductDetails productDetails = new ProductDetails();
	KeyBoardPopup keyBoardPopup = KeyBoardPopupBuilder.create().initLocale(Locale.ENGLISH).build();
	public static int tempAddOnPrice;
	public static double prodAddOnServicePrice = 0;

	public static ProductDetails getProductDetails() {
		return productDetails;
	}

	public static void setProductDetails(ProductDetails productDetails) {
		ProdLayout.productDetails = productDetails;
	}

	public static CounterThread getCounterThread() {
		return counterThread;
	}

	public static void setCounterThread(CounterThread counterThread) {
		ProdLayout.counterThread = counterThread;
	}

	public ProdLayout() {
	}

	public ProdLayout(Stage primaryStage, StackPane videoVbox, VBox weatherVbox, VBox productsVBox,
			Properties props, HttpHost proxy) {
		existingStage = primaryStage;
		videoVboxProducts = videoVbox;
		weatherVboxProducts = weatherVbox;
		productsVBox = productsVBox;
		properties = props;
		this.proxy = proxy;
	}

	public VBox showProductsList(Stage existStage, int carouselNo)
			throws Exception {

		VBox productsListVBox = new VBox();
		File dir = new File(properties.getProperty("products.images.path"));

		Map<String, Image> images = new HashMap<String, Image>();
		int fileCount = 0;

		for (File file : dir.listFiles()) {
			if (file.isFile()) {
				images.put(file.getName(), new Image(new FileInputStream(file), 800, 600, true, true));
				if (fileCount++ > 50) {
					break;
				}
			}
		}

		if (carouselNo == 1) {
			final CarouselOne<ImageHandle> carouselOne = new CarouselOne<>();
			carouselOne.setPrefSize(SvmController.getWidth(), SvmController.getHeightMiddle() / 2);

			TreeItem<ImageHandle> rootOne = new TreeItem<>();

			carouselOne.setRoot(rootOne);
			carouselOne.setShowRoot(false);
			carouselOne.setCache(true);
			carouselOne.setCacheShape(true);
			carouselOne.setCacheHint(CacheHint.SPEED);

			for (Map.Entry<String, Image> entry : images.entrySet()) {
				ImageView imageView = new ImageView();
				imageView.setId(entry.getKey());
				imageView.setImage(entry.getValue());

				ImageView priceTagView = new ImageView(
						new Image(new File(properties.getProperty("price.tag.path")).toURI().toString()));
				priceTagView.setFitHeight(150);
				priceTagView.setFitWidth(150);

				rootOne.getChildren().add(new TreeItem<>(new ImageHandle(priceTagView, imageView)));
			}

			carouselOne.setCellFactory(new Callback<TreeView<ImageHandle>, TreeCell<ImageHandle>>() {
				@Override
				public TreeCell<ImageHandle> call(final TreeView<ImageHandle> carouselOne) {
					TreeCell<ImageHandle> carouselCell = new TreeCell<ImageHandle>() {
						@Override
						protected void updateItem(ImageHandle item, boolean empty) {
							super.updateItem(item, empty);

							if (!empty) {
								StringProperty productUnitPrice = new SimpleStringProperty();
								Label priceText = new Label();
								
								ImageView imageView = item.getImage();
								imageView.setPreserveRatio(true);

								ImageView priceTagImageView = item.getPriceImage();
								priceTagImageView.setFitWidth(200);
								priceTagImageView.setPreserveRatio(true);

								if (getAvailableProductStockQty(imageView) <= 0) {
									imageView.setOpacity(0.30);
									productUnitPrice.setValue(SvmConstants.SOLD_OUT);
									priceText.setTextFill(Color.FIREBRICK);
								} else {
									productUnitPrice = getpriceForProduct(SvmController.getProdIdPriceMap(),
											imageView.getId().split(SvmConstants.SPLIT_PATTERN_DOT)[0]);
									priceText.setTextFill(Color.CHARTREUSE);
								}
								priceText.setText(productUnitPrice.getValue());
								priceText.setAlignment(Pos.CENTER);
								priceText.setPadding(new Insets(0, 0, 10, 0));
								priceText.setFont(Font.font(SvmConstants.FONT_RUPEE_FORADIAN, FontWeight.BOLD, 40));

								StackPane stackPane = new StackPane();
								stackPane.setId(imageView.getId());
								stackPane.getChildren().addAll(imageView, priceTagImageView, priceText);
								stackPane.setAlignment(Pos.BOTTOM_CENTER);

								setGraphic(stackPane);
							} else {
								setGraphic(null);
							}
						}
					};

					carouselOne.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent event) {
							if (!SvmController.isFrDone()) {
								// Start Face Detection & Recognition
								SvmController.setFrDone(true);

								// show loading... splash screen
								VBox messageVbox = new VBox(8);
								String frOrProd = SvmConstants.FR;

								new PlayAudioThread(properties.getProperty("new.user.greeting.message.path")).start();

								new GreetingsLayout(existingStage, videoVboxProducts, weatherVboxProducts, messageVbox,
										properties, proxy).showPleaseWaitStage(frOrProd);
							}
						}
					});

					carouselCell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
					carouselCell.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
						@SuppressWarnings("static-access")
						@Override
						public void handle(MouseEvent event) {
							if (SvmController.isFrDone()) {
								// Show Product Purchase page
								String productOrAddOn = SvmConstants.PRODUCT;
								productCarouselCell = carouselCell;
								showProductNutritionPage(carouselCell, null, productOrAddOn, SvmConstants.ALL);
							}
						}
					});
					
					carouselCell.setCache(true);
					carouselCell.setCacheShape(true);
					carouselCell.setCacheHint(CacheHint.SPEED);
					return carouselCell;
				}
			});

			if (SvmController.isAddHomeBtn()) {
				// Home navigation
				VBox homeHBox = new VBox();
				ImageView homeView = new ImageView(
						new Image(new File(properties.getProperty("home.navigation.path")).toURI().toString()));
				homeHBox.setAlignment(Pos.TOP_LEFT);
				homeHBox.getChildren().add(homeView);
				homeHBox.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						SvmController.setAddHomeBtn(false);
						SvmController.setHomeBtnClicked(true);
						SvmController.setFirstTime(0);
						SvmController.setFrDone(false);

						svmController.showVideoWeatherAndProductsScreen(existingStage);
					}
				});
				productsListVBox.getChildren().addAll(homeHBox, carouselOne);
			} else {
				productsListVBox.getChildren().add(carouselOne);
			}
			
//			productsTilePane = new TilePane();
//			productsTilePane.getChildren().addAll(carouselOne);
		} else if (carouselNo == 2) {
			// sort products
			TreeMap<String, Image> sortedImages = new TreeMap<>(images);

			final CarouselTwo<ImageHandle> carouselTwo = new CarouselTwo<>();
			carouselTwo.setPrefSize(SvmController.getWidth(), SvmController.getHeightMiddle() / 2);

			TreeItem<ImageHandle> rootTwo = new TreeItem<>();
			carouselTwo.setRoot(rootTwo);
			carouselTwo.setShowRoot(false);
			carouselTwo.setCache(true);
			carouselTwo.setCacheShape(true);
			carouselTwo.setCacheHint(CacheHint.SPEED);

			for (Map.Entry<String, Image> entry : sortedImages.entrySet()) {
				ImageView imageView = new ImageView();
				imageView.setId(entry.getKey());
				imageView.setImage(entry.getValue());

				ImageView priceTagView = new ImageView(
						new Image(new File(properties.getProperty("price.tag.path")).toURI().toString()));
				priceTagView.setFitHeight(150);
				priceTagView.setFitWidth(150);

				rootTwo.getChildren().add(new TreeItem<>(new ImageHandle(priceTagView, imageView)));
			}

			carouselTwo.setCellFactory(new Callback<TreeView<ImageHandle>, TreeCell<ImageHandle>>() {
				@Override
				public TreeCell<ImageHandle> call(final TreeView<ImageHandle> carouselTwo) {
					TreeCell<ImageHandle> carouselCell = new TreeCell<ImageHandle>() {
						@Override
						protected void updateItem(ImageHandle item, boolean empty) {
							super.updateItem(item, empty);

							if (!empty) {
								StringProperty productUnitPrice = new SimpleStringProperty();
								Label priceText = new Label();
								
								ImageView imageView = item.getImage();
								imageView.setPreserveRatio(true);

								ImageView priceTagImageView = item.getPriceImage();
								priceTagImageView.setFitWidth(200);
								priceTagImageView.setPreserveRatio(true);

								if (getAvailableProductStockQty(imageView) <= 0) {
									imageView.setOpacity(0.30);
									productUnitPrice.setValue(SvmConstants.SOLD_OUT);
									priceText.setTextFill(Color.FIREBRICK);
								} else {
									productUnitPrice = getpriceForProduct(SvmController.getProdIdPriceMap(),
											imageView.getId().split(SvmConstants.SPLIT_PATTERN_DOT)[0]);
									priceText.setTextFill(Color.CHARTREUSE);
								}
								priceText.setText(productUnitPrice.getValue());
								priceText.setAlignment(Pos.CENTER);
								priceText.setPadding(new Insets(0, 0, 10, 0));
								priceText.setFont(Font.font(SvmConstants.FONT_RUPEE_FORADIAN, FontWeight.BOLD, 40));

								StackPane stackPane = new StackPane();
								stackPane.setId(imageView.getId());
								stackPane.getChildren().addAll(imageView, priceTagImageView, priceText);
								stackPane.setAlignment(Pos.BOTTOM_CENTER);

								setGraphic(stackPane);
							} else {
								setGraphic(null);
							}
						}
					};

					carouselTwo.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent event) {
							if (!SvmController.isFrDone()) {
								// Start Face Detection & Recognition
								SvmController.setFrDone(true);

								// show loading... splash screen
								VBox messageVbox = new VBox(8);
								String frOrProd = SvmConstants.FR;
								
								new PlayAudioThread(properties.getProperty("new.user.greeting.message.path")).start();

								new GreetingsLayout(existingStage, videoVboxProducts, weatherVboxProducts, messageVbox,
										properties, proxy).showPleaseWaitStage(frOrProd);
							}
						}
					});

					carouselCell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
					carouselCell.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
						@SuppressWarnings("static-access")
						@Override
						public void handle(MouseEvent event) {
							if (SvmController.isFrDone()) {
								// Show Product Purchase page
								String productOrAddOn = SvmConstants.PRODUCT;
								productCarouselCell = carouselCell;
								showProductNutritionPage(carouselCell, null, productOrAddOn, SvmConstants.ALL);
							}
						}
					});

					carouselCell.setCache(true);
					carouselCell.setCacheShape(true);
					carouselCell.setCacheHint(CacheHint.SPEED);
					return carouselCell;
				}
			});

			productsListVBox.getChildren().addAll(carouselTwo);
//			productsTilePane = new TilePane();
//			productsTilePane.getChildren().add(carouselTwo);
		}
		
		svmStrUtils.applyCachingPropToPane(productsListVBox);
		return productsListVBox;
	}
	
	public VBox showMyFavoritesProductsList(Stage existStage)
			throws Exception {
		if (favoritesImages == null) {
			File dir = new File(properties.getProperty("products.images.path"));

			favoritesImages = new HashMap<String, Image>();
			int fileCount = 0;

			for (File file : dir.listFiles()) {
				if (file.isFile() && (fileCount % 2) == 0) {
					favoritesImages.put(file.getName(), new Image(new FileInputStream(file), 800, 600, true, true));
					fileCount++;
					if (favoritesImages.size() >= 5) {
						break;
					}
				} else {
					fileCount++;
				}
			}
		}

		final CarouselOne<ImageHandle> carouselOne = new CarouselOne<>();
		carouselOne.setPrefWidth(SvmController.getWidth());
		carouselOne.setPrefHeight((SvmController.getHeightMiddle() / 2));

		TreeItem<ImageHandle> rootOne = new TreeItem<>();
		carouselOne.setRoot(rootOne);
		carouselOne.setShowRoot(false);
		carouselOne.setCache(true);
		carouselOne.setCacheShape(true);
		carouselOne.setCacheHint(CacheHint.SPEED);

		for (Map.Entry<String, Image> entry : favoritesImages.entrySet()) {
			ImageView imageView = new ImageView();
			imageView.setId(entry.getKey());
			imageView.setImage(entry.getValue());

			ImageView priceTagView = new ImageView(
					new Image(new File(properties.getProperty("price.tag.path")).toURI().toString()));
			priceTagView.setFitHeight(150);
			priceTagView.setFitWidth(150);

			rootOne.getChildren().add(new TreeItem<>(new ImageHandle(priceTagView, imageView)));
		}

		carouselOne.setCellFactory(new Callback<TreeView<ImageHandle>, TreeCell<ImageHandle>>() {
			@Override
			public TreeCell<ImageHandle> call(final TreeView<ImageHandle> carouselOne) {
				TreeCell<ImageHandle> carouselCell = new TreeCell<ImageHandle>() {
					@Override
					protected void updateItem(ImageHandle item, boolean empty) {
						super.updateItem(item, empty);

						if (!empty) {
							StringProperty productUnitPrice = new SimpleStringProperty();
							Label priceText = new Label();
							
							ImageView imageView = item.getImage();
							imageView.setPreserveRatio(true);

							ImageView priceTagImageView = item.getPriceImage();
							priceTagImageView.setFitWidth(200);
							priceTagImageView.setPreserveRatio(true);

							if (getAvailableProductStockQty(imageView) <= 0) {
								imageView.setOpacity(0.30);
								productUnitPrice.setValue(SvmConstants.SOLD_OUT);
								priceText.setTextFill(Color.FIREBRICK);
							} else {
								productUnitPrice = getpriceForProduct(SvmController.getProdIdPriceMap(),
										imageView.getId().split(SvmConstants.SPLIT_PATTERN_DOT)[0]);
								priceText.setTextFill(Color.CHARTREUSE);
							}
							priceText.setText(productUnitPrice.getValue());
							priceText.setAlignment(Pos.CENTER);
							priceText.setPadding(new Insets(0, 0, 10, 0));
							priceText.setFont(Font.font(SvmConstants.FONT_RUPEE_FORADIAN, FontWeight.BOLD, 40));

							StackPane stackPane = new StackPane();
							stackPane.setId(imageView.getId());
							stackPane.getChildren().addAll(imageView, priceTagImageView, priceText);
							stackPane.setAlignment(Pos.BOTTOM_CENTER);

							setGraphic(stackPane);
						} else {
							setGraphic(null);
						}
					}
				};

				carouselOne.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (!SvmController.isFrDone()) {
							// Start Face Detection & Recognition
							SvmController.setFrDone(true);

							// show loading... splash screen
							VBox messageVbox = new VBox(8);
							String frOrProd = SvmConstants.FR;

							new PlayAudioThread(properties.getProperty("new.user.greeting.message.path")).start();

							new GreetingsLayout(existingStage, videoVboxProducts, weatherVboxProducts, messageVbox,
									properties, proxy).showPleaseWaitStage(frOrProd);
						}
					}
				});

				carouselCell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				carouselCell.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
					@SuppressWarnings("static-access")
					@Override
					public void handle(MouseEvent event) {
						if (SvmController.isFrDone()) {
							// Show Product Purchase page
							String productOrAddOn = SvmConstants.PRODUCT;
							productCarouselCell = carouselCell;
							showProductNutritionPage(productCarouselCell, null, productOrAddOn, SvmConstants.FAVORITES);
						}
					}
				});

				carouselCell.setCache(true);
				carouselCell.setCacheShape(true);
				carouselCell.setCacheHint(CacheHint.SPEED);
				return carouselCell;
			}
		});

		productsVBox = new VBox();
		HBox suggestionsStillHBox = new HBox();
		suggestionsStillHBox.setAlignment(Pos.CENTER);
		suggestionsStillHBox.setPadding(new Insets(SvmController.getHeightMiddle() / 4, 0, 0, 0));
		
		Button suggestionsStillBtn = new Button();
		suggestionsStillBtn.setText(properties.getProperty("show.more.products"));
		suggestionsStillBtn.setId(SvmConstants.RECORD_SALES);
		suggestionsStillBtn.setPrefWidth(SvmController.getWidth() / 2);
		suggestionsStillBtn.setPrefHeight(SvmController.getHeightMiddle() / 6);
		suggestionsStillBtn.autosize();
		suggestionsStillBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					VBox productsListVBox = new VBox();
					VBox productsListVBoxOne = new VBox();
					VBox productsListVBoxTwo = new VBox();
					
					// show products carousel screen
					productsListVBoxOne = showProductsList(existingStage, 1);
					productsListVBoxTwo = showProductsList(existingStage, 2);
					
					productsListVBox.getChildren().addAll(productsListVBoxOne, productsListVBoxTwo);
					
					// Show Products Stage
					new SvmController().showPrimaryStageProductsCarousel(existingStage,
							videoVboxProducts, productsListVBox, weatherVboxProducts);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		suggestionsStillHBox.getChildren().add(suggestionsStillBtn);
		
		productsVBox.getChildren().addAll(carouselOne, suggestionsStillHBox);

		svmStrUtils.applyCachingPropToPane(productsVBox);
		
		return productsVBox;
	}
	
	public VBox showSuggestionsProductsList(Stage existStage)
			throws Exception {
		if (suggessionsImages == null) {
			File dir = new File(properties.getProperty("products.images.path"));

			suggessionsImages = new HashMap<String, Image>();
			int fileCount = 0;

			for (File file : dir.listFiles()) {
				if (file.isFile() && (fileCount % 2) != 0) {
					suggessionsImages.put(file.getName(), new Image(new FileInputStream(file), 800, 600, true, true));
					fileCount++;
					if (suggessionsImages.size() >= 5) {
						break;
					}
				} else {
					fileCount++;
				}
			}
		}

		final CarouselOne<ImageHandle> carouselOne = new CarouselOne<>();
		carouselOne.setPrefWidth(SvmController.getWidth());
		carouselOne.setPrefHeight((SvmController.getHeightMiddle() / 2));

		TreeItem<ImageHandle> rootOne = new TreeItem<>();
		carouselOne.setRoot(rootOne);
		carouselOne.setShowRoot(false);
		carouselOne.setCache(true);
		carouselOne.setCacheShape(true);
		carouselOne.setCacheHint(CacheHint.SPEED);

		for (Map.Entry<String, Image> entry : suggessionsImages.entrySet()) {
			ImageView imageView = new ImageView();
			imageView.setId(entry.getKey());
			imageView.setImage(entry.getValue());

			ImageView priceTagView = new ImageView(
					new Image(new File(properties.getProperty("price.tag.path")).toURI().toString()));
			priceTagView.setFitHeight(150);
			priceTagView.setFitWidth(150);

			rootOne.getChildren().add(new TreeItem<>(new ImageHandle(priceTagView, imageView)));
		}

		carouselOne.setCellFactory(new Callback<TreeView<ImageHandle>, TreeCell<ImageHandle>>() {
			@Override
			public TreeCell<ImageHandle> call(final TreeView<ImageHandle> carouselOne) {
				TreeCell<ImageHandle> carouselCell = new TreeCell<ImageHandle>() {
					@Override
					protected void updateItem(ImageHandle item, boolean empty) {
						super.updateItem(item, empty);

						if (!empty) {
							StringProperty productUnitPrice = new SimpleStringProperty();
							Label priceText = new Label();
							
							ImageView imageView = item.getImage();
							imageView.setPreserveRatio(true);

							ImageView priceTagImageView = item.getPriceImage();
							priceTagImageView.setFitWidth(200);
							priceTagImageView.setPreserveRatio(true);

							if (getAvailableProductStockQty(imageView) <= 0) {
								imageView.setOpacity(0.30);
								productUnitPrice.setValue(SvmConstants.SOLD_OUT);
								priceText.setTextFill(Color.FIREBRICK);
							} else {
								productUnitPrice = getpriceForProduct(SvmController.getProdIdPriceMap(),
										imageView.getId().split(SvmConstants.SPLIT_PATTERN_DOT)[0]);
								priceText.setTextFill(Color.CHARTREUSE);
							}
							priceText.setText(productUnitPrice.getValue());
							priceText.setAlignment(Pos.CENTER);
							priceText.setPadding(new Insets(0, 0, 10, 0));
							priceText.setFont(Font.font(SvmConstants.FONT_RUPEE_FORADIAN, FontWeight.BOLD, 40));

							StackPane stackPane = new StackPane();
							stackPane.setId(imageView.getId());
							stackPane.getChildren().addAll(imageView, priceTagImageView, priceText);
							stackPane.setAlignment(Pos.BOTTOM_CENTER);

							setGraphic(stackPane);
						} else {
							setGraphic(null);
						}
					}
				};

				carouselOne.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (!SvmController.isFrDone()) {
							// Start Face Detection & Recognition
							SvmController.setFrDone(true);

							// show loading... splash screen
							VBox messageVbox = new VBox(8);
							String frOrProd = SvmConstants.FR;

							new PlayAudioThread(properties.getProperty("new.user.greeting.message.path")).start();

							new GreetingsLayout(existingStage, videoVboxProducts, weatherVboxProducts, messageVbox,
									properties, proxy).showPleaseWaitStage(frOrProd);
						}
					}
				});

				carouselCell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				carouselCell.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
					@SuppressWarnings("static-access")
					@Override
					public void handle(MouseEvent event) {
						if (SvmController.isFrDone()) {
							// Show Product Purchase page
							String productOrAddOn = SvmConstants.PRODUCT;
							productCarouselCell = carouselCell;
							showProductNutritionPage(productCarouselCell, null, productOrAddOn, SvmConstants.SUGGESTIONS);
						}
					}
				});

				carouselCell.setCache(true);
				carouselCell.setCacheShape(true);
				carouselCell.setCacheHint(CacheHint.SPEED);
				return carouselCell;
			}
		});

		productsVBox = new VBox();
		HBox suggestionsStillHBox = new HBox();
		suggestionsStillHBox.setAlignment(Pos.CENTER);
		suggestionsStillHBox.setPadding(new Insets(SvmController.getHeightMiddle() / 4, 0, 0, 0));
		
		Button suggestionsStillBtn = new Button();
		suggestionsStillBtn.setText(properties.getProperty("show.more.products"));
		suggestionsStillBtn.setId(SvmConstants.RECORD_SALES);
		suggestionsStillBtn.setPrefWidth(SvmController.getWidth() / 2);
		suggestionsStillBtn.setPrefHeight(SvmController.getHeightMiddle() / 6);
		suggestionsStillBtn.autosize();
		suggestionsStillBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					VBox productsListVBox = new VBox();
					VBox productsListVBoxOne = new VBox();
					VBox productsListVBoxTwo = new VBox();
					
					// show products carousel screen
					productsListVBoxOne = showProductsList(existingStage, 1);
					productsListVBoxTwo = showProductsList(existingStage, 2);
					productsListVBox.getChildren().addAll(productsListVBoxOne, productsListVBoxTwo);
					
					// Show Products Stage
					new SvmController().showPrimaryStageProductsCarousel(existingStage,
							videoVboxProducts, productsListVBox, weatherVboxProducts);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		suggestionsStillHBox.getChildren().add(suggestionsStillBtn);
		
		productsVBox.getChildren().addAll(carouselOne, suggestionsStillHBox);

		svmStrUtils.applyCachingPropToPane(productsVBox);
		
		return productsVBox;
	}
	
	private StringProperty getpriceForProduct(Map<String, String> prodIdPriceMap, String productId) {
		StringProperty productUnitPrice = new SimpleStringProperty();
		
			Iterator itr = prodIdPriceMap.entrySet().iterator();
		    while (itr.hasNext()) {
		        Map.Entry pair = (Map.Entry)itr.next();
		        if (productId.equals(pair.getKey().toString())) {
		        	productUnitPrice.setValue(pair.getValue().toString());
		        	break;
		        }
		    }
	    
		return productUnitPrice;
	}

	private void showProductNutritionPage(TreeCell<ImageHandle> productCarouselCell, TreeCell<ImageHandle> carouselCell,
			String productOrAddOn, String productsListType) {

		boolean productAvailabile = false;
		String[] imageName;
		BorderPane productNutritionPane = new BorderPane();
		productNutritionPane.setPadding(new Insets(10, 20, 10, 10));

		HBox priceQtyMainHBox = new HBox(15);
		priceQtyMainHBox.setPadding(new Insets(0, 0, 30, 0));
		priceQtyMainHBox.setPrefSize(SvmController.getWidth(), SvmController.getHeightMiddle() / 7);
		
		HBox proceedToBuyBtnHBox = new HBox(50);
		productAvailabile = createPriceQuantityLayout(priceQtyMainHBox, proceedToBuyBtnHBox, productCarouselCell, carouselCell,
				productAvailabile, productOrAddOn, productsListType);
		
		if (productAvailabile) {
			imageName = productCarouselCell.getChildrenUnmodifiable().toString().split(SvmConstants.SPLIT_PATTERN_ONE);
			ImageView productView = getImageView(imageName[1]);
			productView.setFitHeight(500);
			productView.setFitWidth(300);

			if (productOrAddOn.equals(SvmConstants.PRODUCT)) {
				ImageView nutritionView = new ImageView(
						new Image(new File(properties.getProperty("nutrition.details.path")).toURI().toString()));
				
				productNutritionPane.setTop(priceQtyMainHBox);
				productNutritionPane.setLeft(productView);
				productNutritionPane.setRight(nutritionView);
				productNutritionPane.setBottom(proceedToBuyBtnHBox);

			} else if (productOrAddOn.equals(SvmConstants.ADDON)) {
				HBox plusHBox = new HBox();
				plusHBox.setPadding(
						new Insets(SvmController.getHeightMiddle() / 4, 0, 0, SvmController.getWidth() / 8));

				ImageView plusView = new ImageView(
						new Image(new File(properties.getProperty("plus.path")).toURI().toString()));
				plusView.setFitHeight(50);
				plusView.setFitWidth(50);
				plusHBox.getChildren().add(plusView);

				String[] addOnImageName = carouselCell.getChildrenUnmodifiable().toString().split(SvmConstants.SPLIT_PATTERN_ONE);
				ImageView addOnServiceView = getAddOnImageView(addOnImageName[1]);
				addOnServiceView.setFitHeight(450);
				addOnServiceView.setFitWidth(450);

				prodAddOnServicePrice = Double.parseDouble(prodUnitPriceStatic.getValue()
						.replaceAll(SvmConstants.STRING_REPLACE_TWO, SvmConstants.EMPTY_STRING))
						+ Double.parseDouble(addOnImageName[2]);
				
				Label prodAddOnServiceLabel = new Label(properties.getProperty("total.amount") + prodAddOnServicePrice + SvmConstants.TILDA);
				prodAddOnServiceLabel.setFont(Font.font(SvmConstants.FONT_RUPEE_FORADIAN, FontWeight.LIGHT, 40));
				prodAddOnServiceLabel.setTextFill(Color.RED);

				Button backBtn = new Button();
				backBtn.setText(properties.getProperty("back"));
				backBtn.setId(SvmConstants.RECORD_SALES);
				backBtn.setPrefSize(SvmController.getWidth() / 3, SvmController.getHeightMiddle() / 7);
				backBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						try {
							VBox addOnServiceVBox = new VBox();
							VBox addOnServiceVBoxOne = new VBox();
							VBox addOnServiceVBoxTwo = new VBox();

							addOnServiceVBoxOne = showAddOnSevicesList(productCarouselCell, existingStage, 1);
							addOnServiceVBoxTwo = showAddOnSevicesList(productCarouselCell, existingStage, 2);
							
							addOnServiceVBox.getChildren().addAll(addOnServiceVBoxOne, addOnServiceVBoxTwo);
							
							new SvmController().showPrimaryStageAddOnServiceCarousel(existingStage, videoVboxProducts, addOnServiceVBox,
									weatherVboxProducts, false);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				
				Button buyButton = new Button();
				buyButton.setText(properties.getProperty("buy"));
				buyButton.setId(SvmConstants.RECORD_SALES);
				buyButton.setPrefSize(SvmController.getWidth() / 3, SvmController.getHeightMiddle() / 7);
				buyButton.setEffect(new DropShadow());
				buyButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (SvmController.getImageName().equals(SvmConstants.EMPTY_STRING)) {
							// not user
							createAddressDetailsPage();
						} else {
							// user
							createShippingConfirmationPage();
						}
					}
				});

				priceQtyMainHBox.getChildren().addAll(prodAddOnServiceLabel);
				proceedToBuyBtnHBox.setAlignment(Pos.CENTER);
				proceedToBuyBtnHBox.getChildren().addAll(backBtn, buyButton);
				
				VBox mainVBox = new VBox(10);
				mainVBox.getChildren().addAll(priceQtyMainHBox, proceedToBuyBtnHBox);
			
				// Home navigation
				VBox homeViewVBox = new VBox();
				homeViewVBox.setAlignment(Pos.TOP_LEFT);
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
				homeViewVBox.getChildren().add(homeView);
				
				productNutritionPane.setTop(homeViewVBox);
				productNutritionPane.setLeft(productView);
				productNutritionPane.setCenter(plusHBox);
				productNutritionPane.setRight(addOnServiceView);
				productNutritionPane.setBottom(mainVBox);
			}
		} else {
			imageName = productCarouselCell.getChildrenUnmodifiable().toString().split(SvmConstants.SPLIT_PATTERN_TWO);
			ImageView productView = getImageView(imageName[1]);
			productView.setFitHeight(500);
			productView.setFitWidth(300);

			ImageView soldOutView = new ImageView(
					new Image(new File(properties.getProperty("sold.out.path")).toURI().toString()));
			soldOutView.setOpacity(0.75);

			ImageView nutritionView = new ImageView(
					new Image(new File(properties.getProperty("nutrition.details.path")).toURI().toString()));

			StackPane prodSoldOutPane = new StackPane();
			prodSoldOutPane.setPadding(new Insets(0, 0, SvmController.getHeightMiddle() / 4, 0));
			prodSoldOutPane.getChildren().addAll(productView, soldOutView);

			productNutritionPane.setPadding(new Insets(SvmController.getHeightMiddle() / 5.5, 8, 10, 10));
			productNutritionPane.setLeft(prodSoldOutPane);
			productNutritionPane.setRight(nutritionView);
			productNutritionPane.setBottom(priceQtyMainHBox);
		}

		new SvmController().showPrimaryStageProductNutrition(existingStage, videoVboxProducts, productNutritionPane,
				weatherVboxProducts);
	}

	private boolean createPriceQuantityLayout(HBox priceQtyMainHBox, HBox proceedToBuyBtnHBox, TreeCell<ImageHandle> prdCarouselCell,
			TreeCell<ImageHandle> carouselCell, boolean productAvailabile,
			String productOrAddOn, String productsListType) {
		try {
			Button backBtn = new Button();
			ComboBox<Integer> comboBox = null;
			Label selectQty = new Label(properties.getProperty("quantity"));
			priceQtyMainHBox.setAlignment(Pos.CENTER);

			backBtn.setText(properties.getProperty("back"));
			backBtn.setId(SvmConstants.RECORD_SALES);
			backBtn.setPrefWidth(SvmController.getWidth() / 5);
			backBtn.setPrefHeight(SvmController.getHeightMiddle() / 7.5);
			backBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					try {
						VBox productsListTileVBox = new VBox();
						
						if (productsListType.equalsIgnoreCase(SvmConstants.ALL)) {
							VBox productsListVBoxOne = new VBox();
							VBox productsListVBoxTwo = new VBox();

							productsListVBoxOne = showProductsList(existingStage, 1);
							productsListVBoxTwo = showProductsList(existingStage, 2);
							productsListTileVBox.getChildren().addAll(productsListVBoxOne, productsListVBoxTwo);
						} else if (productsListType.equalsIgnoreCase(SvmConstants.FAVORITES)) {
							productsListTileVBox = showMyFavoritesProductsList(existingStage);
						} else if (productsListType.equalsIgnoreCase(SvmConstants.SUGGESTIONS)) {
							productsListTileVBox = showSuggestionsProductsList(existingStage);
						}

						new SvmController().showPrimaryStageProductsCarousel(existingStage, videoVboxProducts,
								productsListTileVBox, weatherVboxProducts);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			// get available product qty
			String[] imageName = prdCarouselCell.getChildrenUnmodifiable().toString().split(SvmConstants.SPLIT_PATTERN_TWO);
			
			readThread = svmController.startReadThread();
			
			socketAPI.getProductBalance(SvmController.getClient(), imageName[1].split(SvmConstants.SPLIT_PATTERN_DOT)[0]);

			SvmController.waitForThread();
			readThread.shutdown();
			
			String respMsgProdBal = svmController.getResponseMessage();
			String prodBalance = svmStrUtils.getProductBalance(respMsgProdBal);
			
			// create qty list
			List<Integer> qtyList = new ArrayList<Integer>();
			qtyList = createQtyList(qtyList, prodBalance);

			if (qtyList != null && !qtyList.isEmpty()) {
				if (productOrAddOn.equals(SvmConstants.PRODUCT)) {
					prodUnitPrice = getpriceForProduct(SvmController.getProdIdPriceMap(), imageName[1].split("\\.")[0]);
					productUnitPrice = prodUnitPrice.getValue();

					selectQty.setFont(Font.font(SvmConstants.FONT_ARIAL_BLACK, FontWeight.BOLD, 40));
					selectQty.setTextFill(Color.BROWN);

					ObservableList<Integer> qtyOptions = FXCollections.observableArrayList(qtyList);
					comboBox = new ComboBox<Integer>(qtyOptions);
					comboBox.getStyleClass().add(SvmConstants.CUSTOM_COMBOBOX);
					comboBox.setValue(1);
					comboBox.setVisibleRowCount(5);
					comboBox.setPrefHeight(50);
					comboBox.setPrefWidth(SvmController.getWidth() / 5);
					comboBox.valueProperty().addListener(new ChangeListener<Integer>() {
						@Override
						public void changed(ObservableValue ov, Integer oldValue, Integer newValue) {
							if (newValue > 0) {
								qtyToDispense = newValue;
								String prodUnitPriceStr = updateUnitPriceAdd(String.valueOf(productUnitPrice),
										String.valueOf(newValue));
								prodUnitPrice.setValue(prodUnitPriceStr);
							}
						}
					});

					prodUnitPriceStatic = prodUnitPrice;
				}

				Label prodUnitPriceText = new Label(prodUnitPrice.getValue());
				prodUnitPriceText.textProperty().bind(prodUnitPrice);
				prodUnitPriceText.setFont(Font.font(SvmConstants.FONT_RUPEE_FORADIAN, FontWeight.LIGHT, 40));
				prodUnitPriceText.setTextFill(Color.BROWN);

				Button proceedToBuyBtn = new Button();
				proceedToBuyBtn.setText(properties.getProperty("proceed.to.buy"));
				proceedToBuyBtn.setId(SvmConstants.RECORD_SALES);
				proceedToBuyBtn.setPrefWidth(SvmController.getWidth() / 3);
				proceedToBuyBtn.setPrefHeight(SvmController.getHeightMiddle() / 7);
				proceedToBuyBtn.setEffect(new DropShadow());
				proceedToBuyBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						try {
							showAddOnConfirmationPage(prdCarouselCell);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

				if (carouselCell == null) {
					priceQtyMainHBox.getChildren().addAll(backBtn, addVerticalSeperator(), selectQty, comboBox, addVerticalSeperator(),
							prodUnitPriceText);
					proceedToBuyBtnHBox.setAlignment(Pos.CENTER);
					proceedToBuyBtnHBox.getChildren().add(proceedToBuyBtn);
				}

				productAvailabile = true;
			} else {
				Label soldOut = new Label(properties.getProperty("sold.out"));
				soldOut.setFont(Font.font(SvmConstants.FONT_ARIAL_BLACK, FontWeight.LIGHT, 40));
				soldOut.setTextFill(Color.RED);

				priceQtyMainHBox.getChildren().addAll(soldOut, backBtn);

				productAvailabile = false;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		return productAvailabile;
	}

	public Separator addVerticalSeperator() {
		Separator qtyListUnitPriceSep = new Separator();

		qtyListUnitPriceSep.setOrientation(Orientation.VERTICAL);
		qtyListUnitPriceSep.setValignment(VPos.CENTER);
		qtyListUnitPriceSep.setPrefHeight(10);

		return qtyListUnitPriceSep;
	}
	
	public Separator addHorizontalSeperator() {
		Separator horizontalSep = new Separator();

		horizontalSep.setOrientation(Orientation.HORIZONTAL);
		horizontalSep.setValignment(VPos.CENTER);
		horizontalSep.setPrefHeight(4);

		return horizontalSep;
	}

	private ImageView getImageView(String imageId) {
		final ImageView imageView = new ImageView();
		Image image = new Image(new File(properties.getProperty("products.images.path") + imageId).toURI().toString());
		imageView.setFitHeight(250);
		imageView.setFitWidth(250);
		imageView.setImage(image);

		return imageView;
	}

	private ImageView getAddOnImageView(String imageId) {
		final ImageView imageView = new ImageView();
		Image image = new Image(new File(properties.getProperty("addon.services.images.path") + imageId).toURI().toString());
		imageView.setFitHeight(250);
		imageView.setFitWidth(250);
		imageView.setImage(image);

		return imageView;
	}

	private List<Integer> createQtyList(List<Integer> qtyList, String prodBalance) {
		int prodBalInt = Integer.parseInt(prodBalance);

		if (prodBalInt > 0) {
			for (int i = 1; i <= prodBalInt; i++) {
				qtyList.add(i);
			}
		}

		return qtyList;
	}

	private String updateUnitPriceAdd(String productUnitPrice, String newValue) {
		DecimalFormat df = new DecimalFormat(SvmConstants.DECIMAL_FORMAT);

		double prodUnitPriceDouble = Double.parseDouble(productUnitPrice.replaceAll(SvmConstants.STRING_REPLACE_THREE, SvmConstants.EMPTY_STRING));
		prodUnitPriceDouble = prodUnitPriceDouble * Double.parseDouble(newValue);
		String dfProdUnitPriceStr = df.format(prodUnitPriceDouble) + SvmConstants.TILDA;

		return dfProdUnitPriceStr;
	}

	private String updateUnitPriceDeduct(String productUnitPrice, String newValue) {
		DecimalFormat df = new DecimalFormat(SvmConstants.DECIMAL_FORMAT);

		double prodUnitPriceDouble = Double.parseDouble(productUnitPrice.replaceAll(SvmConstants.STRING_REPLACE_TWO, SvmConstants.EMPTY_STRING));
		if (prodUnitPriceDouble - Double.parseDouble(newValue) > 0) {
			prodUnitPriceDouble = prodUnitPriceDouble - Double.parseDouble(newValue);
		}

		String dfProdUnitPriceStr = df.format(prodUnitPriceDouble) + SvmConstants.TILDA;

		return dfProdUnitPriceStr;
	}
	
	private static class ImageHandle {
		private ImageView priceImageView;
		private ImageView imageView;

		public ImageHandle(ImageView priceImageView, ImageView imageView) {
			this.priceImageView = priceImageView;
			this.imageView = imageView;
		}

		public ImageView getPriceImage() {
			return priceImageView;
		}

		public ImageView getImage() {
			return imageView;
		}
	}
	
	private static class ImageHandleFavorites {
		private final ImageView priceImageViewFav;
		private final ImageView imageViewFav;

		public ImageHandleFavorites(ImageView priceImageViewFav, ImageView imageViewFav) {
			this.priceImageViewFav = priceImageViewFav;
			this.imageViewFav = imageViewFav;
		}

		public ImageView getPriceImage() {
			return priceImageViewFav;
		}

		public ImageView getImage() {
			return imageViewFav;
		}
	}

	public VBox showAddOnSevicesList(TreeCell<ImageHandle> prdCarouselCell, Stage existingStage, int carouselNo)
			throws Exception {
		tempAddOnPrice = 0;
		VBox addOnServiceTilePane = new VBox();
		File dir = new File(properties.getProperty("addon.services.images.path"));

		
		Map<String, Image> images = new HashMap<String, Image>();
		int fileCount = 0;

		for (File file : dir.listFiles()) {
			if (file.isFile()) {
				images.put(file.getName(), new Image(new FileInputStream(file), 800, 600, true, true));
				if (fileCount++ > 50) {
					break;
				}
			}
		}

		if (carouselNo == 1) {
			final CarouselOne<ImageHandle> carouselOne = new CarouselOne<>();

			carouselOne.setPrefSize(SvmController.getWidth(), SvmController.getHeightMiddle() / 2);

			TreeItem<ImageHandle> root = new TreeItem<>();
			carouselOne.setRoot(root);
			carouselOne.setShowRoot(false);

			for (Map.Entry<String, Image> entry : images.entrySet()) {
				ImageView imageView = new ImageView();
				imageView.setId(entry.getKey());
				imageView.setImage(entry.getValue());

				ImageView priceTagView = new ImageView(
						new Image(new File(properties.getProperty("price.tag.path")).toURI().toString()));
				priceTagView.setFitHeight(150);
				priceTagView.setFitWidth(150);

				root.getChildren().add(new TreeItem<>(new ImageHandle(priceTagView, imageView)));
			}

			carouselOne.setCellFactory(new Callback<TreeView<ImageHandle>, TreeCell<ImageHandle>>() {
				@Override
				public TreeCell<ImageHandle> call(final TreeView<ImageHandle> carousel) {
					TreeCell<ImageHandle> carouselCell = new TreeCell<ImageHandle>() {
						@Override
						protected void updateItem(ImageHandle item, boolean empty) {
							super.updateItem(item, empty);

							if (!empty) {
								ImageView imageView = item.getImage();
								imageView.setPreserveRatio(true);

								ImageView priceTagImageView = item.getPriceImage();
								priceTagImageView.setPreserveRatio(true);

								Label priceText = new Label();
								tempAddOnPrice = tempAddOnPrice + 100;
								priceText.setText(tempAddOnPrice + SvmConstants.TILDA);
								priceText.setAlignment(Pos.CENTER);
								priceText.setPadding(new Insets(0, 0, 10, 0));
								priceText.setFont(Font.font(SvmConstants.FONT_RUPEE_FORADIAN, FontWeight.BOLD, 40));
								priceText.setTextFill(Color.CHARTREUSE);

								StackPane stackPane = new StackPane();
								stackPane.setId(imageView.getId() + SvmConstants.COMMA + tempAddOnPrice);
								stackPane.getChildren().addAll(imageView, priceTagImageView, priceText);
								stackPane.setAlignment(Pos.BOTTOM_CENTER);

								setGraphic(stackPane);
							} else {
								setGraphic(null);
							}
						}
					};

					carouselCell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
					carouselCell.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent event) {
							try {
								// Show AddOn Service Purchase page
								String productOrAddOn = SvmConstants.ADDON;
								showProductNutritionPage(productCarouselCell, carouselCell, productOrAddOn, SvmConstants.ALL);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

					return carouselCell;
				}
			});

			// Back navigation
			VBox backHBox = new VBox();
			ImageView backView = new ImageView(
					new Image(new File(properties.getProperty("back.navigation.path")).toURI().toString()));
			backHBox.setAlignment(Pos.TOP_LEFT);
			backHBox.getChildren().add(backView);
			backHBox.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					showAddOnConfirmationPage(prdCarouselCell);
				}
			});
						
			addOnServiceTilePane.getChildren().addAll(backHBox, carouselOne);
		} else if (carouselNo == 2) {
			// sort products
			TreeMap<String, Image> sortedImages = new TreeMap<>(images);

			final CarouselTwo<ImageHandle> carouselTwo = new CarouselTwo<>();

			carouselTwo.setPrefSize(SvmController.getWidth(), SvmController.getHeightMiddle() / 2);

			TreeItem<ImageHandle> root = new TreeItem<>();
			carouselTwo.setRoot(root);
			carouselTwo.setShowRoot(false);

			for (Map.Entry<String, Image> entry : sortedImages.entrySet()) {
				ImageView imageView = new ImageView();
				imageView.setId(entry.getKey());
				imageView.setImage(entry.getValue());

				ImageView priceTagView = new ImageView(
						new Image(new File(properties.getProperty("price.tag.path")).toURI().toString()));
				priceTagView.setFitHeight(150);
				priceTagView.setFitWidth(150);

				root.getChildren().add(new TreeItem<>(new ImageHandle(priceTagView, imageView)));
			}

			carouselTwo.setCellFactory(new Callback<TreeView<ImageHandle>, TreeCell<ImageHandle>>() {
				@Override
				public TreeCell<ImageHandle> call(final TreeView<ImageHandle> carousel) {
					TreeCell<ImageHandle> carouselCell = new TreeCell<ImageHandle>() {
						@Override
						protected void updateItem(ImageHandle item, boolean empty) {
							super.updateItem(item, empty);

							if (!empty) {
								ImageView imageView = item.getImage();
								imageView.setPreserveRatio(true);

								ImageView priceTagImageView = item.getPriceImage();
								priceTagImageView.setPreserveRatio(true);

								Label priceText = new Label();
								tempAddOnPrice = tempAddOnPrice + 100;
								priceText.setText(tempAddOnPrice + SvmConstants.TILDA);
								priceText.setAlignment(Pos.CENTER);
								priceText.setPadding(new Insets(0, 0, 10, 0));
								priceText.setFont(Font.font(SvmConstants.FONT_RUPEE_FORADIAN, FontWeight.BOLD, 40));
								priceText.setTextFill(Color.CHARTREUSE);

								StackPane stackPane = new StackPane();
								stackPane.setId(imageView.getId() + SvmConstants.COMMA + tempAddOnPrice);
								stackPane.getChildren().addAll(imageView, priceTagImageView, priceText);
								stackPane.setAlignment(Pos.BOTTOM_CENTER);

								setGraphic(stackPane);
							} else {
								setGraphic(null);
							}
						}
					};

					carouselCell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
					carouselCell.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent event) {
							try {
								// Show AddOn Service Purchase page
								String productOrAddOn = SvmConstants.ADDON;
								showProductNutritionPage(productCarouselCell, carouselCell, productOrAddOn, SvmConstants.ALL);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

					return carouselCell;
				}
			});

			addOnServiceTilePane.getChildren().addAll(carouselTwo);
		}
		
		return addOnServiceTilePane;
	}

	private void showAddOnConfirmationPage(TreeCell<ImageHandle> prdCarouselCell) {
		VBox messageVboxProducts = new VBox(8);
		Button yesAddOnBtn = new Button();
		Button noAddOnBtn = new Button();

		String[] imageName = prdCarouselCell.getChildrenUnmodifiable().toString().split(SvmConstants.SPLIT_PATTERN_TWO);

		productDetails.setProductID(imageName[1].split(SvmConstants.SPLIT_PATTERN_DOT)[0]);
		productDetails.setDispenseQty(qtyToDispense);

		DropShadow ds = new DropShadow();
		ds.setOffsetY(1.5f);
		ds.setColor(Color.color(0.4f, 0.4f, 0.4f));

		Label textMessage = new Label();
		textMessage.setPadding(new Insets(SvmController.getHeightMiddle() / 15, 10, 0, 10));
		userName = SvmController.getImageName();
		if (userName.equals(SvmConstants.EMPTY_STRING)) {
			String message = properties.get("addon.sevice.confirmation").toString().replaceAll(SvmConstants.COMMA, SvmConstants.EMPTY_STRING).trim();
			textMessage.setText(MessageFormat.format(message, userName));
		} else {
			textMessage.setText(MessageFormat.format((String) properties.get("addon.sevice.confirmation"), userName));
		}

		textMessage.setEffect(ds);
		textMessage.setCache(true);
		textMessage.setWrapText(true);
		textMessage.setMinWidth(SvmController.getWidth() / 2);
		textMessage.setMinHeight(SvmController.getHeightMiddle() / 7);
		textMessage.setTextFill(Color.BLACK);
		textMessage.setFont(Font.font(SvmConstants.FONT_GABRIOLA, FontWeight.BOLD, 36));
		
		HBox hBox = new HBox(50);
		hBox.setAlignment(Pos.CENTER);

		yesAddOnBtn.setText(properties.getProperty("yes"));
		yesAddOnBtn.setId(SvmConstants.RECORD_SALES);
		yesAddOnBtn.setPrefWidth(SvmController.getWidth() / 3);
		yesAddOnBtn.setPrefHeight(SvmController.getHeightMiddle() / 7);
		yesAddOnBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					VBox addOnServiceVBox = new VBox();
					VBox addOnServiceVBoxOne = new VBox();
					VBox addOnServiceVBoxTwo = new VBox();

					addOnServiceVBoxOne = showAddOnSevicesList(prdCarouselCell, existingStage, 1);
					addOnServiceVBoxTwo = showAddOnSevicesList(prdCarouselCell, existingStage, 2);
					
					addOnServiceVBox.getChildren().addAll(addOnServiceVBoxOne, addOnServiceVBoxTwo);
					
					new SvmController().showPrimaryStageAddOnServiceCarousel(existingStage, videoVboxProducts, addOnServiceVBox,
							weatherVboxProducts, false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		noAddOnBtn.setText(properties.getProperty("no"));
		noAddOnBtn.setId(SvmConstants.RECORD_SALES);
		noAddOnBtn.setPrefWidth(SvmController.getWidth() / 3);
		noAddOnBtn.setPrefHeight(SvmController.getHeightMiddle() / 7);
		noAddOnBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showPaymentOptionsPage();
			}
		});

		hBox.setPadding(new Insets(SvmController.getHeightMiddle() / 5, 0, 75, 0));
		hBox.getChildren().addAll(yesAddOnBtn, noAddOnBtn);
		
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

		new SvmController().showAddOnServiceConfirmationStage(existingStage, videoVboxProducts, messageVboxProducts,
				weatherVboxProducts);
	}

	private void showRedeemCouponPage(TreeCell<ImageHandle> prdCarouselCell) {
		String[] imageName = prdCarouselCell.getChildrenUnmodifiable().toString().split(SvmConstants.SPLIT_PATTERN_TWO);

		productDetails.setProductID(imageName[1].split(SvmConstants.SPLIT_PATTERN_DOT)[0]);
		productDetails.setDispenseQty(qtyToDispense);

//		getCouponScanDialog(Alert.AlertType.INFORMATION, properties.getProperty("redeem.coupon"));

		readThread = svmController.startReadThread();

		// start QR Scanner
		socketAPI.startQRScan(SvmController.getClient());
		SvmController.setQrCodeScan(true);
		SvmController.waitForThread();
		String respMsgQrCodeScan = svmController.getResponseMessage();
		readThread.shutdown();
		System.out.println("QR Response Code: " + respMsgQrCodeScan);

		if (respMsgQrCodeScan.contains(SvmConstants.SUCCESS_STATUS)) {
			String readTag = svmStrUtils.getReadTag(respMsgQrCodeScan);
			System.out.println("Read Tag: " + readTag);
			if (readTag.startsWith(properties.getProperty("qr.code.format"))) {
				String prodUnitPriceStr = updateUnitPriceDeduct(prodAddOnServicePriceProp.getValue(), String.valueOf(15));
				prodAddOnServicePriceProp.setValue(prodUnitPriceStr);
			} else {
				invalidCouponDialog();
			}
		} else if (respMsgQrCodeScan.contains(SvmConstants.TIMEOUT_STATUS)) {
			getTimeoutDialog();
		}
		 SvmController.setQrCodeScan(false);
	}

	private void getCouponScanDialog(AlertType alertType, String headerText) {
		Alert alert = new Alert(alertType.INFORMATION);

		alert.setHeaderText(headerText);

		Thread alertThread = new Thread(() -> {
			try {
				// Wait for 3 secs
				Thread.sleep(3000);
				if (alert.isShowing()) {
					Platform.runLater(() -> alert.close());
				}
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		});
		alertThread.setName(SvmConstants.ALERT_THREAD);
		alertThread.setDaemon(true);
		alertThread.start();

		alert.showAndWait();
	}
	
	private void getTimeoutDialog() {
		Alert alert = new Alert(AlertType.ERROR);

		alert.setHeaderText(properties.getProperty("redeem.coupon.timedout"));

		Thread alertTimeoutThread = new Thread(() -> {
			try {
				// Wait for 3 secs
				Thread.sleep(3000);
				if (alert.isShowing()) {
					Platform.runLater(() -> alert.close());
				}
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		});
		alertTimeoutThread.setName(SvmConstants.ALERT_TIMEOUT_THREAD);
		alertTimeoutThread.setDaemon(true);
		alertTimeoutThread.start();

		alert.showAndWait();
	}
	
	private void invalidCouponDialog() {
		Alert alert = new Alert(AlertType.ERROR);

		alert.setHeaderText(properties.getProperty("invalid.coupon"));

		Thread invalidCouponThread = new Thread(() -> {
			try {
				// Wait for 3 secs
				Thread.sleep(3000);
				if (alert.isShowing()) {
					Platform.runLater(() -> alert.close());
				}
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		});
		invalidCouponThread.setName(SvmConstants.INVALID_COUPON_THREAD);
		invalidCouponThread.setDaemon(true);
		invalidCouponThread.start();

		alert.showAndWait();
	}

	private void createAddressDetailsPage() {
		GridPane firstStreetStatePane = new GridPane();
		firstStreetStatePane.setPadding(new Insets(5));
		firstStreetStatePane.setHgap(20);
		firstStreetStatePane.setVgap(20);
		firstStreetStatePane.setPadding(new Insets(50, 0, 0, 50));

		// Home navigation
		VBox homeViewVBox = new VBox(10);
		homeViewVBox.setAlignment(Pos.TOP_LEFT);
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
		homeViewVBox.getChildren().add(homeView);
				
		Label firstNameLabel = new Label(properties.getProperty("first.name"));
		firstNameLabel.setTextFill(Color.BLACK);
		firstNameLabel.setFont(Font.font(null, FontWeight.BOLD, 25));
		firstStreetStatePane.add(firstNameLabel, 0, 0);
		TextField firstNameTF = new TextField();
		if (SvmController.getImageName().equals(SvmConstants.EMPTY_STRING)) {
			firstNameTF.setPromptText(properties.getProperty("enter.first.name"));
		} else {
			firstNameTF.setPromptText(properties.getProperty("first.name.prompt.text"));
		}
		firstNameTF.setFocusTraversable(false);
		firstNameTF.getProperties().put(VK_TYPE, SvmConstants.TEXT);
		firstNameTF.setMaxSize(200, 10);
		firstStreetStatePane.add(firstNameTF, 1, 0);

		Label streetLabel = new Label(properties.getProperty("street.name"));
		streetLabel.setTextFill(Color.BLACK);
		streetLabel.setFont(Font.font(null, FontWeight.BOLD, 25));
		firstStreetStatePane.add(streetLabel, 0, 1);
		TextField streetTF = new TextField();
		if (SvmController.getImageName().equals(SvmConstants.EMPTY_STRING)) {
			streetTF.setPromptText(properties.getProperty("enter.street.name"));
		} else {
			streetTF.setPromptText(properties.getProperty("street.name.prompt.text"));
		}
		streetTF.setFocusTraversable(false);
		streetTF.getProperties().put(VK_TYPE, SvmConstants.TEXT);
		streetTF.setMaxSize(200, 10);
		firstStreetStatePane.add(streetTF, 1, 1);

		Label stateLabel = new Label(properties.getProperty("state"));
		stateLabel.setTextFill(Color.BLACK);
		stateLabel.setFont(Font.font(null, FontWeight.BOLD, 25));
		firstStreetStatePane.add(stateLabel, 0, 2);
		TextField stateTF = new TextField();
		if (SvmController.getImageName().equals(SvmConstants.EMPTY_STRING)) {
			stateTF.setPromptText(properties.getProperty("enter.state"));
		} else {
			stateTF.setPromptText(properties.getProperty("state.prompt.text"));
		}
		stateTF.setFocusTraversable(false);
		stateTF.getProperties().put(VK_TYPE, SvmConstants.TEXT);
		stateTF.setMaxSize(200, 10);
		firstStreetStatePane.add(stateTF, 1, 2);

		GridPane lastCityPostalPane = new GridPane();
		lastCityPostalPane.setPadding(new Insets(5));
		lastCityPostalPane.setHgap(20);
		lastCityPostalPane.setVgap(20);
		lastCityPostalPane.setPadding(new Insets(50, 0, 0, 100));

		Label lastNameLabel = new Label(properties.getProperty("last.name"));
		lastNameLabel.setTextFill(Color.BLACK);
		lastNameLabel.setFont(Font.font(null, FontWeight.BOLD, 25));
		lastCityPostalPane.add(lastNameLabel, 0, 0);
		TextField lastNameTF = new TextField();
		if (SvmController.getImageName().equals(SvmConstants.EMPTY_STRING)) {
			lastNameTF.setPromptText(properties.getProperty("enter.last.name"));
		} else {
			lastNameTF.setPromptText(properties.getProperty("last.name.prompt.text"));
		}
		lastNameTF.setFocusTraversable(false);
		lastNameTF.getProperties().put(VK_TYPE, SvmConstants.TEXT);
		lastNameTF.setMaxSize(200, 10);
		lastCityPostalPane.add(lastNameTF, 1, 0);

		Label cityLabel = new Label(properties.getProperty("city"));
		cityLabel.setTextFill(Color.BLACK);
		cityLabel.setFont(Font.font(null, FontWeight.BOLD, 25));
		lastCityPostalPane.add(cityLabel, 0, 1);
		TextField cityTF = new TextField();
		if (SvmController.getImageName().equals(SvmConstants.EMPTY_STRING)) {
			cityTF.setPromptText(properties.getProperty("enter.city"));
		} else {
			cityTF.setPromptText(properties.getProperty("city.prompt.text"));
		}
		cityTF.setFocusTraversable(false);
		cityTF.getProperties().put(VK_TYPE, SvmConstants.TEXT);
		cityTF.setMaxSize(200, 10);
		lastCityPostalPane.add(cityTF, 1, 1);

		Label postalCodeLabel = new Label(properties.getProperty("postal.code"));
		postalCodeLabel.setTextFill(Color.BLACK);
		postalCodeLabel.setFont(Font.font(null, FontWeight.BOLD, 25));
		lastCityPostalPane.add(postalCodeLabel, 0, 2);
		TextField postalCodeTF = new TextField();
		if (SvmController.getImageName().equals(SvmConstants.EMPTY_STRING)) {
			postalCodeTF.setPromptText(properties.getProperty("enter.postal.code"));
		} else {
			postalCodeTF.setPromptText(properties.getProperty("postal.code.prompt.text"));
		}
		postalCodeTF.setFocusTraversable(false);
		postalCodeTF.getProperties().put(VK_TYPE, SvmConstants.NUMERIC);
		postalCodeTF.setMaxSize(200, 10);
		lastCityPostalPane.add(postalCodeTF, 1, 2);

		VBox allContentsVBox = new VBox();

		HBox messageHBox = new HBox(10);
		messageHBox.setAlignment(Pos.CENTER);
		messageHBox.setPadding(new Insets(10, 0, 10, 0));
		
		DropShadow ds = new DropShadow();
		ds.setOffsetY(1.5f);
		ds.setColor(Color.color(0.4f, 0.4f, 0.4f));

		Label textMessage = new Label(properties.getProperty("enter.shipping.address"));
		textMessage.setEffect(ds);
		textMessage.setCache(true);
		textMessage.setWrapText(true);
		textMessage.setTextFill(Color.BLACK);
		textMessage.setFont(Font.font(SvmConstants.FONT_GABRIOLA, FontWeight.BOLD, 40));
		messageHBox.getChildren().add(textMessage);
		
		HBox okHBox = new HBox(10);
		okHBox.setAlignment(Pos.CENTER);
		okHBox.setPadding(new Insets(SvmController.getHeightMiddle() / 8, 0, 0, 0));
		
		Button okBtn = new Button(properties.getProperty("ok.button"));
		okBtn.setId(SvmConstants.RECORD_SALES);
		okBtn.setPrefSize(SvmController.getWidth() / 2, SvmController.getHeightMiddle() / 7);
		okBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				showPaymentOptionsPage();
			}
		});

		okHBox.getChildren().add(okBtn);

		HBox addressHBox = new HBox();
		addressHBox.setPadding(new Insets(SvmController.getHeightMiddle() / 15, 0, 0, 0));
		addressHBox.getChildren().addAll(firstStreetStatePane, lastCityPostalPane);
		allContentsVBox.getChildren().addAll(homeViewVBox, messageHBox, addressHBox, okHBox);

		new SvmController().showPrimaryStageGreetings(existingStage, videoVboxProducts, allContentsVBox,
				weatherVboxProducts, keyBoardPopup);
	}

	private void createShippingConfirmationPage() {
		VBox messageVboxProducts = new VBox();
		Button yesShippingBtn = new Button();
		Button noShippingBtn = new Button();

		DropShadow ds = new DropShadow();
		ds.setOffsetY(1.5f);
		ds.setColor(Color.color(0.4f, 0.4f, 0.4f));

		Label textMessage = new Label();
//		textMessage.setText(properties.get("shipping.message").toString());
		textMessage.setText(properties.getProperty("shipping.message"));
		textMessage.setPadding(new Insets(20, 10, 0, 10));
		textMessage.setEffect(ds);
		textMessage.setCache(true);
		textMessage.setWrapText(true);
		textMessage.setTextFill(Color.BLACK);
		textMessage.setFont(Font.font(SvmConstants.FONT_GABRIOLA, FontWeight.BOLD, 40));

		HBox hBox = new HBox(10);
		hBox.setAlignment(Pos.CENTER);
		hBox.setPadding(new Insets(SvmController.getHeightMiddle() / 8, 0, 0, 0));

		yesShippingBtn.setText(properties.getProperty("yes"));
		yesShippingBtn.setId(SvmConstants.RECORD_SALES);
		yesShippingBtn.setPrefWidth(SvmController.getWidth() / 3);
		yesShippingBtn.setPrefHeight(SvmController.getHeightMiddle() / 7);
		yesShippingBtn.setEffect(new Reflection());
		yesShippingBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showPaymentOptionsPage();
			}
		});

		noShippingBtn.setText(properties.getProperty("no"));
		noShippingBtn.setId(SvmConstants.RECORD_SALES);
		noShippingBtn.setPrefWidth(SvmController.getWidth() / 3);
		noShippingBtn.setPrefHeight(SvmController.getHeightMiddle() / 7);
		noShippingBtn.setEffect(new Reflection());
		noShippingBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				createAddressDetailsPage();
			}
		});

		hBox.getChildren().addAll(yesShippingBtn, noShippingBtn);

		messageVboxProducts.setPadding(new Insets(50, 0, 30, 0));
		messageVboxProducts.setSpacing(10);
		messageVboxProducts.setAlignment(Pos.CENTER);
		messageVboxProducts.getChildren().addAll(textMessage, hBox);

		new SvmController().showAddOnServiceConfirmationStage(existingStage, videoVboxProducts, messageVboxProducts,
				weatherVboxProducts);
	}

	public void showPaymentOptionsPage() {
		PaymentGatewayLayout paymentGatewayLayout = new PaymentGatewayLayout(properties);
		
		VBox mainVBox = new VBox(10);
		mainVBox.setPadding(new Insets(20));
		
		HBox cashRedeemBtnHBox = new HBox(20);
		cashRedeemBtnHBox.setAlignment(Pos.TOP_CENTER);
		
		// Home navigation
		VBox homeViewVBox = new VBox(10);
		homeViewVBox.setAlignment(Pos.TOP_LEFT);
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
		homeViewVBox.getChildren().add(homeView);
				
		Label redeemCouponMessage = new Label();
		redeemCouponMessage.setText(properties.getProperty("redeem.coupon.message"));
		redeemCouponMessage.setCache(true);
		redeemCouponMessage.setWrapText(true);
		redeemCouponMessage.setTextFill(Color.BLACK);
		redeemCouponMessage.setFont(Font.font(SvmConstants.FONT_GABRIOLA, FontWeight.BOLD, 40));
		
		Button redeemCouponBtn = new Button(properties.getProperty("redeem.coupon.button"));
		redeemCouponBtn.setId(SvmConstants.RECORD_SALES);
		redeemCouponBtn.setPrefSize(SvmController.getWidth() / 3, SvmController.getHeightMiddle() / 7);
		redeemCouponBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				showRedeemCouponPage(productCarouselCell);
			}
		});
		cashRedeemBtnHBox.setPadding(new Insets(0, 0, 20, 0));
		cashRedeemBtnHBox.getChildren().addAll(redeemCouponMessage, redeemCouponBtn);
		
		VBox totalAmtMsgVBox = new VBox(30);
		HBox totalAmtLabelHBox = new HBox(10);
		totalAmtLabelHBox.setAlignment(Pos.CENTER);
		totalAmtLabelHBox.setPadding(new Insets(0, 0, 20, 0));
		
		Label totalAmtLabel = new Label(properties.getProperty("total.amount"));
		totalAmtLabel.setFont(Font.font(SvmConstants.FONT_RUPEE_FORADIAN, FontWeight.LIGHT, 40));
		totalAmtLabel.setTextFill(Color.RED);
		
		if (prodAddOnServicePrice == 0) {
			prodAddOnServicePriceProp.setValue(prodUnitPrice.getValue());
		} else {
			prodAddOnServicePriceProp.setValue(prodAddOnServicePrice + SvmConstants.TILDA);
		}
		Label prodAddOnServiceLabel = new Label(prodAddOnServicePriceProp.getValue());
		prodAddOnServiceLabel.textProperty().bind(prodAddOnServicePriceProp);
		prodAddOnServiceLabel.setFont(Font.font(SvmConstants.FONT_RUPEE_FORADIAN, FontWeight.LIGHT, 40));
		prodAddOnServiceLabel.setTextFill(Color.RED);
		totalAmtLabelHBox.getChildren().addAll(totalAmtLabel, prodAddOnServiceLabel);
		
		HBox cashMsgOkBtnHBox = new HBox(175);
		cashMsgOkBtnHBox.setPadding(new Insets(20));
		Label cashTextMessage = new Label();
		cashTextMessage.setText(properties.getProperty("cash.display.message"));
		cashTextMessage.setCache(true);
		cashTextMessage.setWrapText(true);
		cashTextMessage.setTextFill(Color.BLACK);
		cashTextMessage.setFont(Font.font(SvmConstants.FONT_GABRIOLA, FontWeight.BOLD, 40));
		
		Button cashOkBtn = new Button(properties.getProperty("ok.button"));
		cashOkBtn.setId(SvmConstants.RECORD_SALES);
		cashOkBtn.setPrefSize(SvmController.getWidth() / 3, SvmController.getHeightMiddle() / 7);
		cashOkBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				new PaymentGatewayLayout().dispenseProduct();
			}
		});
		cashMsgOkBtnHBox.setAlignment(Pos.BASELINE_CENTER);
		cashMsgOkBtnHBox.getChildren().addAll(cashTextMessage, cashOkBtn);
		totalAmtMsgVBox.getChildren().addAll(totalAmtLabelHBox, cashMsgOkBtnHBox);
		
		VBox otherPaymentsMsgVBox = new VBox(10);
		otherPaymentsMsgVBox.setAlignment(Pos.CENTER);
		otherPaymentsMsgVBox.setPadding(new Insets(50));
		
		Label textMessage = new Label();
		textMessage.setText(properties.getProperty("payment.options"));
		textMessage.setCache(true);
		textMessage.setTextFill(Color.BLACK);
		textMessage.setFont(Font.font(SvmConstants.FONT_GABRIOLA, FontWeight.BOLD, 36));
		
		HBox otherPayBtnsHBox = new HBox(10);
		Button ccBtn = new Button(properties.getProperty("credit.card.button"));
		ccBtn.setId(SvmConstants.RECORD_SALES);
		ccBtn.setPadding(new Insets(10));
		ccBtn.setPrefSize(SvmController.getWidth() / 3, SvmController.getHeightMiddle() / 7);
		ccBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				BorderPane cCBorderPane = paymentGatewayLayout.showCreditCardScreen();
				
				new SvmController().showCreditCardStage(existingStage, videoVboxProducts, cCBorderPane,
						weatherVboxProducts, keyBoardPopup);
			}
		});
		
		Button payPalBtn = new Button(properties.getProperty("paypal.button"));
		payPalBtn.setId(SvmConstants.RECORD_SALES);
		payPalBtn.setPadding(new Insets(10));
		payPalBtn.setPrefSize(SvmController.getWidth() / 3, SvmController.getHeightMiddle() / 7);
		payPalBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				BorderPane payPalBorderPane = paymentGatewayLayout.showPayPalScreen();
				
				new SvmController().showCreditCardStage(existingStage, videoVboxProducts, payPalBorderPane,
						weatherVboxProducts, keyBoardPopup);
			}
		});
		
		Button walletServicesBtn = new Button(properties.getProperty("wallet.services.button"));
		walletServicesBtn.setId(SvmConstants.RECORD_SALES);
		walletServicesBtn.setPadding(new Insets(10));
		walletServicesBtn.setPrefSize(SvmController.getWidth() / 3, SvmController.getHeightMiddle() / 7);
		walletServicesBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				BorderPane walletSerBorderPane = paymentGatewayLayout.showWalletServicesScreen();
				
				new SvmController().showCreditCardStage(existingStage, videoVboxProducts, walletSerBorderPane,
						weatherVboxProducts, keyBoardPopup);
			}
		});
		otherPayBtnsHBox.getChildren().addAll(ccBtn, payPalBtn, walletServicesBtn);
		
		otherPaymentsMsgVBox.getChildren().addAll(textMessage, otherPayBtnsHBox);
		
		mainVBox.getChildren().addAll(homeViewVBox, cashRedeemBtnHBox, totalAmtMsgVBox, otherPaymentsMsgVBox);

		new SvmController().showAddOnServiceConfirmationStage(existingStage, videoVboxProducts, mainVBox,
				weatherVboxProducts);
	}
	
	public void showThankYouScreen() {
		stopCounter = false;
		HBox piHBox = new HBox();
		counterProp.setValue(SvmConstants.COUNTER_VALUE);
		Label counter = new Label(counterProp.getValue());
		counter.textProperty().bind(counterProp);
		counter.setCache(true);
		counter.setWrapText(true);
		counter.setTextFill(Color.RED);
		counter.setFont(Font.font(SvmConstants.FONT_ARIAL, FontWeight.BOLD, 40));
		piHBox.setAlignment(Pos.TOP_RIGHT);
		piHBox.setPadding(new Insets(10, 10, 0, 0));
		piHBox.getChildren().add(counter);
		
		HBox buyButtonHBox = new HBox();
		buyButtonHBox.setAlignment(Pos.CENTER);
		buyButtonHBox.setPadding(new Insets(SvmController.getHeightMiddle() / 1.2, 0, 10, 0));
		
		Button buyProdButton = new Button();
		buyProdButton.setText(properties.getProperty("touch.here.to.end"));
		buyProdButton.setId(SvmConstants.RECORD_SALES);
		buyProdButton.setAlignment(Pos.CENTER);
		buyProdButton.setPrefWidth(SvmController.getWidth() / 2);
		buyProdButton.setPrefHeight(SvmController.getHeightMiddle() / 7.5);
		
		buyProdButton.setEffect(new DropShadow());
		buyProdButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				stopCounter = true;
				SvmController.setFirstTime(0);
				SvmController.setFrDone(false);
				
				// start counter
				SvmController.setCounter(0);
//				new GreetingsLayout();
//				GreetingsLayout.getProgressBarThread().startPBThread();
				
				svmController.showVideoWeatherAndProductsScreen(existingStage);
			}
		});
		
		buyButtonHBox.getChildren().add(buyProdButton);

		// start counter thread
		counterThread = new ProdLayout().new CounterThread(counterProp);
		counterThread.start();
				
		VBox pleaseWaitTilePane = new VBox();
		pleaseWaitTilePane.getChildren().addAll(piHBox, buyButtonHBox);

		new SvmController().showThankYouStage(existingStage, videoVboxProducts, pleaseWaitTilePane,
				weatherVboxProducts);
	}
	
	private int getAvailableProductStockQty(ImageView imageView) {
		// Get product available qty
		readThread = svmController.startReadThread();
		socketAPI.getProductBalance(SvmController.getClient(), imageView.getId().split(SvmConstants.SPLIT_PATTERN_DOT)[0]);
		SvmController.waitForThread();
		String respMsgAvailableQty = svmController.getResponseMessage();
		readThread.shutdown();

		return Integer.parseInt(respMsgAvailableQty.split(SvmConstants.EQUALS)[1].replaceAll(SvmConstants.STRING_REPLACE_THREE, SvmConstants.EMPTY_STRING));
	}

	public class CounterThread extends Thread {
		StringProperty counterProp;
		
		public CounterThread(StringProperty counterProp) {
			this.counterProp = counterProp;
		}
		
		public synchronized void run() {
				for (int i = Integer.parseInt(SvmConstants.COUNTER_VALUE); i >= 0; i--) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
					
					final int progress = i;

					// update ProgressIndicator on FX thread
					Platform.runLater(new Runnable() {
						public void run() {
							try {
								counterProp.setValue(String.valueOf(progress));

								if (Double.parseDouble(counterProp.getValue()) == 0 && stopCounter == false) {
									SvmController.setFirstTime(0);
									SvmController.setFrDone(false);
									
									// start counter
									SvmController.setCounter(0);
//									new GreetingsLayout();
//									GreetingsLayout.getProgressBarThread().startPBThread();

									svmController.showVideoWeatherAndProductsScreen(existingStage);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
		}
	}
	
	public class PlayAudioThread extends Thread {
		String fileName;
		
		public PlayAudioThread(String fileName) {
			this.fileName = fileName;
		}
		
		public synchronized void run() {
			new SimplePlayer(fileName);
		}
	}
	
}
