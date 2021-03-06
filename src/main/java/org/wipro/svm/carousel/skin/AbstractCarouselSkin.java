package org.wipro.svm.carousel.skin;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;
import java.util.function.Function;

import org.wipro.svm.carousel.internal.skin.AbstractTreeViewSkin;
import org.wipro.svm.carousel.internal.skin.LayoutItem;
import org.wipro.svm.controller.SvmController;
import org.wipro.svm.css.SimpleStyleableDoubleProperty;

import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.HPos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.effect.Effect;
import javafx.scene.effect.PerspectiveTransform;
import javafx.scene.effect.Reflection;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

//TODO Carousel loop around to 0
/**
 * Abstract Skin for Carousel that provides layout of Cells with use of
 * {@link LayoutItem}s. It provides basic properties for each Carousel, handles
 * cell allocation and final positioning of the cells (in the proper display
 * order).
 * <p>
 *
 * Subclasses need to provide an implementation of
 * {@link #getLayoutItems(double)}, which is called once per frame if the
 * position of the Carousel changed.
 *
 * @param <T>
 *            the cell content type
 */
public abstract class AbstractCarouselSkin<T> extends AbstractTreeViewSkin<T> {
	private static List<CssMetaData<? extends Styleable, ?>> CSS_META_DATA;

	private static SvmController svmController = new SvmController();
	
	private static int count = 0;

	@Override
	public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
		if (CSS_META_DATA == null) {
			List<CssMetaData<? extends Styleable, ?>> metaData = new ArrayList<>(super.getCssMetaData());
			Collections.addAll(metaData, cellAlignment.getCssMetaData());
			CSS_META_DATA = Collections.unmodifiableList(metaData);
		}

		return CSS_META_DATA;
	}

	private final SimpleStyleableDoubleProperty cellAlignment = new SimpleStyleableDoubleProperty(this, "cellAlignment",
			"-fx-cell-alignment", 1.0);

	public final DoubleProperty cellAlignmentProperty() {
		return cellAlignment;
	}

	public final double getCellAlignment() {
		return cellAlignment.get();
	}

	public final void setCellAlignment(double cellAlignment) {
		this.cellAlignment.set(cellAlignment);
	}

	private final BooleanProperty reflectionsEnabled = new SimpleBooleanProperty(false);

	public final BooleanProperty reflectionsEnabledProperty() {
		return reflectionsEnabled;
	}

	public final boolean getReflectionsEnabled() {
		return reflectionsEnabled.get();
	}

	public final void setReflectionsEnabled(boolean reflectionsEnabled) {
		this.reflectionsEnabled.set(reflectionsEnabled);
	}

	private final BooleanProperty clipReflections = new SimpleBooleanProperty(true);

	public final BooleanProperty clipReflectionsProperty() {
		return clipReflections;
	}

	public final boolean getClipReflections() {
		return clipReflections.get();
	}

	public final void setClipReflections(boolean clipReflections) {
		this.clipReflections.set(clipReflections);
	}

	private final DoubleProperty density = new SimpleDoubleProperty(1.6688);

	public final DoubleProperty densityProperty() {
		return density;
	}

	public final double getDensity() {
		return density.get();
	}

	public final void setDensity(double density) {
		this.density.set(density);
	}

	private final DoubleProperty maxCellWidth = new SimpleDoubleProperty(350);
//	private final DoubleProperty maxCellWidth = new SimpleDoubleProperty();

	public final DoubleProperty maxCellWidthProperty() {
//		maxCellWidth.setValue(SvmController.getWidth() / 5);
		
		return maxCellWidth;
	}

	public final double getMaxCellWidth() {
		return maxCellWidth.get();
	}

	public final void setMaxCellWidth(double maxCellWidth) {
		this.maxCellWidth.set(maxCellWidth);
	}

	private final DoubleProperty maxCellHeight = new SimpleDoubleProperty(300);
