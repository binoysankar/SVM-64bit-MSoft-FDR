package org.wipro.svm.carousel;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class CarouselOne<T> extends TreeView<T> {
  private static final String DEFAULT_STYLE_CLASS_ONE = "carousel-one";

  public CarouselOne(TreeItem<T> root) {
    super(root);
    getStyleClass().setAll(DEFAULT_STYLE_CLASS_ONE);
  }

  public CarouselOne() {
    this(null);
  }

  @Override
  public String getUserAgentStylesheet() {
    return getClass().getResource("/css/carousel.css").toExternalForm();
  }
}
