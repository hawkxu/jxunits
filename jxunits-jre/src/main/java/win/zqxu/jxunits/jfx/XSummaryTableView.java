package win.zqxu.jxunits.jfx;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import win.zqxu.jxunits.jfx.XSummaryComber.XSummaryOrder;
import win.zqxu.jxunits.jfx.XSummaryComber.XSummarySummer;

/**
 * the XSummaryTableView designed to support automatic calculate and show sub-total and
 * summary rows.
 * <p>
 * the items should be {@link XSummaryList} type. usually set items like <b>setItems(new
 * XSummaryList&lt;&gt;(source))</b>
 * </p>
 * <p>
 * the comber property of <code>XSummaryList</code> was bound to internal comber in table,
 * do not unbound it. and do not change any property of the comber.
 * </p>
 * <p>
 * Because the <code>XSummaryList</code> was read only, all changes must be made through
 * the source list.
 * </p>
 * <p>
 * Please do not call {@link #setSortPolicy(javafx.util.Callback)} to change the sort
 * policy
 * </p>
 * <p>
 * The columns added to table should be {@link XSummaryTableColumn}, to update summary
 * list, using below methods
 * 
 * <ul>
 * <li>{@link XSummaryTableColumn#setPredicate(Predicate)}</li>
 * <li>{@link XSummaryTableColumn#setSortType(TableColumn.SortType)}</li>
 * <li>{@link XSummaryTableColumn#setComparator(Comparator)}</li>
 * <li>{@link XSummaryTableColumn#setSubtotalGroup(boolean)}</li>
 * <li>{@link XSummaryTableColumn#setSummaryEnabled(boolean)}</li>
 * <li>{@link XSummaryTableColumn#setSummer(XSummaryComber.XSummarySummer)}</li>
 * <li>{@link #setTotalRowProduce(boolean)}</li>
 * </ul>
 * <p>
 * for batch update summary list, call {@link #beginUpdateSummary()} and
 * {@link #endUpdateSummary()} around above methods in pairs
 * </p>
 * 
 * <p>
 * the rowFactory was set to default create {@link XSummaryTableRow} for apply pseudo
 * class 'summary' to summary row
 * </p>
 * 
 * <p>
 * for forced re-regenerate summary list, just call {@link #beginUpdateSummary()} and
 * {@link #endUpdateSummary()}
 * </p>
 * 
 * @author zqxu
 */
public class XSummaryTableView<S> extends TableView<XSummaryItem<S>> {

  public XSummaryTableView() {
    this(FXCollections.observableArrayList());
  }

  public XSummaryTableView(ObservableList<S> items) {
    super(new XSummaryList<>(items));
    getStyleClass().add("x-summary-table");
    setSortPolicy(table -> true); // just do nothing
    setRowFactory(item -> new XSummaryTableRow<>());
    getSortOrder().addListener(sortOrderHandler);
    getColumns().addListener(columnsHandler);
    handleItemsChanged(null, getItems());
    itemsProperty().addListener((v, o, n) -> handleItemsChanged(o, n));
  }

  /**
   * disable summary row edit
   */
  @Override
  public void edit(int row, TableColumn<XSummaryItem<S>, ?> column) {
    if (row >= 0 && getItems().get(row).isSummary())
      super.edit(-1, null);
    else
      super.edit(row, column);
  }

  private ObservableList<Predicate<S>> predicates = FXCollections.observableArrayList();

  /**
   * Get unmodifiable predicates, follow columns change
   * 
   * @return unmodifiable predicates
   */
  public ObservableList<Predicate<S>> getUnmodifiablePredicates() {
    return FXCollections.unmodifiableObservableList(predicates);
  }

  /**
   * Get unmodifiable orders, follow columns change
   * 
   * @return unmodifiable orders
   */
  public final ObservableList<XSummaryOrder<S, ?>> getUnmodifiableOrders() {
    return FXCollections.unmodifiableObservableList(comber.get().getOrders());
  }

  /**
   * Get unmodifiable summers, follow columns change
   * 
   * @return unmodifiable summers
   */
  public ObservableList<XSummarySummer<S, ?>> getUnmodifiableSummers() {
    return FXCollections.unmodifiableObservableList(comber.get().getSummers());
  }

  private BooleanProperty totalRowProduce = new SimpleBooleanProperty(
      this, "totalRowProduce", true) {
    @Override
    protected void invalidated() {
      if (updateLocker == 0) comber.get().setTotalProduce(get());
    }
  };

  /**
   * total row produce property, default value is true
   * 
   * @return total row produce property
   */
  public final BooleanProperty totalRowProduceProperty() {
    return totalRowProduce;
  }

