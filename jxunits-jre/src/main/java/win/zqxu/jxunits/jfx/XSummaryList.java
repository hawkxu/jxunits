package win.zqxu.jxunits.jfx;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.scene.control.TableColumn.SortType;
import win.zqxu.jxunits.jfx.XSummaryComber.XSummaryOrder;
import win.zqxu.jxunits.jfx.XSummaryComber.XSummarySummer;
import win.zqxu.jxunits.jre.XObjectUtils;

/**
 * list support filter and summary calculation used in {@link XSummaryTableView}
 * 
 * @author zqxu
 */
public class XSummaryList<S> extends ObservableListBase<XSummaryItem<S>>
    implements ObservableList<XSummaryItem<S>> {
  private ItemComparator<S> itemComparator;
  private List<XSummaryOrder<S, ?>> orders;
  private List<XSummarySummer<S, ?>> summers;
  private boolean totalProduce;
  private SummaryItemImpl<S>[] items;
  private int size;

  public XSummaryList() {
    this(FXCollections.observableArrayList());
  }

  public XSummaryList(ObservableList<S> source) {
    setSource(source);
    setComber(new XSummaryComber<>());
  }

  @Override
  public XSummaryItem<S> get(int index) {
    if (index >= size)
      throw new IndexOutOfBoundsException(index + ">=" + size);
    return items[index];
  }

  @Override
  public int size() {
    return size;
  }

  public boolean isSummary(int index) {
    return get(index).isSummary();
  }

  private ListChangeListener<S> sourceListener = c -> sourceChanged(c);
  private ObjectProperty<ObservableList<S>> source =
      new ObjectPropertyBase<ObservableList<S>>() {
        private WeakReference<ObservableList<S>> oldSourceRef;

        @Override
        public Object getBean() {
          return XSummaryList.this;
        }

        @Override
        public String getName() {
          return "source";
        }

        @Override
        protected void invalidated() {
          ObservableList<S> oldList = oldSourceRef == null ? null : oldSourceRef.get();
          ObservableList<S> newList = get();
          if (newList == oldList) return;
          if (oldList != null) oldList.removeListener(sourceListener);
          doFullGeneration();
          if (newList != null) newList.addListener(sourceListener);
          oldSourceRef = newList == null ? null : new WeakReference<>(newList);
        }
      };

  public final ObjectProperty<ObservableList<S>> sourceProperty() {
    return source;
  }

  /**
   * get source list
   * 
   * @return the source list
   */
  public final ObservableList<S> getSource() {
    return source.get();
  }

  /**
   * Set source list
   * 
   * @param source
   *          the source list
   */
  public final void setSource(ObservableList<S> source) {
    this.source.set(source);
  }

  private ChangeListener<Predicate<S>> predicateListener = (v, o, n) -> handlePredicateChange();
  private ChangeListener<List<XSummaryOrder<S, ?>>> ordersListener =
      (v, o, n) -> handleOrdersChange();
  private ChangeListener<List<XSummarySummer<S, ?>>> summersListener =
      (v, o, n) -> handleSummersChange();
  private ChangeListener<Boolean> totalListener = (v, o, n) -> handleTotalProduceChange();
  private ObjectProperty<XSummaryComber<S>> comber =
      new ObjectPropertyBase<XSummaryComber<S>>() {
        private WeakReference<XSummaryComber<S>> oldComberRef;

        @Override
        public Object getBean() {
          return XSummaryList.this;
        }

        @Override
        public String getName() {
          return "comber";
        }

        @Override
        protected void invalidated() {
          XSummaryComber<S> oldComber = oldComberRef == null ? null : oldComberRef.get();
          XSummaryComber<S> newComber = get();
          if (oldComber == newComber) return;
          if (oldComber != null) {
            oldComber.predicateProperty().removeListener(predicateListener);
            oldComber.ordersProperty().removeListener(ordersListener);
            oldComber.summersProperty().removeListener(summersListener);
            oldComber.totalProduceProperty().removeListener(totalListener);
          }
          buildItemComparator();
          buildOrders();
          buildSummers();
          buildTotalProduce();
          doFullGeneration();
          if (newComber != null) {
            newComber.predicateProperty().addListener(predicateListener);
            newComber.ordersProperty().addListener(ordersListener);
            newComber.summersProperty().addListener(summersListener);
            newComber.totalProduceProperty().addListener(totalListener);
          }
        }
      };

  public final ObjectProperty<XSummaryComber<S>> comberProperty() {
    return comber;
  }

  /**
   * Get summary comber
   * 
   * @return the summary comber
   */
  public final XSummaryComber<S> getComber() {
    return comber.get();
  }

  /**
   * Set summary comber
   * 
   * @param comber
   *          the summary comber
   */
  public final void setComber(XSummaryComber<S> comber) {
    this.comber.set(comber);
  }

  private void handlePredicateChange() {
    doFullGeneration();
  }

  private void handleOrdersChange() {
    buildItemComparator();
    buildOrders();
    doSortAndSummary();
  }

  private void handleSummersChange() {
    List<XSummarySummer<S, ?>> oldSummers = summers;
    buildSummers();
    if (summers != null)
      doSummaryOnly();
    else if (oldSummers != null)
      doRemoveSummary();
  }

  private void handleTotalProduceChange() {
    buildTotalProduce();
    doTotalSummary();
  }

  private void buildItemComparator() {
    itemComparator = null;
    XSummaryComber<S> comber = getComber();
    if (comber == null) return;
    List<XSummaryOrder<S, ?>> orders = comber.getOrders();
    if (orders == null || orders.isEmpty()) return;
    itemComparator = new ItemComparator<>(orders);
  }

  private void buildOrders() {
    orders = null;
    XSummaryComber<S> comber = getComber();
    if (comber == null) return;
    List<XSummaryOrder<S, ?>> list = comber.getOrders();
    if (!XObjectUtils.isEmpty(list)) orders = list;
  }

  private void buildSummers() {
    summers = null;
    XSummaryComber<S> comber = getComber();
    if (comber == null) return;
    List<XSummarySummer<S, ?>> list = comber.getSummers();
    if (!XObjectUtils.isEmpty(list)) summers = list;
  }

  private void buildTotalProduce() {
    totalProduce = false;
    XSummaryComber<S> comber = getComber();
    if (comber == null) return;
    totalProduce = comber.isTotalProduce();
  }

  private void doFullGeneration() {
    ObservableList<S> source = getSource();
    Predicate<S> predicate = getPredicate();
    beginChange();
    try {
      for (int i = 0; i < size; i++)
        nextRemove(i, items[i]);
      size = 0;
      items = null;
      ensureSize(source.size());
      for (int i = 0; i < source.size(); i++) {
        S item = source.get(i);
        if (predicate == null || predicate.test(item))
          addSourceItem(i, item);
      }
    } finally {
      endChange();
    }
  }

  private Predicate<S> getPredicate() {
    XSummaryComber<S> comber = getComber();
    return comber == null ? null : comber.getPredicate();
  }

  private void doSortAndSummary() {
    if (size == 0) return;
    SummaryItemImpl<S>[] temp = items;
    int count = size;
    beginChange();
    try {
      size = 0;
      items = null;
      ensureSize(count);
      for (int i = 0; i < count; i++) {
        if (!temp[i].summary)
          addSourceItem(temp[i].sourceIndex, temp[i].sourceItem);
      }
    } finally {
      endChange();
    }
  }

  private void doSummaryOnly() {
    if (size == 0) return;
    beginChange();
    try {
      for (int i = 0; i < size; i++) {
        if (!items[i].summary) {
          updateTotal(true, items[i]);
          updateSubtotal(true, i, items[i]);
        }
      }
    } finally {
      endChange();
    }
  }

  private void doRemoveSummary() {
    beginChange();
    try {
      for (int i = size - 1; i >= 0; i--) {
        if (items[i].summary) removeItem(i);
      }
    } finally {
      endChange();
    }
  }

  private void doTotalSummary() {
    beginChange();
    try {
      if (totalProduce) {
        for (int i = 0; i < size; i++) {
          if (!items[i].summary) updateTotal(true, items[i]);
        }
      } else if (size > 0) {
        SummaryItemImpl<S> last = items[size - 1];
        if (last.summary && !last.subtotal)
          removeItem(size - 1);
      }
    } finally {
      endChange();
    }
  }

  private void sourceChanged(Change<? extends S> c) {
    beginChange();
    try {
      while (c.next()) {
        if (c.wasUpdated() || c.wasReplaced())
          handleSourceUpdated(c);
        else if (c.wasAdded())
          handleSourceAdded(c);
        else if (c.wasRemoved())
          handleSourceRemoved(c);
      }
    } finally {
      endChange();
    }
  }

  private void handleSourceUpdated(Change<? extends S> c) {
    ObservableList<? extends S> list = c.getList();
    for (int i = c.getFrom(); i < c.getTo(); i++) {
      removeSourceItem(i);
      addSourceItem(i, list.get(i));
    }
  }

  private int findSourceItem(int sourceIndex) {
    for (int i = 0; i < size; i++) {
      if (items[i].sourceIndex == sourceIndex)
        return i;
    }
    return -1;
  }

  private void handleSourceAdded(Change<? extends S> c) {
    int sourceIndex = c.getFrom();
    int addedCount = c.getAddedSize();
    updateIndices(sourceIndex, addedCount);
    List<? extends S> list = c.getAddedSubList();
    for (int i = 0; i < addedCount; i++) {
      addSourceItem(sourceIndex + i, list.get(i));
    }
  }

  private void handleSourceRemoved(Change<? extends S> c) {
    int sourceIndex = c.getFrom();
    int removeCount = c.getRemovedSize();
    for (int i = 0; i < removeCount; i++) {
      removeSourceItem(sourceIndex + i);
    }
    updateIndices(sourceIndex, -removeCount);
  }

  private void addSourceItem(int sourceIndex, S sourceItem) {
    SummaryItemImpl<S> unsorted = new SummaryItemImpl<>(sourceIndex, sourceItem);
    int index = findInsertPos(unsorted);
    addItem(index, unsorted);
    updateTotal(true, unsorted);
    updateSubtotal(true, index, unsorted);
  }

  private int findInsertPos(SummaryItemImpl<S> item) {
    if (itemComparator != null) {
      int pos;
      for (pos = 0; pos < size; pos++) {
        if (itemComparator.compare(item, items[pos]) < 0) break;
      }
      return pos;
    } else if (size > 0 && items[size - 1].summary) {
      return size - 1;
    }
    return size;
  }

  private void addItem(int index, SummaryItemImpl<S> unsorted) {
    ensureSize(size + 1);
    System.arraycopy(items, index, items, index + 1, size - index);
    items[index] = unsorted;
    size++;
    nextAdd(index, index + 1);
  }

  @SuppressWarnings("unchecked")
  private void ensureSize(int size) {
    if (items == null) items = new SummaryItemImpl[0];
    if (items.length < size) {
      SummaryItemImpl<S>[] replacement = new SummaryItemImpl[size * 3 / 2 + 1];
      System.arraycopy(items, 0, replacement, 0, this.size);
      items = replacement;
    }
  }

  private void updateTotal(boolean add, SummaryItemImpl<S> item) {
    if (summers != null && totalProduce)
      updateSummary(new SummaryItemImpl<>(false), add, 0, item);
  }

  private void updateSubtotal(boolean add, int index, SummaryItemImpl<S> item) {
    if (orders == null || summers == null) return;
    List<XSummaryOrder<S, ?>> groupKeys = new ArrayList<>();
    for (XSummaryOrder<S, ?> order : orders) {
      groupKeys.add(order);
      if (!order.isSubtotalGroup()) continue;
      SummaryItemImpl<S> subtotal = new SummaryItemImpl<>(true);
      for (XSummaryOrder<S, ?> key : groupKeys)
        subtotal.orderValues.put(key, key.getOrderValue(item.sourceItem));
      updateSummary(subtotal, add, index, item);
    }
  }

  @SuppressWarnings("unchecked")
  private void updateSummary(SummaryItemImpl<S> summary, boolean add, int from,
      SummaryItemImpl<S> item) {
    int index = findSummaryPos(summary, from);
    if (index >= 0) summary = items[index];
    for (XSummarySummer<S, ?> summer : summers) {
      Object summing = summary.summerValues.get(summer);
      XSummarySummer<S, Object> generic = (XSummarySummer<S, Object>) summer;
      if (add)
        summing = generic.sum(summing, item.sourceItem);
      else
        summing = generic.subtract(summing, item.sourceItem);
      summary.count += add ? 1 : -1;
      summary.summerValues.put(summer, summing);
    }
    if (!add && summary.count == 0) {
      removeItem(index);
    } else if (index >= 0) {
      items[index] = summary;
      nextUpdate(index);
    } else {
      index = -index;
      addItem(index, summary);
    }
  }

  private int findSummaryPos(SummaryItemImpl<S> summary, int from) {
    SummaryItemImpl<S> last = items[size - 1];
    int pos;
    if (summary.isSubtotal()) {
      for (pos = from; pos < size; pos++) {
        int sort = itemComparator.compare(summary, items[pos]);
        if (sort == 0) return pos;
        if (sort < 0) return -pos;
      }
      return -size;
    }
    if (last != null && last.summary && !last.subtotal)
      return size - 1;
    return -size;
  }

  private void removeSourceItem(int sourceIndex) {
    int index = findSourceItem(sourceIndex);
    if (index >= 0) {
      SummaryItemImpl<S> removed = items[index];
      removeItem(index);
      updateTotal(false, removed);
      updateSubtotal(false, index, removed);
    }
  }

  private void removeItem(int index) {
    XSummaryItem<S> removed = items[index];
    System.arraycopy(items, index + 1, items, index, size - index - 1);
    items[--size] = null;
    nextRemove(index, removed);
  }

  private void updateIndices(int from, int difference) {
    for (int i = 0; i < size; i++) {
      if (items[i].sourceIndex >= from)
        items[i].sourceIndex += difference;
    }
  }

  private static class ItemComparator<S> implements Comparator<XSummaryItem<S>> {
    private List<XSummaryOrder<S, ?>> orders;

    public ItemComparator(List<XSummaryOrder<S, ?>> orders) {
      this.orders = orders;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compare(XSummaryItem<S> o1, XSummaryItem<S> o2) {
      for (XSummaryOrder<S, ?> order : orders) {
        if (o1.isSummary() && !o1.isSubtotal())
          return 1;
        if (o2.isSummary() && !o2.isSubtotal())
          return -1;
        Object v1 = getComparingValue(order, o1);
        Object v2 = getComparingValue(order, o2);
        if (v1 == v2) continue;
        if (o1.isSummary() && v1 == null)
          return 1;
        if (o2.isSummary() && v2 == null)
          return -1;
        Comparator<Object> comparator = (Comparator<Object>) order.getComparator();
        int result;
        if (order.getSortType() == SortType.ASCENDING)
          result = comparator.compare(v1, v2);
        else
          result = comparator.compare(v2, v1);
        if (result != 0) return result;
      }
      if (o1.isSummary() == o2.isSummary()) return 0;
      return o1.isSummary() ? 1 : -1;
    }

    private Object getComparingValue(XSummaryOrder<S, ?> order, XSummaryItem<S> item) {
      if (item.isSummary())
        return item.getSummaryValue(order);
      return order.getOrderValue(item.getSourceItem());
    }
  }

  private static class SummaryItemImpl<S> implements XSummaryItem<S> {
    private int sourceIndex = -1;
    private S sourceItem;
    private boolean summary;
    private boolean subtotal;
    private int count;
    private Map<XSummaryOrder<S, ?>, Object> orderValues;
    private Map<XSummarySummer<S, ?>, Object> summerValues;

    public SummaryItemImpl(int sourceIndex, S sourceItem) {
      this.sourceIndex = sourceIndex;
      this.sourceItem = sourceItem;
    }

    public SummaryItemImpl(boolean subtotal) {
      this.summary = true;
      this.subtotal = subtotal;
      this.orderValues = new HashMap<>();
      this.summerValues = new HashMap<>();
    }

    @Override
    public int getSourceIndex() {
      return sourceIndex;
    }

    @Override
    public S getSourceItem() {
      return sourceItem;
    }

    @Override
    public boolean isSummary() {
      return summary;
    }

    @Override
    public boolean isSubtotal() {
      return subtotal;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getSummaryValue(XSummaryOrder<S, T> order) {
      return (T) orderValues.get(order);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getSummaryValue(XSummarySummer<S, T> summer) {
      return (T) summerValues.get(summer);
    }
  }
}
