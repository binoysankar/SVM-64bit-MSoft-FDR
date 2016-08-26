package org.wipro.svm.carousel.internal.skin;

import java.io.File;

import com.sun.javafx.scene.control.skin.TreeCellSkin;

import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

/**
 * TreeCellSkin with absolutely no additional paddings whatsoever.
 */
public class EmptyTreeCellSkin<T> extends TreeCellSkin<T> {

  public EmptyTreeCellSkin(TreeCell<T> treeCell) {
    super(treeCell);
    
    File weatherBackgrd = new File("C:/Vendron/SVM/images/others/image.png");
	Image image = new Image(weatherBackgrd.toURI().toString());
	BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, false);
	treeCell.setBackground(new Background(
			new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
					BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, backgroundSize)));
  }

  @Override
  protected void layoutChildren(double x, double y, double w, double h) {
    layoutLabelInArea(x, y, w, h);
  }
}

