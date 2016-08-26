package org.wipro.svm.carousel.internal.skin;

import javafx.scene.control.IndexedCell;

public interface CellPool<C extends IndexedCell<?>> {

  /**
   * Gets a cell with the given index.  If unavailable, the cell is either
   * taken from the cells available for reuse or newly instantiated if no
   * cells were available.<p>
   *
   * The returned cell is part of the pool and must eventually be returned
   * by resetting the pool to prevent the pool from expanding too much.
   * Subsequent calls for a cell with the same index will return the same
   * cell instance.<p>
   *
   * @param index the index of the cell
   * @return a cell with the given index
   */
  C getCell(int index);

  /**
   * Resets the pool and makes all cells available for reuse.
   */
  void reset();

  /**
   * Discards any cells currently available for reuse.
   */
  void trim();
}