  /**
   * Determine whether produce total row, default is true
   * 
   * @return true or false
   */
  public final boolean isTotalRowProduce() {
    return totalRowProduce.get();
  }

  /**
   * Set whether produce total row, default is true
   * 
   * @param totalRowProduce
   *          true or false
   */
  public final void setTotalRowProduce(final boolean totalRowProduce) {
    this.totalRowProduce.set(totalRowProduce);
  }

  private ObjectProperty<XSummaryComber<S>> comber = new SimpleObjectProperty<>(
      this, "comber", rebuildComber());

  private XSummaryComber<S> rebuildComber() {
    XSummaryComber<S> building = new XSummaryComber<>();
    rebuildPredicates();
    building.setPredicate(buildComberPredicate());
    building.getOrders().setAll(buildComberOrders());
    building.getSummers().setAll(buildComberSummers());
    building.setTotalProduce(isTotalRowProduce());
    return building;
  }

  private void rebuildPredicates() {
    predicates.clear();
    XSummaryTableColumn<S, ?> column;
    for (TableColumn<XSummaryItem<S>, ?> loop : getColumns()) {
      if (!(loop instanceof XSummaryTableColumn)) continue;
      column = (XSummaryTableColumn<S, ?>) loop;
      Predicate<S> predicate = column.getItemPredicate();
      if (predicate != null) predicates.add(predicate);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Predicate<S> buildComberPredicate() {
    if (predicates.isEmpty()) return null;
    Predicate combine = predicates.get(0);
    for (int i = 1; i < predicates.size(); i++)
      combine = combine.and(predicates.get(i));
    return (Predicate<S>) combine;
  }

  private List<XSummaryOrder<S, ?>> buildComberOrders() {
    List<XSummaryOrder<S, ?>> orders = new ArrayList<>();
    for (TableColumn<XSummaryItem<S>, ?> column : getSortOrder()) {
      if (column instanceof XSummaryTableColumn)
        orders.add(((XSummaryTableColumn<S, ?>) column).getOrder());
    }
    return orders;
  }

  private List<XSummarySummer<S, ?>> buildComberSummers() {
    List<XSummarySummer<S, ?>> summers = new ArrayList<>();
    for (TableColumn<XSummaryItem<S>, ?> column : getLeafColumns()) {
      if (column instanceof XSummaryTableColumn) {
        XSummaryTableColumn<S, ?> xc = (XSummaryTableColumn<S, ?>) column;
        if (xc.isSummaryEnabled()) summers.add(xc.getSummer());
      }
    }
    return summers;
  }

  private List<TableColumn<XSummaryItem<S>, ?>> getLeafColumns() {
    List<TableColumn<XSummaryItem<S>, ?>> leafColumns = new ArrayList<>();
    fillLeafColumns(leafColumns, getColumns());
    return leafColumns;
  }

  private void fillLeafColumns(List<TableColumn<XSummaryItem<S>, ?>> leafColumns,
      List<TableColumn<XSummaryItem<S>, ?>> columns) {
    for (TableColumn<XSummaryItem<S>, ?> column : columns) {
      if (column.getColumns().isEmpty())
        leafColumns.add(column);
      else
        fillLeafColumns(leafColumns, column.getColumns());
    }
  }

  private int updateLocker = 0;

  /**
   * begin batch change summary settings, like filters, orders, etc.
   */
  public final void beginUpdateSummary() {
    updateLocker++;
  }

  /**
   * end batch change summary settings, invoke in pairs with {@link #beginUpdateSummary()}
   */
  public final void endUpdateSummary() {
    if (updateLocker == 0)
      throw new IllegalStateException("no match beginUpdateSummary found");
    if (--updateLocker == 0) comber.set(rebuildComber());
  }

  private InvalidationListener sortOrderHandler = v -> {
    if (updateLocker == 0) comber.get().getOrders().setAll(buildComberOrders());
  };

  private ListChangeListener<TableColumn<XSummaryItem<S>, ?>> columnsHandler = c -> {
    boolean changed = false;
    while (c.next()) {
      changed = true;
      if (c.wasAdded()) handleColumnsAdded(c.getAddedSubList());
      if (c.wasRemoved()) handleColumnsRemoved(c.getRemoved());
    }
    if (changed) comber.set(rebuildComber());
  };

  private void handleColumnsAdded(List<? extends TableColumn<XSummaryItem<S>, ?>> added) {
    for (TableColumn<XSummaryItem<S>, ?> column : added) {
      if (column instanceof XSummaryTableColumn)
        installSummaryColumnListener((XSummaryTableColumn<S, ?>) column);
    }
  }

  private void handleColumnsRemoved(List<? extends TableColumn<XSummaryItem<S>, ?>> removed) {
    for (TableColumn<XSummaryItem<S>, ?> column : removed) {
      if (column instanceof XSummaryTableColumn)
        uninstallSummaryColumnListener((XSummaryTableColumn<S, ?>) column);
    }
  }

  private void installSummaryColumnListener(XSummaryTableColumn<S, ?> column) {
    column.itemPredicateProperty().addListener(getColumnPredicateHandler(column));
    column.orderProperty().addListener(getColumnOrderHandler(column));
    column.summaryEnabledProperty().addListener(getColumnSummaryHandler(column));
    column.summerProperty().addListener(getColumnSummerHandler(column));
  }

  private void uninstallSummaryColumnListener(XSummaryTableColumn<S, ?> column) {
    column.itemPredicateProperty().removeListener(columnPredicateHandlers.remove(column));
    column.orderProperty().removeListener(columnOrderHandlers.remove(column));
    column.summaryEnabledProperty().removeListener(columnSummaryHandlers.remove(column));
    column.summerProperty().removeListener(columnSummerHandlers.remove(column));
  }

  private Map<Object, ChangeListener<Predicate<S>>> columnPredicateHandlers = new HashMap<>();
  private Map<Object, ChangeListener<XSummaryOrder<S, ?>>> columnOrderHandlers = new HashMap<>();
  private Map<Object, ChangeListener<Boolean>> columnSummaryHandlers = new HashMap<>();
  private Map<Object, ChangeListener<XSummarySummer<S, ?>>> columnSummerHandlers = new HashMap<>();

  private ChangeListener<Predicate<S>> getColumnPredicateHandler(Object column) {
    if (!columnPredicateHandlers.containsKey(column)) {
      columnPredicateHandlers.put(column, (v, o, n) -> {
        if (updateLocker != 0) return;
        if (o != null) predicates.remove(o);
        if (n != null) predicates.add(n);
        comber.get().setPredicate(buildComberPredicate());
      });
    }
    return columnPredicateHandlers.get(column);
  }

  private ChangeListener<XSummaryOrder<S, ?>> getColumnOrderHandler(Object column) {
    if (!columnOrderHandlers.containsKey(column)) {
      columnOrderHandlers.put(column, (v, o, n) -> {
        // if the column not in sort order, then the order
        // change must be processed by sort order handler
        if (updateLocker == 0 && getSortOrder().contains(column))
          comber.get().getOrders().setAll(buildComberOrders());
      });
    }
    return columnOrderHandlers.get(column);
  }

  private ChangeListener<Boolean> getColumnSummaryHandler(Object column) {
    if (!columnSummaryHandlers.containsKey(column)) {
      columnSummaryHandlers.put(column, (v, o, n) -> {
        if (updateLocker == 0)
          comber.get().getSummers().setAll(buildComberSummers());
      });
    }
    return columnSummaryHandlers.get(column);
  }

  private ChangeListener<XSummarySummer<S, ?>> getColumnSummerHandler(Object column) {
    if (!columnSummerHandlers.containsKey(column)) {
      columnSummerHandlers.put(column, (v, o, n) -> {
        if (updateLocker == 0)
          comber.get().getSummers().setAll(buildComberSummers());
      });
    }
    return columnSummerHandlers.get(column);
  }

  private void handleItemsChanged(ObservableList<XSummaryItem<S>> o,
      ObservableList<XSummaryItem<S>> n) {
    if (o != null) o.removeListener(summaryItemsChangedHandler);
    if (o instanceof XSummaryList)
      ((XSummaryList<S>) o).comberProperty().unbind();
    if (n instanceof XSummaryList)
      ((XSummaryList<S>) n).comberProperty().bind(comber);
    if (n != null) n.addListener(summaryItemsChangedHandler);
  }

  private ListChangeListener<XSummaryItem<S>> summaryItemsChangedHandler =
      c -> handleSummaryItemsChanged(c);

  private void handleSummaryItemsChanged(Change<? extends XSummaryItem<S>> c) {
    XSummaryItem<S> selected = getSelectionModel().getSelectedItem();
    if (selected == null) return;
    int index = -1, sourceIndex = selected.getSourceIndex();
    while (c.next()) {
      if (c.wasAdded()) {
        List<? extends XSummaryItem<S>> list = c.getAddedSubList();
        for (int i = 0; i < list.size(); i++) {
          if (list.get(i).getSourceIndex() == sourceIndex) {
            index = c.getFrom() + i;
            break;
          }
        }
      }
    }
    final int restore = index;
    if (restore != -1) Platform.runLater(() -> getSelectionModel().select(restore));
  }

  @Override
  public String getUserAgentStylesheet() {
    return XSummaryTableView.class.getResource("x-styles.css").toExternalForm();
  }
}
