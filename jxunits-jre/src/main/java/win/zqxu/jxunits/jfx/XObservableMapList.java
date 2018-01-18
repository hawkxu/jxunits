package win.zqxu.jxunits.jfx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableListBase;
import javafx.collections.ObservableMap;

/**
 * Used to show Map in TableView, for example:
 * <p>
 * tableView.setItems(XJfxUtils.observableMapList(observableMap); <br>
 * tableColumn.setCellValueFactory(new PropertyValueFactory("key")); <br>
 * tableColumn.setCellValueFactory(new PropertyValueFactory("value"));
 * </p>
 * 
 * @author zqxu
 */
public class XObservableMapList<K, V> extends ObservableListBase<Entry<K, V>> {
  private ObservableMap<K, V> map;
  private List<Entry<K, V>> entryList;

  public XObservableMapList(ObservableMap<K, V> map) {
    this.map = map;
    initializeEntryList();
    map.addListener((MapChangeListener<K, V>) change -> handleMapChange(change));
  }

  private void initializeEntryList() {
    entryList = new ArrayList<>();
    for (Entry<K, V> entry : map.entrySet())
      entryList.add(new ObservableEntry(entry));
  }

  @Override
  public Entry<K, V> get(int index) {
    return entryList.get(index);
  }

  @Override
  public int size() {
    return entryList.size();
  }

  private void handleMapChange(Change<? extends K, ? extends V> change) {
    if (change.wasAdded() && change.wasRemoved())
      updateEntry(change);
    else
      fireChange(new MapListChange(change));
  }

  private void updateEntry(Change<? extends K, ? extends V> change) {
    int index = indexOf(change.getKey(), true);
    ((ObservableEntry) entryList.get(index)).value.set(change.getValueAdded());
  }

  private int indexOf(K key, boolean remove) {
    Collection<Entry<K, V>> entries;
    entries = remove ? entryList : map.entrySet();
    int index = 0;
    for (Entry<K, V> entry : entries) {
      if (Objects.equals(key, entry.getKey()))
        return index;
      index++;
    }
    return -1;
  }

  private ObservableEntry createEntry(K key) {
    for (Entry<K, V> entry : map.entrySet())
      if (Objects.equals(entry.getKey(), key))
        return new ObservableEntry(entry);
    return null;
  }

  public class ObservableEntry implements Entry<K, V> {
    private ObjectProperty<K> key;
    private ObjectProperty<V> value;

    public ObservableEntry(Entry<K, V> entry) {
      key = new SimpleObjectProperty<>(entry.getKey());
      value = new SimpleObjectProperty<>(entry.getValue());
    }

    @Override
    public K getKey() {
      return key.get();
    }

    @Override
    public V getValue() {
      return value.get();
    }

    @Override
    public V setValue(V value) {
      throw new UnsupportedOperationException();
    }

    public ObjectProperty<K> keyProperty() {
      return key;
    }

    public ObjectProperty<V> valueProperty() {
      return value;
    }
  }

  private class MapListChange extends ListChangeListener.Change<Entry<K, V>> {
    private int index;
    private List<Entry<K, V>> removed;

    public MapListChange(Change<? extends K, ? extends V> change) {
      super(XObservableMapList.this);
      removed = new ArrayList<>();
      K key = change.getKey();
      if (change.wasRemoved()) {
        index = indexOf(key, true);
        removed.add(entryList.remove(index));
      } else {
        index = indexOf(key, false);
        entryList.add(index, createEntry(key));
      }
    }

    @Override
    public boolean next() {
      return false;
    }

    @Override
    public void reset() {
    }

    @Override
    public int getFrom() {
      return index;
    }

    @Override
    public int getTo() {
      return index + (removed.isEmpty() ? 1 : 0);
    }

    @Override
    public List<Entry<K, V>> getRemoved() {
      return removed;
    }

    @Override
    protected int[] getPermutation() {
      return new int[0];
    }
  }
}
