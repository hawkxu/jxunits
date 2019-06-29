package win.zqxu.jxunits.jfx;

import java.util.Comparator;
import java.util.function.Predicate;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn.SortType;

public class XSummaryComber<S> {
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

  private ObservableList<XSummaryOrder<S, ?>> orders = FXCollections.observableArrayList();

  /**
   * Get observable orders list, can change order by the list
   * 
   * @return observable orders list
   */
  public final ObservableList<XSummaryOrder<S, ?>> getOrders() {
    return orders;
  }

  private ObservableList<XSummarySummer<S, ?>> summers = FXCollections.observableArrayList();

  /**
   * Get observable summers list, can change summer by the list
   * 
   * @return observable summers list
   */
  public final ObservableList<XSummarySummer<S, ?>> getSummers() {
    return summers;
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

  /**
   * summary order interface
   * 
   * @author zqxu
   */
  public static interface XSummaryOrder<S, T> {
    /**
     * Get sort type for ordering
     * 
     * @return sort type
     */
    public SortType getSortType();

    /***
     * Get comparator for compare value
     * 
     * @return comparator
     */
    public Comparator<T> getComparator();

    /**
     * Indicate whether this order is sub total group
     * 
     * @return true or false
     */
    public boolean isSubtotalGroup();

    /**
     * Get observable value for this order from the item
     * 
     * @param item
     *          the item
     * @return observable value
     */
    public ObservableValue<T> getObservableValue(S item);
  }

  /**
   * summary summer interface
   * 
   * @author zqxu
   */
  public static interface XSummarySummer<S, T> {
    /**
     * Get observable value for this summer from the item
     * 
     * @param item
     *          the item
     * @return observable value
     */
    public ObservableValue<T> getObservableValue(S item);

    /**
     * Sum value with the summing value
     * 
     * @param summing
     *          the summing value, maybe null
     * @param value
     *          the value to sum, maybe null
     * @return summing result
     */
    public T sum(T summing, T value);

    /**
     * Subtract value from the summing value
     * 
     * @param summing
     *          the summing value, maybe null
     * @param value
     *          the value to subtract, maybe null
     * @return summing result
     */
    public T subtract(T summing, T value);
  }
}
