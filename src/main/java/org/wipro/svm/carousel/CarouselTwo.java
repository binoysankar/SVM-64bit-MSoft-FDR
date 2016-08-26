package org.wipro.svm.carousel;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class CarouselTwo<T> extends TreeView<T> {
  private static final String DEFAULT_STYLE_CLASS_TWO = "carousel-two";

  public CarouselTwo(TreeItem<T> root) {
    super(root);
    getStyleClass().setAll(DEFAULT_STYLE_CLASS_TWO);
  }

  public CarouselTwo() {
    this(null);
  }

  @Override
  public String getUserAgentStylesheet() {
    return getClass().getResource("/css/carousel.css").toExternalForm();
  }
}
