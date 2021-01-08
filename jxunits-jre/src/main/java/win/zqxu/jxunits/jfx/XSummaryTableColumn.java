package win.zqxu.jxunits.jfx;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.function.Predicate;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
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
    updateColumnOrder();
    sortTypeProperty().addListener(v -> updateColumnOrder());
    comparatorProperty().addListener(v -> updateColumnOrder());
    subtotalGroup.addListener(v -> updateColumnOrder());
    cellValueFactoryProperty().bind(
        Bindings.createObjectBinding(() -> summaryValueFactory));
  }

  private ReadOnlyObjectWrapper<Predicate<S>> itemPredicate =
      new ReadOnlyObjectWrapper<>(this, "itemPredicate");

  /**
   * read only item predicate property, building from predicate of this column
   * 
   * @return item predicate property
   */
  public final ReadOnlyObjectProperty<Predicate<S>> itemPredicateProperty() {
    return itemPredicate.getReadOnlyProperty();
  }

  /**
   * Get item predicate, building from predicate of this column
   * 
   * @return item predicate
   */
  public final Predicate<S> getItemPredicate() {
    return itemPredicate.get();
  }

  private Predicate<S> buildItemPredicate(Predicate<T> predicate) {
    if (predicate == null) return null;
    return item -> {
      return predicate.test(getSourceData(item));
    };
  }

  private ObjectProperty<Predicate<T>> predicate =
      new SimpleObjectProperty<Predicate<T>>(this, "predicate") {
        @Override
        protected void invalidated() {
          itemPredicate.set(buildItemPredicate(get()));
        }
      };

  /**
   * column predicate property
   * 
   * @return column predicate property
   */
  public final ObjectProperty<Predicate<T>> predicateProperty() {
    return predicate;
  }

  /**
   * Get column predate to filter items, default is null
   * 
   * @return column predate
   */
  public final Predicate<T> getPredicate() {
    return predicate.get();
  }

  /**
   * Set column predate to filter items, default is null
   * 
   * @param predicate
   *          column predate
   */
  public final void setPredicate(Predicate<T> predicate) {
    this.predicate.set(predicate);
  }

  private BooleanProperty subtotalGroup = new SimpleBooleanProperty(this, "subtotalGroup");

  /**
   * property indicate whether this column was used as sub total group. this property will
   * be ignored if column's sort type is null
   * 
   * @return sub total group property
   */
  public final BooleanProperty subtotalGroupProperty() {
    return subtotalGroup;
  }

  /**
   * Determine whether this column was used as sub total group, default is false. this
   * property will be ignored if column's sort type is null
   * 
   * @return true or false
   */
  public final boolean isSubtotalGroup() {
    return subtotalGroup.get();
  }

  /**
   * Set whether this column was used as sub total group, default is false. this property
   * will be ignored if column's sort type is null
   * 
   * @param subtotalGroup
   *          true or false
   */
  public final void setSubtotalGroup(boolean subtotalGroup) {
    this.subtotalGroup.set(subtotalGroup);
  }

  private ReadOnlyObjectWrapper<XSummaryOrder<S, T>> order =
      new ReadOnlyObjectWrapper<>(this, "order");

  /**
   * read only column order property, follow by sort type, comparator and sub total group
   * property
   * 
   * @return read only column order property
   */
  public final ReadOnlyObjectProperty<XSummaryOrder<S, T>> orderProperty() {
    return order.getReadOnlyProperty();
  }

  /**
   * Get column order to sort items
   * 
   * @return column order
   */
  public final XSummaryOrder<S, T> getOrder() {
    return order.get();
  }

  // items ordering is controlled by column sort type and table view sort order
  // so, we only need change column order every time here
  private void updateColumnOrder() {
    order.set(new XSummaryColumnOrder<>(this));
  }

  private BooleanProperty summaryEnabled = new SimpleBooleanProperty(
      this, "summaryEnabled");

  /**
   * summary enabled property
   * 
   * @return summary enabled property
   */
  public final BooleanProperty summaryEnabledProperty() {
    return this.summaryEnabled;
  }

  /**
   * Determine whether summary enabled on this column
   * 
   * @return true or false
   */
  public final boolean isSummaryEnabled() {
    return this.summaryEnabledProperty().get();
  }

  /**
   * Set whether summary enabled on this column
   * 
   * @param summaryEnabled
   *          true or false
   */
  public final void setSummaryEnabled(boolean summaryEnabled) {
    this.summaryEnabledProperty().set(summaryEnabled);
  }

  private XSummaryColumnSummer<S, T> DEFAULT_SUMMER = new XSummaryColumnSummer<>(this);
  private ObjectProperty<XSummarySummer<S, T>> summer =
      new SimpleObjectProperty<XSummarySummer<S, T>>(this, "summer", DEFAULT_SUMMER) {
        @Override
        public void set(XSummaryComber.XSummarySummer<S, T> newValue) {
          super.set(newValue != null ? newValue : DEFAULT_SUMMER);
        };
      };

  /**
   * summer property used to calculate items summary value, if set to null, then default
   * summer will be used
   * 
   * @return summer property
   */
  public final ObjectProperty<XSummarySummer<S, T>> summerProperty() {
    return summer;
  }

  /**
   * Get summer used to calculate items summary value, default is a
   * {@link XSummaryColumnSummer}
   * 
   * @return the summer
   */
  public final XSummarySummer<S, T> getSummer() {
    return summer.get();
  }

  /**
   * Set summer used to calculate items summary value, if set to null, then default summer
   * will be used
   * 
   * @param summer
   *          the summer
   * @see XSummaryTableColumn.XSummaryColumnSummer
   */
  public final void setSummer(XSummarySummer<S, T> summer) {
    this.summer.set(summer);
  }

  private Callback<CellDataFeatures<XSummaryItem<S>, T>, ObservableValue<T>> summaryValueFactory =
      cdf -> {
        TableView<XSummaryItem<S>> table = cdf.getTableView();
        if (table == null) return null;
        XSummaryItem<S> item = cdf.getValue();
        if (item == null) return null;
        if (item.isSummary()) {
          if (table.getSortOrder().contains(XSummaryTableColumn.this))
            return item.getSummaryValue(getOrder());
          if (getSummer() != null) return item.getSummaryValue(getSummer());
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

  /**
   * Get observable value from source item follow this column
   * 
   * @param item
   *          the source item
   * @return observable value from source item follow this column
   */
  public final ObservableValue<T> getSourceObservableValue(S item) {
    Callback<XSummaryDataFeatures<S, T>, ObservableValue<T>> svf = getSourceValueFactory();
    return svf == null ? null : svf.call(new XSummaryDataFeatures<>(getTableView(), this, item));
  }

  /**
   * Get value from source item follow this column
   * 
   * @param item
   *          the source item
   * @return value from source item follow this column
   */
  public final T getSourceData(S item) {
    ObservableValue<T> observable = getSourceObservableValue(item);
    return observable == null ? null : observable.getValue();
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

  /**
   * Implemented order class to provide oder using column
   * 
   * @author zqxu
   */
  public static class XSummaryColumnOrder<S, T> implements XSummaryOrder<S, T> {
    private final XSummaryTableColumn<S, T> column;

    public XSummaryColumnOrder(XSummaryTableColumn<S, T> column) {
      this.column = column;
    }

    @Override
    public final SortType getSortType() {
      return column.getSortType();
    }

    @Override
    public Comparator<T> getComparator() {
      return column.getComparator();
    }

    @Override
    public final boolean isSubtotalGroup() {
      return column.isSubtotalGroup();
    }

    @Override
    public ObservableValue<T> getObservableValue(S item) {
      return column.getSourceObservableValue(item);
    }
  }

  /**
   * Implemented summer class to calculate number summary value
   * 
   * @author zqxu
   */
  public static class XSummaryColumnSummer<S, T> implements XSummarySummer<S, T> {
    protected final XSummaryTableColumn<S, T> column;

    public XSummaryColumnSummer(XSummaryTableColumn<S, T> column) {
      this.column = column;
    }

    @Override
    public ObservableValue<T> getObservableValue(S item) {
      return column.getSourceObservableValue(item);
    }

    @Override
    public final T sum(T summing, T value) {
      return doSummer(summing, 1, value);
    }

    @Override
    public final T subtract(T summing, T value) {
      return doSummer(summing, -1, value);
    }

    @SuppressWarnings("unchecked")
    protected T doSummer(T summing, int sign, T value) {
      if (!(value instanceof Number)) return summing;
      Number operand = signed(sign, value);
      if (operand == null) return summing;
      if (summing == null) return (T) operand;
      Number result = (Number) summing;
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
      return null; // unsupported type
    }

    private Number signed(int sign, T value) {
      if (!(value instanceof Number)) return null;
      Number number = (Number) value;
      if (sign >= 0) return number;
      Class<?> type = number.getClass();
      if (type == BigInteger.class)
        return ((BigInteger) number).negate();
      if (type == BigDecimal.class)
        return ((BigDecimal) number).negate();
      if (type == Byte.class)
        return Byte.valueOf((byte) -number.byteValue());
      if (type == Short.class)
        return Short.valueOf((short) -number.shortValue());
      if (type == Integer.class)
        return Integer.valueOf(-number.intValue());
      if (type == Long.class)
        return Long.valueOf(-number.longValue());
      if (type == Float.class)
        return Float.valueOf(-number.floatValue());
      if (type == Double.class)
        return Double.valueOf(-number.doubleValue());
      return null; // unsupported type
    }

    private BigInteger doAddBigInteger(Number summing, Number operand) {
      BigInteger result = (BigInteger) summing;
      if (operand instanceof BigInteger) return result.add((BigInteger) operand);
      return result.add(BigInteger.valueOf(operand.longValue()));
    }

    private BigDecimal doAddBigDecimal(Number summing, Number operand) {
      BigDecimal result = (BigDecimal) summing;
      if (operand instanceof BigDecimal) return result.add((BigDecimal) operand);
      return result.add(BigDecimal.valueOf(operand.doubleValue()));
    }

    private double doAddDouble(Number result, Number operand) {
      return result.doubleValue() + operand.doubleValue();
    }
  }
}