//	private final DoubleProperty maxCellHeight = new SimpleDoubleProperty();

	public final DoubleProperty maxCellHeightProperty() {
//		maxCellHeight.setValue((SvmController.getHeightMiddle() / 2));
		
		return maxCellHeight;
	}

	public final double getMaxCellHeight() {
		return maxCellHeight.get();
	}

	public final void setMaxCellHeight(double maxCellHeight) {
		this.maxCellHeight.set(maxCellHeight);
	}

	private final WeakHashMap<IndexedCell<?>, LayoutItem> carouselCells = new WeakHashMap<>();

	private final Transition transition = new Transition() {
		{
			setCycleDuration(Duration.millis(500));
			setInterpolator(Interpolator.EASE_IN);
		}

		@Override
		protected void interpolate(double frac) {
			
			fractionalIndex = startFractionalIndex - startFractionalIndex * frac;
			getSkinnable().requestLayout();
		}
	};
	
	private double startFractionalIndex;
	private double fractionalIndex;

	private final ChangeListener<Bounds> cellBoundsRequestLayout = new ChangeListener<Bounds>() {
		@Override
		public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
			getSkinnable().requestLayout();
		}
	};

	protected final InvalidationListener requestLayout = new InvalidationListener() {
		@Override
		public void invalidated(Observable observable) {
			getSkinnable().requestLayout();
		}
	};

	private final ChangeListener<Number> moveCarousel = new ChangeListener<Number>() {
		@Override
		public void changed(ObservableValue<? extends Number> observableValue, Number old, Number current) {
			/*
			 * Calculate at how many (fractional) items distance from the middle
			 * the carousel currently is and start the transition that will move
			 * the now focused cell to the middle.
			 */
			
			startFractionalIndex = fractionalIndex - old.doubleValue() + current.doubleValue();
			transition.playFromStart();
		}
	};
	
	public AbstractCarouselSkin(final TreeView<T> carousel) {
		super(carousel);

		carousel.expandedItemCountProperty().addListener(requestLayout);

		carousel.widthProperty().addListener(requestLayout);
		
		carousel.getFocusModel().focusedIndexProperty().addListener(moveCarousel);

		densityProperty().addListener(requestLayout);
		cellAlignmentProperty().addListener(requestLayout);
		reflectionsEnabledProperty().addListener(requestLayout);
		clipReflectionsProperty().addListener(requestLayout);
		maxCellWidthProperty().addListener(requestLayout);
		maxCellHeightProperty().addListener(requestLayout);
	}

	@Override
	public void dispose() {
		getSkinnable().expandedItemCountProperty().removeListener(requestLayout);

		getSkinnable().widthProperty().removeListener(requestLayout);
		getSkinnable().getFocusModel().focusedIndexProperty().removeListener(moveCarousel);

		densityProperty().removeListener(requestLayout);
		cellAlignmentProperty().removeListener(requestLayout);
		reflectionsEnabledProperty().removeListener(requestLayout);
		clipReflectionsProperty().removeListener(requestLayout);
		maxCellWidthProperty().removeListener(requestLayout);
		maxCellHeightProperty().removeListener(requestLayout);

		for (IndexedCell<?> cell : carouselCells.keySet()) {
			cell.boundsInParentProperty().removeListener(cellBoundsRequestLayout);
		}

		super.dispose();
	}

	protected final Comparator<Node> Z_ORDER_FRACTIONAL = new Comparator<Node>() {
		@Override
		public int compare(Node o1, Node o2) {
			if (!(o1 instanceof TreeCell && o2 instanceof TreeCell)) {
				return 0;
			}

			TreeCell<?> cell1 = (TreeCell<?>) o1;
			TreeCell<?> cell2 = (TreeCell<?>) o2;

			int selectedIndex = getSkinnable().getFocusModel().getFocusedIndex();
			int currentIndex = selectedIndex - (int) Math.round(fractionalIndex);

			int dist1 = Math.abs(cell1.getIndex() - currentIndex);
			int dist2 = Math.abs(cell2.getIndex() - currentIndex);

			return Integer.compare(dist2, dist1);
		}
	};

	@Override
	protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset,
			double leftInset) {
		return 16;
	}

	@Override
	protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset,
			double leftInset) {
		return 16;
	}

	@Override
	protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset,
			double leftInset) {
		return 16;
	}

	@Override
	protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset,
			double leftInset) {
		return 16;
	}

	@Override
	public TreeCell<T> createCell() {
		TreeCell<T> treeCell = super.createCell();

		treeCell.boundsInParentProperty().addListener(cellBoundsRequestLayout);

		return treeCell;
	}

	private final List<LayoutItem> visibleLayoutItems = new ArrayList<>();

	/**
	 * Adds a Cell configured with the given index to the Carousel and returns
	 * the associated {@link LayoutItem}. If no LayoutItem was associated yet
	 * with the given index, a new LayoutItem is created using the
	 * layoutItemProvider.
	 * <p>
	 *
	 * This method is called by Skins during the call to
	 * {@link #getLayoutItems(double)} (which itself is called during the Skin's
	 * {@link #layoutChildren(double, double, double, double)} call). Each time
	 * a layout is performed, all Cells are removed from view. By calling this
	 * method Skins can make Cells visible by having them added to the list of
	 * children of the Carousel.
	 * <p>
	 *
	 * This method reuses Cells and their associated LayoutItems as much as
	 * possible.
	 *
	 * @param index
	 *            the index of the item in the tree for which a LayoutItem
	 *            should be added
	 * @param layoutItemProvider
	 *            a factory that provides {@link LayoutItem} instances
	 * @return the added LayoutItem
	 */
	protected final <I extends LayoutItem> I addVisibleCell(int index, Function<IndexedCell<?>, I> layoutItemProvider) {
		IndexedCell<?> cell = getCellPool().getCell(index);

		getChildren().add(cell); // Add to children so layout calculations are accurate

		@SuppressWarnings("unchecked")
		I item = (I) carouselCells.get(cell);

		if (item == null) {
			item = layoutItemProvider.apply(cell);
			carouselCells.put(cell, item);
		}

		visibleLayoutItems.add(item);

		return item;
	}

	/**
	 * Called during {@link #layoutChildren(double, double, double, double)} to
	 * delegate the layout to a Skin subclass. Implementors should call
	 * {@link #addVisibleCell(int, Function)} as many times as needed to add the
	 * visible cells needed for this layout pass.
	 *
	 * @param fractionalIndex
	 *            the index of the current tree item at the center of the
	 *            carousel, where fractions indicate positions inbetween two
	 *            items. TODO description of FI is wrong
	 */
	protected abstract void delegateLayout(double fractionalIndex);

	@Override
	protected void layoutChildren(double x, double y, double w, double h) {
		Shape currentClip = null;
		Shape cumulativeClip = null;

		getSkinnable().setClip(new Rectangle(x, y, w, h));

		getCellPool().reset();
		getChildren().clear();
		visibleLayoutItems.clear();

		delegateLayout(fractionalIndex);

		/*
		 * Loops through all items and puts the appropriate clip on the
		 * associated cells. If clipping is off, this loop only resets the clips
		 * to null in order to avoid artifacts when switching clipping and/or
		 * reflections on/off.
		 */

		boolean needsClipping = getReflectionsEnabled() && getClipReflections();

		for (LayoutItem item : visibleLayoutItems) {
			Node cell = item.getCell();
			cell.setEffect(item.getEffect());

			layoutInArea(cell, w / 2, h / 2, cell.prefWidth(-1) * cell.getScaleX(),
					cell.prefHeight(-1) * cell.getScaleY(), 0, HPos.CENTER, VPos.CENTER);

			item.getCell().setClip(cumulativeClip);

			if (needsClipping) {
				currentClip = item.createReflectionShape();

				if (currentClip != null) {
					if (cumulativeClip == null) {
						cumulativeClip = new Rectangle(x - w / 2, y - h / 2, w, h);
					}

					cumulativeClip = Shape.subtract(cumulativeClip, currentClip);
				} else if (cumulativeClip != null) {
					cumulativeClip = Shape.union(cumulativeClip, cumulativeClip);
				}
			}
		}

		getCellPool().trim();

		/*
		 * Sort the children and re-add them to the container.
		 */

		List<Node> children = new ArrayList<>(getChildren());

		Collections.sort(children, Z_ORDER_FRACTIONAL);

		getChildren().setAll(children);
	}

	@SuppressWarnings("unused")
	private void debug_showClip(Node cell) {
		Shape shape = (Shape) cell.getClip();

		if (shape != null) {
			Shape copy = Shape.subtract(new Rectangle(-2000, -2000, 4000, 4000), shape);

			layoutInArea(copy, 0, 0, getSkinnable().getWidth(), getSkinnable().getHeight(), 0, HPos.CENTER,
					VPos.CENTER);
			copy.setFill(new Color(1.0, 0.0, 0.0, 0.1));
			getChildren().add(copy);
		}
	}

	public abstract class AbstractLayoutPass<I extends AbstractLayoutItem> {
		private final double fractionalIndex;

		private I currentItem;

		public AbstractLayoutPass(double fractionalIndex) {
			this.fractionalIndex = fractionalIndex;
		}

		public I currentItem() {
			return currentItem;
		}

		public final void createLayout() {
			int index;

			while ((index = nextIndex()) >= 0) {
				currentItem = addLayoutItem(index);
				currentItem.reset();

				/*
				 * Set the item's relative fractional index from which its other
				 * properties can be derived.
				 */

				currentItem.setRelativeFractionalIndex(
						getSkinnable().getFocusModel().getFocusedIndex() - index - fractionalIndex);
				customizeLayoutItem();
			}
		}

		/**
		 * Returns the next index of a LayoutItem to be added in this layout
		 * pass, or -1 if no more items need to be added.
		 *
		 * @return the next index of a LayoutItem to be added in this layout
		 *         pass, or -1 if no more items need to be added
		 */
		protected abstract int nextIndex();

		protected abstract I addLayoutItem(int index);

		protected abstract void customizeLayoutItem();
	}

	public abstract class AbstractLayoutItem implements LayoutItem {
		private static final int REFLECTION_MAX_HEIGHT = 100; // TODO
																// externalize
		private static final double REFLECTION_OPACITY = 0.5; // TODO
																// externalize

		private final WeakReference<IndexedCell<?>> cellRef;

		/*
		 * Input fields:
		 */

		private double translateX;
		private double translateY;
		private double relativeFractionalIndex;

		/*
		 * Derived fields:
		 */

		private Dimension2D normalizedSize;
		private PerspectiveTransform perspectiveTransform;
		private boolean reflectionValid;
		private Reflection reflection; // only valid if reflectionValid is true

		public AbstractLayoutItem(IndexedCell<?> cell) {
			this.cellRef = new WeakReference<IndexedCell<?>>(cell);
		}

		@Override
		public IndexedCell<?> getCell() {
			return cellRef.get();
		}

		@Override
		public Effect getEffect() {
			return getPerspectiveTransform();
		}

		@Override
		public Shape createReflectionShape() {
			Reflection reflection = getReflection();

			if (reflection == null) {
				return null;
			}

			PerspectiveTransform perspectiveTransform = getPerspectiveTransform();
			IndexedCell<?> cell = getCell();
			double cellHeight = getNormalizedSize().getHeight();

			double reflectionY = cellHeight + 2 * getHorizonDistance();
			double fullHeight = reflectionY + cellHeight * reflection.getFraction();

			double reflectionLY = perspectiveTransform.getUly()
					+ (perspectiveTransform.getLly() - perspectiveTransform.getUly()) / fullHeight * reflectionY;
			double reflectionRY = perspectiveTransform.getUry()
					+ (perspectiveTransform.getLry() - perspectiveTransform.getUry()) / fullHeight * reflectionY;

			return new Polygon(perspectiveTransform.getUlx() * cell.getScaleX(), reflectionLY * cell.getScaleY(),
					perspectiveTransform.getUrx() * cell.getScaleX(), reflectionRY * cell.getScaleY(),
					perspectiveTransform.getLrx() * cell.getScaleX(), perspectiveTransform.getLry() * cell.getScaleY(),
					perspectiveTransform.getLlx() * cell.getScaleX(), perspectiveTransform.getLly() * cell.getScaleY());
		}

		public Reflection getReflection() {
			if (!reflectionValid) {
				reflection = null;

				if (getReflectionsEnabled()) {
					reflection = createReflection();
				}

				reflectionValid = true;
			}

			return reflection;
		}

		public PerspectiveTransform getPerspectiveTransform() {
			if (perspectiveTransform == null) {
				perspectiveTransform = createPerspectiveTransform();

				Reflection reflection = getReflection();

				if (reflection != null) {
					perspectiveTransform.setInput(reflection);
				}
			}

			return perspectiveTransform;
		}

		public void setPerspectiveTransform(PerspectiveTransform perspectiveTransform) {
			this.perspectiveTransform = perspectiveTransform;
		}

		void reset() {
			normalizedSize = null;
			perspectiveTransform = null;
			reflectionValid = false;
			translateX = 0;
			translateY = 0;
		}

		/**
		 * Returns the width and height of the cell when it is made to fit
		 * within the MaxCellWidth and MaxCellHeight restrictions while
		 * preserving the aspect ratio.
		 *
		 * @return the normalized dimensions
		 */
		public Dimension2D getNormalizedSize() {
			if (normalizedSize == null) {
				IndexedCell<?> cell = getCell();

				double prefWidth = cell.prefWidth(-1) * cell.getScaleX();
				double prefHeight = cell.prefHeight(-1) * cell.getScaleY();
				double maxCellWidth = getMaxCellWidth() * cell.getScaleX();
				double maxCellHeight = getMaxCellHeight() * cell.getScaleY();

				if (prefWidth > maxCellWidth) {
					prefHeight = prefHeight / prefWidth * maxCellWidth;
					prefWidth = maxCellWidth;
				}
				if (prefHeight > maxCellHeight) {
					prefWidth = prefWidth / prefHeight * maxCellHeight;
					prefHeight = maxCellHeight;
				}

				normalizedSize = new Dimension2D(prefWidth, prefHeight);
			}

			return normalizedSize;
		}

		public double getRelativeFractionalIndex() {
			return relativeFractionalIndex;
		}

		public void setRelativeFractionalIndex(double relativeFractionalIndex) {
			this.relativeFractionalIndex = relativeFractionalIndex;
		}

		public void setTranslation(double translateX, double translateY) {
			this.translateX = translateX;
			this.translateY = translateY;
		}

		public double getTranslateX() {
			return translateX;
		}

		public double getTranslateY() {
			return translateY;
		}

		/**
		 * Gets the cells rectangle adjusted for cell height, cell alignment and
		 * carousel alignment in such a way that coordinate (0,0) is the
		 * baseline of the cell. The reflection portion is also included if
		 * enabled.
		 */
		public Rectangle2D getCellRectangle(double viewAlignment) {
			Dimension2D cellSize = getNormalizedSize();
			IndexedCell<?> cell = getCell();
			double maxCellHeight = getMaxCellHeight() * cell.getScaleY();

			double minX = -0.5 * cellSize.getWidth();
			double minY = -maxCellHeight * viewAlignment + (maxCellHeight - cellSize.getHeight()) * getCellAlignment();
			double width = cellSize.getWidth();
			double height = cellSize.getHeight();

			Reflection reflection = getReflection();

			if (reflection != null) {
				height += 2 * getHorizonDistance() + height * reflection.getFraction();
			}

			return new Rectangle2D(minX, minY, width, height);
		}

		protected Reflection createReflection() {
			double cellHeight = getNormalizedSize().getHeight();
			double horizonDistance = getHorizonDistance();
			double reflectionPortion = (REFLECTION_MAX_HEIGHT - horizonDistance) / cellHeight;

			if (reflectionPortion >= 0 && horizonDistance < REFLECTION_MAX_HEIGHT) {
				double reflectionTopOpacity = REFLECTION_OPACITY
						- REFLECTION_OPACITY / REFLECTION_MAX_HEIGHT * horizonDistance;
				double reflectionBottomOpacity = 0;

				if (reflectionPortion > 0) {
					if (reflectionPortion > 1) {
						reflectionBottomOpacity = REFLECTION_OPACITY - REFLECTION_OPACITY / reflectionPortion;
						reflectionPortion = 1;
					}

					return new Reflection(2 * horizonDistance / cellHeight * getCell().prefHeight(-1),
							reflectionPortion, reflectionTopOpacity, reflectionBottomOpacity);
				}
			}

			return null;
		}

		private double getHorizonDistance() {
			double unusedHeight = getMaxCellHeight() * getCell().getScaleY() - getNormalizedSize().getHeight();

			return unusedHeight - unusedHeight * getCellAlignment();
		}

		protected abstract PerspectiveTransform createPerspectiveTransform();
	}
}
