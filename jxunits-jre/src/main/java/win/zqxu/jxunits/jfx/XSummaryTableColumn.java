package win.zqxu.jxunits.jfx;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.function.Predicate;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import win.zqxu.jxunits.jfx.XSummaryComber.XSummaryOrder;
import win.zqxu.jxunits.jfx.XSummaryComber.XSummarySummer;

/**
 * column for displaying and editing data in {@link XSummaryTableView}
 * <p>
 * the {@link #cellValueFactoryProperty()} was bound to internal value factory, do not
 * unbound it.
 * </p>
 * 
 * <p>
 * use {@link #setSourceValueFactory(Callback)} instead of
 * {@link #setCellValueFactory(Callback)}
 * </p>
 * 
 * @author zqxu
 */
public class XSummaryTableColumn<S, T> extends TableColumn<XSummaryItem<S>, T> {

  public XSummaryTableColumn() {
    this(null);
  }

  public XSummaryTableColumn(String text) {
    super(text);
    sortTypeProperty().addListener(d -> order.set(new SummaryColumnOrder<>(this)));
    comparatorProperty().addListener(d -> order.set(new SummaryColumnOrder<>(this)));
    subtotalGroup.addListener(d -> order.set(new SummaryColumnOrder<>(this)));
    cellValueFactoryProperty().bind(Bindings.createObjectBinding(() -> summaryValueFactory));
  }

  private ReadOnlyObjectWrapper<Predicate<S>> columnPredicate =
      new ReadOnlyObjectWrapper<>(this, "columnPredicate");

  public final ReadOnlyObjectProperty<Predicate<S>> columnPredicateProperty() {
    return columnPredicate.getReadOnlyProperty();
  }

  public final Predicate<S> getColumnPredicate() {
    return columnPredicate.get();
  }

  private ObjectProperty<Predicate<T>> predicate = new ObjectPropertyBase<Predicate<T>>() {
    @Override
    public Object getBean() {
      return XSummaryTableColumn.this;
    }

    @Override
    public String getName() {
      return "predicate";
    }

    @Override
    protected void invalidated() {
      final Predicate<T> predicate = get();
      if (predicate == null)
        columnPredicate.set(null);
      else
        columnPredicate.set(item -> predicate.test(getSourceCellData(item)));
    }
  };

  public final ObjectProperty<Predicate<T>> predicateProperty() {
    return predicate;
  }

  public final Predicate<T> getPredicate() {
    return predicate.get();
  }

  public final void setPredicate(Predicate<T> predicate) {
    this.predicate.set(predicate);
  }

  private BooleanProperty subtotalGroup = new SimpleBooleanProperty(this, "subtotalGroup");

  public final BooleanProperty subtotalGroupProperty() {
    return subtotalGroup;
  }

  /**
   * Determine whether this column was used as subtotal group, default is false.
   * 
   * @return true or false
   */
  public final boolean isSubtotalGroup() {
    return subtotalGroup.get();
  }

  /**
   * Set whether this column was used as subtotal group
   * 
   * @param subtotalGroup
   *          true or false
   */
  public final void setSubtotalGroup(boolean subtotalGroup) {
    this.subtotalGroup.set(subtotalGroup);
  }

  private ReadOnlyObjectWrapper<XSummaryOrder<S, T>> order =
      new ReadOnlyObjectWrapper<>(this, "order", new SummaryColumnOrder<>(this));

  public final ReadOnlyObjectProperty<XSummaryOrder<S, T>> orderProperty() {
    return order.getReadOnlyProperty();
  }

  public final XSummaryOrder<S, T> getOrder() {
    return order.get();
  }

  private ObjectProperty<XSummarySummer<S, T>> summer =
      new SimpleObjectProperty<>(this, "summer", new SummaryColumnSummer<>(this));

  public final ObjectProperty<XSummarySummer<S, T>> summerProperty() {
    return summer;
  }

  /**
   * Get the summary summer instance used to sum column data, default is null.
   * 
   * @return the summary summer instance
   */
  public final XSummarySummer<S, T> getSummer() {
    return summer.get();
  }

  /**
   * Set the summary summer instance used to sum column data
   * 
   * @param summer
   *          the summary summer instance
   */
  public final void setSummer(SummaryColumnSummer<S, T> summer) {
    this.summer.set(summer);
  }

  private Callback<CellDataFeatures<XSummaryItem<S>, T>, ObservableValue<T>> summaryValueFactory =
      cdf -> {
        TableView<XSummaryItem<S>> table = cdf.getTableView();
        if (!(table instanceof XSummaryTableView)) return null;
        XSummaryItem<S> item = cdf.getValue();
        if (item == null) return null;
        XSummaryTableView<S> st = (XSummaryTableView<S>) table;
        if (item.isSummary()) {
          if (st.getSortOrder().contains(cdf.getTableColumn())) {
            return new ReadOnlyObjectWrapper<>(item.getSummaryValue(getOrder()));
          }
          if (st.getSummers().contains(cdf.getTableColumn()))
            return new ReadOnlyObjectWrapper<>(item.getSummaryValue(getSummer()));
          return null;
        }
        Callback<XSummaryDataFeatures<S, T>, ObservableValue<T>> svf = getSourceValueFactory();
        if (svf == null) return null;
        return svf.call(new XSummaryDataFeatures<>(table, this, item.getSourceItem()));
      };

