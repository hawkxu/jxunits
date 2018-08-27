package win.zqxu.jxunits.jfx;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn.SortType;

public class XSummaryComber<S> {
  public XSummaryComber() {
    orders.set(FXCollections.observableArrayList());
    summers.set(FXCollections.observableArrayList());
  }

  private ObjectProperty<Predicate<S>> predicate = new SimpleObjectProperty<>();

  public final ObjectProperty<Predicate<S>> predicateProperty() {
    return predicate;
  }

  /**
   * Get predicate (filter)
   * 
   * @return the predicate
   */
  public final Predicate<S> getPredicate() {
    return predicate.get();
  }

  /**
   * Set predicate (filter)
   * 
   * @param predicate
   *          the predicate
   */
  public final void setPredicate(Predicate<S> predicate) {
    this.predicate.set(predicate);
  }

  private ObjectProperty<List<XSummaryOrder<S, ?>>> orders = new SimpleObjectProperty<>();

  public final ObjectProperty<List<XSummaryOrder<S, ?>>> ordersProperty() {
    return orders;
  }

  /**
   * Get an unmodifiable list of orders
   * 
   * @return the orders
   */
  public final List<XSummaryOrder<S, ?>> getOrders() {
    List<XSummaryOrder<S, ?>> list = orders.get();
    return list == null ? null : Collections.unmodifiableList(list);
  }

  /**
   * Set orders
   * 
   * @param orders
   *          the orders
   */
  public final void setOrders(List<XSummaryOrder<S, ?>> orders) {
    this.orders.set(orders);
  }

  private ObjectProperty<List<XSummarySummer<S, ?>>> summers = new SimpleObjectProperty<>();

  public final ObjectProperty<List<XSummarySummer<S, ?>>> summersProperty() {
    return summers;
  }

  /**
   * Get an unmodifiable list of summers
   * 
   * @return the summers
   */
  public final List<XSummarySummer<S, ?>> getSummers() {
    List<XSummarySummer<S, ?>> list = summers.get();
    return list == null ? null : Collections.unmodifiableList(list);
  }

  /**
   * Set summers
   * 
   * @param summers
   *          the summers
   */
  public final void setSummers(List<XSummarySummer<S, ?>> summers) {
    this.summers.set(summers);
  }

  private BooleanProperty totalProduce = new SimpleBooleanProperty();

  public final BooleanProperty totalProduceProperty() {
    return totalProduce;
  }

  /**
   * Determine whether generate total item, default is false
   * 
   * @return true or false
   */
  public final boolean isTotalProduce() {
    return totalProduce.get();
  }

  /**
   * Set whether generate total item
   * 
   * @param totalProduce
   *          true or false
   */
  public final void setTotalProduce(boolean totalProduce) {
    this.totalProduce.set(totalProduce);
  }

  public static interface XSummaryOrder<S, T> {
    /***
     * Get comparator for compare value
     * 
     * @return comparator
     */
    public Comparator<T> getComparator();

    /**
     * Get sort type for ordering
     * 
     * @return sor type
     */
    public SortType getSortType();

    /**
     * Get ordering value of the item for this order
     * 
     * @param item
     *          the item
     * @return ordering value
     */
    public T getOrderValue(S item);

    /**
     * Indicator for whether this order is subtotal group
     * 
     * @return true or false
     */
    public boolean isSubtotalGroup();
  }

  public static interface XSummarySummer<S, T> {
    /**
     * Sum item to the summing value
     * 
     * @param summing
     *          the summing value, maybe null
     * @param item
     *          the item to sum, maybe null
     * @return summing result
     */
    public T sum(T summing, S item);

    /**
     * Subtract item from the summing value
     * 
     * @param summing
     *          the summing value, maybe null
     * @param item
     *          the item to subtract, maybe null
     * @return summing result
     */
    public T subtract(T summing, S item);
  }
}
