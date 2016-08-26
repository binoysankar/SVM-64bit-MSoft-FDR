package org.wipro.svm.view;

import java.io.File;
import java.util.Properties;

import org.wipro.svm.controller.SvmController;
import org.wipro.svm.controller.SvmController.ReadThread;
import org.wipro.svm.service.impl.SocketAPIServiceImpl;
import org.wipro.svm.utils.SvmConstants;
import org.wipro.svm.virtual.keyboard.control.VkProperties;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class PaymentGatewayLayout implements VkProperties {
	private Properties props;
	private static SvmController svmController = new SvmController();
	SocketAPIServiceImpl socketAPI = new SocketAPIServiceImpl();
	ReadThread readThread;
	
	public PaymentGatewayLayout() {
	}
	
	public PaymentGatewayLayout(Properties props) {
		this.props = props;
	}

	public BorderPane showCreditCardScreen() {
		VBox mainVBox = new VBox(20);

		HBox labelHBox = new HBox(10);
		HBox cardTypesHBox = new HBox(10);
		HBox cardNoNameHBox = new HBox(10);
		HBox expiryDateHBox = new HBox(10);
		HBox cvvHBox = new HBox(10);
		HBox btnHBox = new HBox(10);
		
		BorderPane bPane = new BorderPane();
		bPane.setPadding(new Insets(10, 10, 10, 10));
		bPane.setPrefSize(SvmController.getWidth(), SvmController.getHeightMiddle());

		Button proceedbtn = new Button();
		Button cancelbtn = new Button();

		DropShadow ds = new DropShadow();
		ds.setOffsetY(1.5f);
		ds.setColor(Color.color(0.4f, 0.4f, 0.4f));

		Label cCLabel = new Label();
		cCLabel.setText(props.getProperty("pay.with.credit.card"));
		cCLabel.setEffect(ds);
		cCLabel.setCache(true);
		cCLabel.setWrapText(true);
		cCLabel.setTextFill(Color.BROWN);
		cCLabel.setFont(Font.font(SvmConstants.FONT_GABRIOLA, FontWeight.BOLD, 40));
		labelHBox.setPadding(new Insets(10, 10, 10, 30));
		labelHBox.setAlignment(Pos.BASELINE_LEFT);
		labelHBox.getChildren().add(cCLabel);

		Label weAcceptLabel = new Label();
		weAcceptLabel.setText(props.getProperty("we.accept"));
		weAcceptLabel.setCache(true);
		weAcceptLabel.setWrapText(true);
		weAcceptLabel.setTextFill(Color.BLACK);
		weAcceptLabel.setFont(Font.font(SvmConstants.FONT_GABRIOLA, FontWeight.MEDIUM, 36));
		ImageView cardTypesView = new ImageView(
				new Image(new File(props.getProperty("card.types.path")).toURI().toString()));
		cardTypesHBox.setAlignment(Pos.BASELINE_LEFT);
		cardTypesHBox.setPadding(new Insets(10, 10, 10, 30));
		cardTypesHBox.getChildren().addAll(weAcceptLabel, cardTypesView);

		TextField cCNumber = new TextField();
		cCNumber.getStyleClass().add(SvmConstants.TEXT_INPUT);
		cCNumber.setPromptText(props.getProperty("credit.card.prompt.text"));
		cCNumber.setFocusTraversable(false);
		cCNumber.getProperties().put(VK_TYPE, SvmConstants.NUMERIC);
		cCNumber.setMaxSize(250, 10);
		TextField cardHolderName = new TextField();
		cardHolderName.setPromptText(props.getProperty("card.holder.name.prompt.text"));
		cardHolderName.setFocusTraversable(false);
		cardHolderName.getProperties().put(VK_TYPE, SvmConstants.TEXT);
		cardHolderName.setMaxSize(250, 10);
		cardNoNameHBox.setAlignment(Pos.BASELINE_LEFT);
		cardNoNameHBox.setPadding(new Insets(10, 10, 10, 30));
		cardNoNameHBox.getChildren().addAll(cCNumber, cardHolderName);

		Label cCExpiryDateLabel = new Label();
		cCExpiryDateLabel.setText(props.getProperty("card.expiry.date"));
		cCExpiryDateLabel.setCache(true);
		cCExpiryDateLabel.setWrapText(true);
		cCExpiryDateLabel.setTextFill(Color.BLACK);
		cCExpiryDateLabel.setFont(Font.font(SvmConstants.FONT_GABRIOLA, FontWeight.MEDIUM, 36));

		TextField month = new TextField();
		month.getStyleClass().add(SvmConstants.TEXT_INPUT);
		month.setPromptText(props.getProperty("card.expiry.month.prompt.text"));
		month.setFocusTraversable(false);
		month.getProperties().put(VK_TYPE, SvmConstants.NUMERIC);
		month.setMaxSize(60, 10);
		Label backSlashLabel = new Label();
		backSlashLabel.setText(SvmConstants.BACK_SLASH);
		backSlashLabel.setCache(true);
		backSlashLabel.setWrapText(true);
		backSlashLabel.setTextFill(Color.BLACK);
		backSlashLabel.setFont(Font.font(SvmConstants.FONT_GABRIOLA, FontWeight.MEDIUM, 36));
		TextField year = new TextField();
		year.getStyleClass().add(SvmConstants.TEXT_INPUT);
		year.setPromptText(props.getProperty("card.expiry.year.prompt.text"));
		year.setFocusTraversable(false);
		year.getProperties().put(VK_TYPE, SvmConstants.NUMERIC);
		year.setMaxSize(60, 10);
		TextField cvvNumber = new TextField();
		cvvNumber.getStyleClass().add(SvmConstants.TEXT_INPUT);
		cvvNumber.setPromptText(props.getProperty("cvv.number.prompt.text"));
		cvvNumber.setFocusTraversable(false);
		cvvNumber.getProperties().put(VK_TYPE, SvmConstants.NUMERIC);
		cvvNumber.setMaxSize(80, 10);
		expiryDateHBox.setAlignment(Pos.BASELINE_LEFT);
		expiryDateHBox.setPadding(new Insets(10, 10, 10, 30));
		expiryDateHBox.getChildren().addAll(cCExpiryDateLabel, month, backSlashLabel, year, cvvNumber);
		
		ImageView cvvHelpView = new ImageView(
				new Image(new File(props.getProperty("cvv.help.path")).toURI().toString()));
		cvvHelpView.setFitWidth(400);
		cvvHelpView.setFitHeight(185);
		Hyperlink cvvPopupLink = new Hyperlink(SvmConstants.CVV_QUESTION);
		cvvPopupLink.setTextFill(Color.BROWN);
		cvvPopupLink.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				cvvPopupLink.setText("");
				cvvPopupLink.setGraphic(cvvHelpView);
			}
		});
		cvvPopupLink.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				cvvPopupLink.setText(SvmConstants.CVV_QUESTION);
				cvvPopupLink.setGraphic(null);
			}
		});
		cvvHBox.setAlignment(Pos.TOP_CENTER);
		cvvHBox.getChildren().addAll(cvvPopupLink);
		
		proceedbtn.setText(props.getProperty("proceed.button"));
		proceedbtn.setId(SvmConstants.RECORD_SALES);
		proceedbtn.setPrefWidth(SvmController.getWidth() / 2.5);
		proceedbtn.setPrefHeight(SvmController.getHeightMiddle() / 7);
		proceedbtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				dispenseProduct();
			}
		});

		cancelbtn.setText(props.getProperty("cancel.button"));
		cancelbtn.setId(SvmConstants.RECORD_SALES);
		cancelbtn.setPrefWidth(SvmController.getWidth() / 2.5);
		cancelbtn.setPrefHeight(SvmController.getHeightMiddle() / 7);
		cancelbtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				new ProdLayout().showPaymentOptionsPage();
			}
		});
		btnHBox.setAlignment(Pos.CENTER);
		btnHBox.getChildren().addAll(proceedbtn, cancelbtn);

		mainVBox.getChildren().addAll(labelHBox, cardTypesHBox, cardNoNameHBox, expiryDateHBox, cvvHBox);

		bPane.setTop(mainVBox);
		bPane.setBottom(btnHBox);

		return bPane;
	}
	
	public BorderPane showPayPalScreen() {
		VBox mainVBox = new VBox(20);

		HBox labelHBox = new HBox(10);
		HBox btnHBox = new HBox(10);
		
		BorderPane bPane = new BorderPane();
		bPane.setPadding(new Insets(10, 10, 10, 10));
		bPane.setPrefSize(SvmController.getWidth(), SvmController.getHeightMiddle());

		Button proceedbtn = new Button();
		Button cancelbtn = new Button();

		DropShadow ds = new DropShadow();
		ds.setOffsetY(1.5f);
		ds.setColor(Color.color(0.4f, 0.4f, 0.4f));

		Label cCLabel = new Label();
		cCLabel.setText(props.getProperty("paypal.text"));
		cCLabel.setEffect(ds);
		cCLabel.setCache(true);
		cCLabel.setWrapText(true);
		cCLabel.setTextFill(Color.BROWN);
		cCLabel.setFont(Font.font(SvmConstants.FONT_GABRIOLA, FontWeight.BOLD, 40));
		HBox payPalHBox = new HBox(10);
		ImageView payPalView = new ImageView(
				new Image(new File(props.getProperty("paypal.path")).toURI().toString()));
		payPalHBox.setPadding(new Insets(10, 10, 10, 30));
		payPalHBox.setAlignment(Pos.BASELINE_RIGHT);
		payPalHBox.getChildren().add(payPalView);
		labelHBox.setPadding(new Insets(10, 10, 10, 30));
		labelHBox.setAlignment(Pos.BASELINE_LEFT);
		labelHBox.getChildren().addAll(cCLabel, payPalHBox);

		VBox emailVBox = new VBox(5);
		Label emailLabel = new Label();
		emailLabel.setText(props.getProperty("email.text"));
		emailLabel.setEffect(ds);
		emailLabel.setCache(true);
		emailLabel.setWrapText(true);
		emailLabel.setTextFill(Color.BLACK);
		emailLabel.setFont(Font.font(SvmConstants.FONT_GABRIOLA, FontWeight.NORMAL, 36));
		TextField emailAddress = new TextField();
		emailAddress.getStyleClass().add(SvmConstants.TEXT_INPUT);
		emailAddress.setPromptText(props.getProperty("email.address.prompt.text"));
		emailAddress.setFocusTraversable(false);
		emailAddress.getProperties().put(VK_TYPE, SvmConstants.TEXT);
		emailAddress.setMaxSize(255, 10);
		emailVBox.setPadding(new Insets(10, 10, 10, 30));
		emailVBox.setAlignment(Pos.BASELINE_LEFT);
		emailVBox.getChildren().addAll(emailLabel, emailAddress);
		
		VBox payPalPasswordVBox = new VBox(5);
		Label payPalPasswordLabel = new Label();
		payPalPasswordLabel.setText(props.getProperty("paypal.password.text"));
		payPalPasswordLabel.setEffect(ds);
		payPalPasswordLabel.setCache(true);
		payPalPasswordLabel.setWrapText(true);
		payPalPasswordLabel.setTextFill(Color.BLACK);
		payPalPasswordLabel.setFont(Font.font(SvmConstants.FONT_GABRIOLA, FontWeight.NORMAL, 36));
		TextField payPalPasswordTF = new TextField();
		payPalPasswordTF.setPromptText(props.getProperty("paypal.password.prompt.text"));
		payPalPasswordTF.setFocusTraversable(false);
		payPalPasswordTF.getProperties().put(VK_TYPE, SvmConstants.TEXT);
		payPalPasswordTF.setMaxSize(250, 10);
		payPalPasswordVBox.setAlignment(Pos.BASELINE_LEFT);
		payPalPasswordVBox.setPadding(new Insets(10, 10, 10, 30));
		payPalPasswordVBox.getChildren().addAll(payPalPasswordLabel, payPalPasswordTF);

		proceedbtn.setText(props.getProperty("proceed.button"));
		proceedbtn.setId(SvmConstants.RECORD_SALES);
		proceedbtn.setPrefWidth(SvmController.getWidth() / 2.5);
		proceedbtn.setPrefHeight(SvmController.getHeightMiddle() / 7);
		proceedbtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				dispenseProduct();
			}
		});

		cancelbtn.setText(props.getProperty("cancel.button"));
		cancelbtn.setId(SvmConstants.RECORD_SALES);
		cancelbtn.setPrefWidth(SvmController.getWidth() / 2.5);
		cancelbtn.setPrefHeight(SvmController.getHeightMiddle() / 7);
		cancelbtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				new ProdLayout().showPaymentOptionsPage();
			}
		});
		btnHBox.setAlignment(Pos.CENTER);
		btnHBox.getChildren().addAll(proceedbtn, cancelbtn);

		mainVBox.getChildren().addAll(labelHBox, emailVBox, payPalPasswordVBox);

		bPane.setTop(mainVBox);
		bPane.setBottom(btnHBox);

		return bPane;
	}
	
	public BorderPane showWalletServicesScreen() {
		VBox mainVBox = new VBox(20);

		HBox labelHBox = new HBox(10);
		HBox btnHBox = new HBox(10);
		
		GridPane voucherIdPane = new GridPane();
		voucherIdPane.setPadding(new Insets(5));
		voucherIdPane.setHgap(20);
		voucherIdPane.setVgap(50);
		voucherIdPane.setPadding(new Insets(50, 0, 0, 30));
		
		BorderPane bPane = new BorderPane();
		bPane.setPadding(new Insets(10, 10, 10, 10));
		bPane.setPrefSize(SvmController.getWidth(), SvmController.getHeightMiddle());

		Button proceedbtn = new Button();
		Button cancelbtn = new Button();

		DropShadow ds = new DropShadow();
		ds.setOffsetY(1.5f);
		ds.setColor(Color.color(0.4f, 0.4f, 0.4f));

		Label walletLabel = new Label();
		walletLabel.setText(props.getProperty("pay.with.wallet"));
		walletLabel.setEffect(ds);
		walletLabel.setCache(true);
		walletLabel.setWrapText(true);
		walletLabel.setTextFill(Color.BROWN);
		walletLabel.setFont(Font.font(SvmConstants.FONT_GABRIOLA, FontWeight.BOLD, 40));
		labelHBox.setPadding(new Insets(10, 10, 10, 30));
		labelHBox.setAlignment(Pos.BASELINE_LEFT);
		labelHBox.getChildren().addAll(walletLabel);

		Label voucherIdLabel = new Label();
		voucherIdLabel.setText(props.getProperty("wallet.voucher.id"));
		voucherIdLabel.setEffect(ds);
		voucherIdLabel.setCache(true);
		voucherIdLabel.setWrapText(true);
		voucherIdLabel.setTextFill(Color.BLACK);
		voucherIdLabel.setFont(Font.font(SvmConstants.FONT_GABRIOLA, FontWeight.NORMAL, 36));
		voucherIdPane.add(voucherIdLabel, 0, 0);
		TextField voucherIdTF = new TextField();
		voucherIdTF.getStyleClass().add(SvmConstants.TEXT_INPUT);
		voucherIdTF.setPromptText(props.getProperty("voucher.id.prompt.text"));
		voucherIdTF.setFocusTraversable(false);
		voucherIdTF.getProperties().put(VK_TYPE, SvmConstants.NUMERIC);
		voucherIdTF.setMaxSize(255, 10);
		voucherIdPane.add(voucherIdTF, 1, 0);
		
		Label pinLabel = new Label();
		pinLabel.setText(props.getProperty("wallet.pin"));
		pinLabel.setEffect(ds);
		pinLabel.setCache(true);
		pinLabel.setWrapText(true);
		pinLabel.setTextFill(Color.BLACK);
		pinLabel.setFont(Font.font(SvmConstants.FONT_GABRIOLA, FontWeight.NORMAL, 36));
		voucherIdPane.add(pinLabel, 0, 1);
		TextField pinTF = new TextField();
		pinTF.getStyleClass().add(SvmConstants.TEXT_INPUT);
		pinTF.setPromptText(props.getProperty("pin.prompt.text"));
		pinTF.setFocusTraversable(false);
		pinTF.getProperties().put(VK_TYPE, SvmConstants.NUMERIC);
		pinTF.setMaxSize(255, 10);
		voucherIdPane.add(pinTF, 1, 1);

		proceedbtn.setText(props.getProperty("proceed.button"));
		proceedbtn.setId(SvmConstants.RECORD_SALES);
		proceedbtn.setPrefWidth(SvmController.getWidth() / 2.5);
		proceedbtn.setPrefHeight(SvmController.getHeightMiddle() / 7);
		proceedbtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				dispenseProduct();
			}
		});

		cancelbtn.setText(props.getProperty("cancel.button"));
		cancelbtn.setId(SvmConstants.RECORD_SALES);
		cancelbtn.setPrefWidth(SvmController.getWidth() / 2.5);
		cancelbtn.setPrefHeight(SvmController.getHeightMiddle() / 7);
		cancelbtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				new ProdLayout().showPaymentOptionsPage();
			}
		});
		btnHBox.setAlignment(Pos.CENTER);
		btnHBox.getChildren().addAll(proceedbtn, cancelbtn);

		mainVBox.getChildren().addAll(labelHBox, voucherIdPane);

		bPane.setTop(mainVBox);
		bPane.setBottom(btnHBox);

		return bPane;
	}
	
	public void dispenseProduct() {
		// Add Product and Qty to Cart
		readThread = svmController.startReadThread();
		socketAPI.addProductToCart(SvmController.getClient(), ProdLayout.getProductDetails().getProductID(),
				ProdLayout.getProductDetails().getDispenseQty());
		SvmController.waitForThread();
		readThread.shutdown();

		// Dispense Product - Free Vending Enabled
		readThread = svmController.startReadThread();
		socketAPI.dispenseProduct(SvmController.getClient());
		SvmController.waitForThread();
		readThread.shutdown();

		new ProdLayout().showThankYouScreen();
	}
	
}