  /**
   * value factory for get value of summary item
   * 
   * @return summary value factory
   */
  public final Callback<CellDataFeatures<XSummaryItem<S>, T>, ObservableValue<T>> getSummaryValueFactory() {
    return summaryValueFactory;
  }

  private ObjectProperty<Callback<XSummaryDataFeatures<S, T>, ObservableValue<T>>> sourceValueFactory =
      new SimpleObjectProperty<>(this, "sourceValueFactory");

  public final ObjectProperty<Callback<XSummaryDataFeatures<S, T>, ObservableValue<T>>> sourceValueFactoryProperty() {
    return sourceValueFactory;
  }

  /**
   * get value factory for got value from source item
   * 
   * @return value factory
   */
  public final Callback<XSummaryDataFeatures<S, T>, ObservableValue<T>> getSourceValueFactory() {
    return sourceValueFactory.get();
  }

  /**
   * set value factory for got value from source item
   * 
   * @param sourceValueFactory
   *          value factory
   */
  public final void setSourceValueFactory(
      Callback<XSummaryDataFeatures<S, T>, ObservableValue<T>> sourceValueFactory) {
    this.sourceValueFactory.set(sourceValueFactory);
  }

  public final T getSourceCellData(S item) {
    Callback<XSummaryDataFeatures<S, T>, ObservableValue<T>> svf = getSourceValueFactory();
    if (svf == null) return null;
    return svf.call(new XSummaryDataFeatures<>(getTableView(), this, item)).getValue();
  }

  public static class XSummaryDataFeatures<S, T> {
    private final TableView<XSummaryItem<S>> tableView;
    private final XSummaryTableColumn<S, T> tableColumn;
    private final S value;

    /**
     * Instantiates a XSummaryDataFeatures instance with the given properties set as
     * read-only values of this instance.
     *
     * @param tableView
     *          The TableView that this instance refers to.
     * @param tableColumn
     *          The TableColumn that this instance refers to.
     * @param value
     *          The value for a row in the TableView.
     */
    public XSummaryDataFeatures(TableView<XSummaryItem<S>> tableView,
        XSummaryTableColumn<S, T> tableColumn, S value) {
      this.tableView = tableView;
      this.tableColumn = tableColumn;
      this.value = value;
    }

    /**
     * Returns the {@link TableView} passed in to the constructor.
     * 
     * @return the table view
     */
    public TableView<XSummaryItem<S>> getTableView() {
      return tableView;
    }

    /**
     * Returns the {@link XSummaryTableColumn} passed in to the constructor.
     * 
     * @return the table column
     */
    public XSummaryTableColumn<S, T> getTableColumn() {
      return tableColumn;
    }

    /**
     * Returns the value passed in to the constructor.
     * 
     * @return the value
     */
    public S getValue() {
      return value;
    }
  }

  private static class SummaryColumnOrder<S, T> implements XSummaryOrder<S, T> {
    private final XSummaryTableColumn<S, T> column;

    public SummaryColumnOrder(XSummaryTableColumn<S, T> column) {
      this.column = column;
    }

    public Comparator<T> getComparator() {
      return column.getComparator();
    }

    public final SortType getSortType() {
      return column.getSortType();
    }

    @Override
    public final T getOrderValue(S item) {
      return column.getSourceCellData(item);
    }

    public final boolean isSubtotalGroup() {
      return column.isSubtotalGroup();
    }
  }

  private static class SummaryColumnSummer<S, T> implements XSummarySummer<S, T> {
    protected final XSummaryTableColumn<S, T> column;

    public SummaryColumnSummer(XSummaryTableColumn<S, T> column) {
      this.column = column;
    }

    @Override
    public final T sum(T summing, S item) {
      return doSummer(summing, 1, item);
    }

    @Override
    public final T subtract(T summing, S item) {
      return doSummer(summing, -1, item);
    }

    @SuppressWarnings("unchecked")
    protected T doSummer(T summing, int sign, S item) {
      if (item == null) return summing;
      T value = column.getSourceCellData(item);
      if (!(value instanceof Number))
        return summing;
      if (!(summing instanceof Number))
        return value;
      Number result = (Number) summing;
      Number operand = (Number) value;
      Class<?> type = result.getClass();
      if (type == BigInteger.class)
        return (T) doAddBigInteger(result, operand);
      if (type == BigDecimal.class)
        return (T) doAddBigDecimal(result, operand);
      double summed = doAddDouble(result, operand);
      if (type == Byte.class)
        return (T) Byte.valueOf((byte) summed);
      if (type == Short.class)
        return (T) Short.valueOf((short) summed);
      if (type == Integer.class)
        return (T) Integer.valueOf((int) summed);
      if (type == Long.class)
        return (T) Long.valueOf((long) summed);
      if (type == Float.class)
        return (T) Float.valueOf((float) summed);
      if (type == Double.class)
        return (T) Double.valueOf(summed);
      return null;
    }

    private BigInteger doAddBigInteger(Number summing, Number operand) {
      if (operand instanceof BigInteger)
        return ((BigInteger) summing).add((BigInteger) operand);
      return ((BigInteger) summing).add(BigInteger.valueOf(operand.longValue()));
    }

    private BigDecimal doAddBigDecimal(Number summing, Number operand) {
      if (operand instanceof BigDecimal)
        return ((BigDecimal) summing).add((BigDecimal) operand);
      return ((BigDecimal) summing).add(BigDecimal.valueOf(operand.doubleValue()));
    }

    private double doAddDouble(Number result, Number operand) {
      return result.doubleValue() + operand.doubleValue();
    }
  }
}
