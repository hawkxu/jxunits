package win.zqxu.jxunits.jfx;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
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
 * the items property was bound to summaryItems, do not unbound it.
 * </p>
 * <p>
 * the comber property of summaryItems was bound to internal comber in table, do not
 * unbound it.
 * </p>
 * <p>
 * use {@link #setSourceItems(ObservableList)} to set items instead of
 * {@link #setItems(ObservableList)}.
 * </p>
 * <p>
 * Because the summaryItems was read only, all changes must be made through the source
 * items.
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
 * <li>{@link XSummaryTableColumn#setSummer(XSummaryColumnSummer)}</li>
 * <li>{@link #getPredicates()}</li>
 * <li>{@link #getSortOrder()}</li>
 * <li>{@link #getSummers()}</li>
 * <li>{@link #setTotalRowProduce(boolean)}</li>
 * <li>{@link #setSourceItems(ObservableList)}</li>
 * </ul>
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
    getStyleClass().add("x-summary-table");
    setSortPolicy(table -> true); // just do nothing
    setRowFactory(item -> new XSummaryTableRow<>());
    setSourceItems(items);
    summaryItems.comberProperty().bind(comber);
    itemsProperty().bind(Bindings.createObjectBinding(() -> summaryItems));
    getPredicates().addListener((ListChangeListener<? super XSummaryTableColumn<S, ?>>) c -> {
      handlePredicatesChange(c);
    });
    getSortOrder().addListener((ListChangeListener<TableColumn<XSummaryItem<S>, ?>>) c -> {
      handleOrdersChange(c);
    });
    getSummers().addListener((ListChangeListener<XSummaryTableColumn<S, ?>>) c -> {
      handleSummersChange(c);
    });
    totalRowProduce.addListener((v, o, n) -> updateTotalRowProduce());
  }

  private XSummaryList<S> summaryItems = new XSummaryList<>();

  /**
   * Get summary items, the summary items contains source items and summary records.
   * 
   * @return summary items
   */
  public final XSummaryList<S> getSummaryItems() {
    return summaryItems;
  }

  /**
   * Get source items contained in summary items
   * 
   * @return the source items
   */
  public final ObservableList<S> getSourceItems() {
    return getSummaryItems().getSource();
  }

  /**
   * Set source items to summary items
   * 
   * @param sourceItems
   *          the source items
   */
  public final void setSourceItems(ObservableList<S> sourceItems) {
    summaryItems.setSource(sourceItems);
  }

  /**
   * disable summary row edit
   */
  @Override
  public void edit(int row, TableColumn<XSummaryItem<S>, ?> column) {
    if (row >= 0 && !summaryItems.isSummary(row)) super.edit(row, column);
  }

  private ObservableList<XSummaryTableColumn<S, ?>> summers = FXCollections.observableArrayList();

  public ObservableList<XSummaryTableColumn<S, ?>> getSummers() {
    return summers;
  }

  private ObservableList<XSummaryTableColumn<S, ?>> predicates =
      FXCollections.observableArrayList();

  public ObservableList<XSummaryTableColumn<S, ?>> getPredicates() {
    return predicates;
  }

  private BooleanProperty totalRowProduce =
      new SimpleBooleanProperty(this, "totalRowProduce", false);

  public final BooleanProperty totalRowProduceProperty() {
    return totalRowProduce;
  }

  public final boolean isTotalRowProduce() {
    return totalRowProduce.get();
  }

  public final void setTotalRowProduce(final boolean totalRowProduce) {
    this.totalRowProduce.set(totalRowProduce);
  }

  private int updateLocker = 0;

  public final void beginUpdateSummary() {
    updateLocker++;
  }

  public final void endUpdateSummary() {
    if (updateLocker == 0)
      throw new IllegalStateException("no match beginUpdateSummary found");
    if (--updateLocker == 0) comber.set(buildSComber());
  }

  private ReadOnlyObjectWrapper<XSummaryComber<S>> comber =
      new ReadOnlyObjectWrapper<>(this, "comber", buildSComber());

  private XSummaryComber<S> buildSComber() {
    XSummaryComber<S> comber = new XSummaryComber<>();
    comber.setPredicate(buildPredicate());
    comber.setOrders(buildOrders());
    comber.setSummers(buildSummers());
    comber.setTotalProduce(isTotalRowProduce());
    return comber;
  }

  private Predicate<S> buildPredicate() {
    Predicate<S> predicate = null;
    for (XSummaryTableColumn<S, ?> column : predicates) {
      Predicate<S> next = column.getColumnPredicate();
      if (next == null) continue;
      if (predicate == null)
        predicate = next;
      else
        predicate = predicate.and(next);
    }
    return predicate;
  }

  private List<XSummaryOrder<S, ?>> buildOrders() {
    List<XSummaryOrder<S, ?>> orders = new ArrayList<>();
    ObservableList<TableColumn<XSummaryItem<S>, ?>> columns = getColumns();
    for (TableColumn<XSummaryItem<S>, ?> column : getSortOrder()) {
      if (column.isSortable() && columns.contains(column)
          && column instanceof XSummaryTableColumn) {
        XSummaryOrder<S, ?> order = ((XSummaryTableColumn<S, ?>) column).getOrder();
        if (order != null) orders.add(order);
      }
    }
    return orders;
  }

  private List<XSummarySummer<S, ?>> buildSummers() {
    List<XSummarySummer<S, ?>> summers = new ArrayList<>();
    ObservableList<TableColumn<XSummaryItem<S>, ?>> columns = getColumns();
    for (XSummaryTableColumn<S, ?> column : getSummers()) {
      if (columns.contains(column) && column instanceof XSummaryTableColumn) {
        XSummarySummer<S, ?> summer = ((XSummaryTableColumn<S, ?>) column).getSummer();
        if (summer != null) summers.add(summer);
      }
    }
    return summers;
  }

  private void updatePredicate() {
    if (updateLocker == 0) comber.get().setPredicate(buildPredicate());
  }

  private void updateOrders() {
    if (updateLocker == 0) comber.get().setOrders(buildOrders());
  }

  private void updateSummers() {
    if (updateLocker == 0) comber.get().setSummers(buildSummers());
  }

  private void updateTotalRowProduce() {
    if (updateLocker == 0) comber.get().setTotalProduce(isTotalRowProduce());
  }

  private Map<XSummaryTableColumn<S, ?>, InvalidationListener> columnPredicateListeners =
      new HashMap<>();
  private Map<XSummaryTableColumn<S, ?>, InvalidationListener> columnOrderListeners =
      new HashMap<>();
  private Map<XSummaryTableColumn<S, ?>, InvalidationListener> columnSummerListeners =
      new HashMap<>();

  private void handlePredicatesChange(Change<? extends XSummaryTableColumn<S, ?>> c) {
    updatePredicate();
    while (c.next()) {
      if (c.wasAdded()) {
        for (XSummaryTableColumn<S, ?> column : c.getAddedSubList()) {
          column.columnPredicateProperty().addListener(getColumnPredicateListener(column));
        }
      }
      if (c.wasRemoved()) {
        for (XSummaryTableColumn<S, ?> column : c.getRemoved()) {
          column.columnPredicateProperty().removeListener(columnPredicateListeners.remove(column));
        }
      }
    }
  }

  private InvalidationListener getColumnPredicateListener(XSummaryTableColumn<S, ?> column) {
    InvalidationListener listener = columnPredicateListeners.get(column);
    if (listener == null) {
      columnPredicateListeners.put(column, listener = d -> updatePredicate());
    }
    return listener;
  }

  private void handleOrdersChange(Change<? extends TableColumn<XSummaryItem<S>, ?>> c) {
    updateOrders();
    while (c.next()) {
      if (c.wasAdded()) {
        for (TableColumn<XSummaryItem<S>, ?> column : c.getAddedSubList()) {
          if (column instanceof XSummaryTableColumn)
            ((XSummaryTableColumn<S, ?>) column).orderProperty()
                .addListener(getColumnOrderListener((XSummaryTableColumn<S, ?>) column));
        }
      }
      if (c.wasRemoved()) {
        for (TableColumn<XSummaryItem<S>, ?> column : c.getAddedSubList()) {
          ((XSummaryTableColumn<S, ?>) column).orderProperty()
              .removeListener(columnOrderListeners.remove(column));
        }
      }
    }
  }

  private InvalidationListener getColumnOrderListener(XSummaryTableColumn<S, ?> column) {
    InvalidationListener listener = columnOrderListeners.get(column);
    if (listener == null) {
      columnOrderListeners.put(column, listener = d -> updateOrders());
    }
    return listener;
  }

  private void handleSummersChange(Change<? extends XSummaryTableColumn<S, ?>> c) {
    updateSummers();
    while (c.next()) {
      if (c.wasAdded()) {
        for (XSummaryTableColumn<S, ?> column : c.getAddedSubList()) {
          column.summerProperty().addListener(getColumnSummerListener(column));
        }
      }
      if (c.wasRemoved()) {
        for (XSummaryTableColumn<S, ?> column : c.getRemoved()) {
          column.summerProperty().removeListener(columnSummerListeners.remove(column));
        }
      }
    }
  }

  private InvalidationListener getColumnSummerListener(XSummaryTableColumn<S, ?> column) {
    InvalidationListener listener = columnSummerListeners.get(column);
    if (listener == null) {
      columnSummerListeners.put(column, listener = d -> updateSummers());
    }
    return listener;
  }

  @Override
  public String getUserAgentStylesheet() {
    return XValueField.class.getResource("x-styles.css").toExternalForm();
  }
}
