package win.zqxu.jxunits.jfx;

import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import win.zqxu.jxunits.jre.XResource;

public class XRangeFieldSkin<T extends Comparable<? super T>>
    extends SkinBase<XRangeField<T>> {
  private static final PseudoClass FIXED = PseudoClass.getPseudoClass("fixed");
  protected final XRangeField<T> control;
  protected Button btnOption;
  protected XRangeOptionMenu mnuOption;
  protected XValueField<T> xvfLow;
  protected Label labTo;
  protected XValueField<T> xvfHigh;
  protected Button btnMultiple;
  private XRangeItem<T> bounding;
  private ListChangeListener<XRangeItem<T>> itemsHandler;
  private ChangeListener<XRangeSign> signHandler;
  private ChangeListener<XRangeOption> optionHandler;

  protected XRangeFieldSkin(XRangeField<T> control) {
    super(control);
    this.control = control;
    initChildren();
    consumeMouseEvents(false);
    handleControlFocused();
    bindRangeControl();
  }

  protected void initChildren() {
    getChildren().add(btnOption = new Button());
    btnOption.getStyleClass().add("option");
    btnOption.setGraphic(new ImageView());
    btnOption.setPadding(new Insets(3, 4, 4, 4));
    btnOption.setOnAction(event -> showOptionMenu());
    getChildren().add(xvfLow = createValueField());
    xvfLow.getStyleClass().add("low");
    getChildren().add(labTo = new Label());
    labTo.getStyleClass().add("to");
    labTo.setText(XResource.getString("RANGE.TO"));
    getChildren().add(xvfHigh = createValueField());
    xvfHigh.getStyleClass().add("high");
    getChildren().add(btnMultiple = new Button());
    btnMultiple.getStyleClass().add("multiple");
    btnMultiple.setGraphic(new ImageView());
    btnMultiple.setPadding(new Insets(3, 4, 4, 4));
    btnMultiple.setOnAction(event -> showRangeEditor());
  }

  protected XValueField<T> createValueField() {
    return new XValueField<>();
  }

  private void handleControlFocused() {
    handleControlFocused(control.isFocused());
    control.focusedProperty().addListener((v, o, n) -> {
      handleControlFocused(n);
    });
    final ChangeListener<Boolean> handler = (v, o, n) -> {
      Boolean focused = XJfxUtils.isFocusOwner(control);
      control.getProperties().put("FOCUSED", focused);
    };
    btnOption.focusedProperty().addListener(handler);
    xvfLow.focusedProperty().addListener(handler);
    xvfHigh.focusedProperty().addListener(handler);
    btnMultiple.focusedProperty().addListener(handler);
  }

  private void handleControlFocused(Boolean focused) {
    Map<Object, Object> props = control.getProperties();
    Object focusLow = props.remove("FOCUS-LOW");
    if (focused && !XJfxUtils.isFocusAncestor(control)) {
      if (focusLow != null
          || btnOption.isDisable()
          || !btnOption.isVisible())
        xvfLow.requestFocus();
      else
        btnOption.requestFocus();
    }
  }

  private void bindRangeControl() {
    btnOption.disableProperty().bind(control.disabledProperty()
        .or(control.editableProperty().not()));
    xvfLow.editableProperty().bind(control.editableProperty());
    xvfLow.disableProperty().bind(control.disabledProperty());
    xvfHigh.editableProperty().bind(control.editableProperty());
    xvfHigh.disableProperty().bind(control.disabledProperty());
    btnMultiple.disableProperty().bind(control.disabledProperty());
    xvfLow.formatterProperty().bind(control.formatterProperty());
    xvfLow.providerProperty().bind(control.providerProperty());
    xvfLow.prefColumnCountProperty().bind(control.prefColumnCountProperty());
    xvfHigh.prefColumnCountProperty().bind(control.prefColumnCountProperty());
    if (control.getFormatter() != null)
      xvfHigh.setFormatter(control.getFormatter().clone());
    if (control.getProvider() != null)
      xvfHigh.setProvider(control.getProvider().clone());
    control.formatterProperty().addListener((v, o, n) -> {
      xvfHigh.setFormatter(n == null ? null : n.clone());
    });
    control.providerProperty().addListener((v, o, n) -> {
      xvfHigh.setProvider(n == null ? null : n.clone());
    });
    control.itemsProperty().addListener(getItemsHandler());
    updateMultipleButton();
    if (control.itemsProperty().isEmpty())
      updateOptionButton();
    else
      bindRangeItem(control.itemsProperty().get(0));
    xvfLow.valueProperty().addListener((v, o, n) -> handleLowChanged(n));
    xvfHigh.valueProperty().addListener((v, o, n) -> handleHighChanged(n));
    control.intervalProperty().addListener((v, o, n) -> control.requestLayout());
    control.multipleProperty().addListener((v, o, n) -> control.requestLayout());
    control.hideOptionProperty().addListener((v, o, n) -> control.requestLayout());
    handleFixedOption(control.getFixedOption());
    control.fixedOptionProperty().addListener((v, o, n) -> handleFixedOption(n));
  }

  private ListChangeListener<XRangeItem<T>> getItemsHandler() {
    if (itemsHandler == null) {
      itemsHandler = changes -> {
        updateMultipleButton();
        bindRangeItem(getSkinnable().getFirstRange());
      };
    }
    return itemsHandler;
  }

  private void bindRangeItem(XRangeItem<T> range) {
    if (bounding == range) return;
    if (bounding != null) {
      bounding.signProperty().removeListener(getSignHandler());
      bounding.optionProperty().removeListener(getOptionHandler());
      xvfLow.valueProperty().unbindBidirectional(bounding.lowProperty());
      xvfHigh.valueProperty().unbindBidirectional(bounding.highProperty());
    }
    bounding = range;
    updateOptionButton();
    if (bounding == null) {
      xvfLow.setValue(null);
      xvfHigh.setValue(null);
    } else {
      bounding.signProperty().addListener(getSignHandler());
      bounding.optionProperty().addListener(getOptionHandler());
      xvfLow.valueProperty().bindBidirectional(bounding.lowProperty());
      xvfHigh.valueProperty().bindBidirectional(bounding.highProperty());
    }
  }

  private void updateOptionButton() {
    ((ImageView) btnOption.getGraphic()).setImage(XRangeItem.getOptionImage(bounding));
  }

  private void updateMultipleButton() {
    Image icon = control.itemsProperty().size() > 1
        ? XImageLoader.get("MULT_MULT.png")
        : XImageLoader.get("MULT_NONE.png");
    ((ImageView) btnMultiple.getGraphic()).setImage(icon);
  }

  private ChangeListener<XRangeSign> getSignHandler() {
    if (signHandler == null) {
      signHandler = (v, o, n) -> {
        if (bounding.isEmpty())
          control.getItems().remove(0);
        else
          updateOptionButton();
      };
    }
    return signHandler;
  }

  private ChangeListener<XRangeOption> getOptionHandler() {
    if (optionHandler == null) {
      optionHandler = (v, o, n) -> {
        fixedOption(bounding);
        if (bounding.isEmpty())
          control.getItems().remove(0);
        else
          updateOptionButton();
      };
    }
    return optionHandler;
  }

  private void fixedOption(XRangeItem<T> range) {
    XRangeOption fixed = control.getFixedOption();
    if (fixed == null) return;
    XRangeOption option = range.getOption();
    if (option != fixed) range.setOption(fixed);
  }

  private void handleLowChanged(T value) {
    if (bounding == null && !XRangeItem.isEmpty(value))
      control.itemsProperty().add(new XRangeItem<>(value));
  }

  private void handleHighChanged(T value) {
    if (bounding == null && !XRangeItem.isEmpty(value))
      control.itemsProperty().add(new XRangeItem<>((T) null, value));
  }

  private void handleFixedOption(XRangeOption fixedOption) {
    control.requestLayout();
    btnOption.pseudoClassStateChanged(FIXED, fixedOption != null);
  }

  private void showOptionMenu() {
    if (control.getFixedOption() != null) return;
    if (mnuOption == null) {
      mnuOption = new XRangeOptionMenu();
      mnuOption.setOnAction(event -> handleOptionMenu(event));
    }
    mnuOption.show(btnOption, bounding);
  }

  private void handleOptionMenu(ActionEvent event) {
    String menuId = ((MenuItem) event.getTarget()).getId();
    if (menuId.equals("RM"))
      control.getItems().remove(0);
    else if (bounding != null)
      bounding.setOption(XRangeOption.valueOf(menuId));
    else {
      XRangeItem<T> range = new XRangeItem<>();
      range.setOption(XRangeOption.valueOf(menuId));
      control.itemsProperty().add(range);
    }
  }

  private void showRangeEditor() {
    XRangeEditor<T> editor = new XRangeEditor<>();
    editor.setRanges(control.getItems());
    editor.setFormatter(control.getFormatter());
    editor.setProvider(control.getProvider());
    editor.setEditable(control.isEditable());
    editor.setInterval(control.isInterval());
    editor.setFixedOption(control.getFixedOption());
    if (editor.show(control, null))
      control.setItems(editor.getRanges());
  }

  @Override
  protected void layoutChildren(double x, double y, double w, double h) {
    btnOption.setVisible(!control.isHideOption());
    if (!control.isHideOption()) {
      layoutInArea(btnOption, x, 0, w, h, 0, Insets.EMPTY, false, false, HPos.LEFT, VPos.CENTER);
      x += btnOption.prefWidth(h) + 2;
    }
    layoutInArea(xvfLow, x, 0, w, h, 0, Insets.EMPTY, false, false, HPos.LEFT, VPos.CENTER);
    x += xvfLow.prefWidth(h);
    boolean interval = isInterval();
    labTo.setVisible(interval);
    xvfHigh.setVisible(interval);
    if (interval) {
      x += 20;
      layoutInArea(labTo, x, 0, w, h, 0, Insets.EMPTY, false, false, HPos.LEFT, VPos.CENTER);
      x += labTo.prefWidth(h) + 20;
      layoutInArea(xvfHigh, x, 0, w, h, 0, Insets.EMPTY, false, false, HPos.LEFT, VPos.CENTER);
      x += xvfHigh.prefWidth(h);
    }
    btnMultiple.setVisible(control.isMultiple());
    if (control.isMultiple()) {
      x += 5;
      layoutInArea(btnMultiple, x, 0, w, h, 0, Insets.EMPTY, false, false, HPos.LEFT, VPos.CENTER);
    }
  }

  @Override
  protected double computePrefWidth(double h, double topInset, double rightInset,
      double bottomInset, double leftInset) {
    double width = leftInset + rightInset;
    if (!control.isHideOption()) width += btnOption.prefWidth(h) + 2;
    width += xvfLow.prefWidth(h);
    if (isInterval()) width += 20 + labTo.prefWidth(h) + 20 + xvfHigh.prefWidth(h);
    return !control.isMultiple() ? width : width + 5 + btnMultiple.prefWidth(h);
  }

  private boolean isInterval() {
    return control.getFixedOption() == null && control.isInterval();
  }

  @Override
  protected double computePrefHeight(double w, double topInset, double rightInset,
      double bottomInset, double leftInset) {
    double prefHeight = xvfLow.prefHeight(-1);
    if (!control.isHideOption()) prefHeight = Math.max(prefHeight, btnOption.prefHeight(-1));
    return topInset + bottomInset + prefHeight;
  }
}