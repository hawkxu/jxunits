package win.zqxu.jxunits.jfx;

import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
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

class XRangeFieldSkin<T extends Comparable<? super T>> extends SkinBase<XRangeField<T>> {
  private XRangeField<T> control;
  private Button optionButton;
  private XRangeOptionMenu optionMenu;
  private XValueField<T> lowField;
  private Label toLabel;
  private XValueField<T> highField;
  private Button multipleButton;
  private XRangeItem<T> bounding;
  private ListChangeListener<XRangeItem<T>> itemsHandler;
  private ChangeListener<XRangeSign> signHandler;
  private ChangeListener<XRangeOption> optionHandler;

  protected XRangeFieldSkin(XRangeField<T> control) {
    super(control);
    this.control = control;
    initChildren();
    bindRangeControl();
  }

  private void initChildren() {
    getChildren().add(optionButton = new Button());
    optionButton.setGraphic(new ImageView());
    optionButton.setPadding(new Insets(3, 4, 4, 4));
    optionButton.setOnAction(event -> showOptionMenu());
    getChildren().add(lowField = new XValueField<>());
    getChildren().add(toLabel = new Label());
    toLabel.setText(XResource.getString("RANGE.TO"));
    getChildren().add(highField = new XValueField<>());
    getChildren().add(multipleButton = new Button());
    multipleButton.setGraphic(new ImageView());
    multipleButton.setPadding(new Insets(3, 4, 4, 4));
    multipleButton.setOnAction(event -> showRangeEditor());
  }

  private void bindRangeControl() {
    optionButton.disableProperty().bind(control.disabledProperty()
        .or(control.editableProperty().not()));
    lowField.editableProperty().bind(control.editableProperty());
    lowField.disableProperty().bind(control.disabledProperty());
    highField.editableProperty().bind(control.editableProperty());
    highField.disableProperty().bind(control.disabledProperty());
    multipleButton.disableProperty().bind(control.disabledProperty());
    lowField.formatterProperty().bind(control.formatterProperty());
    lowField.providerProperty().bind(control.providerProperty());
    lowField.prefColumnCountProperty().bind(control.prefColumnCountProperty());
    highField.prefColumnCountProperty().bind(control.prefColumnCountProperty());
    if (control.getFormatter() != null)
      highField.setFormatter(control.getFormatter().clone());
    if (control.getProvider() != null)
      highField.setProvider(control.getProvider().clone());
    control.formatterProperty().addListener((v, o, n) -> {
      highField.setFormatter(n == null ? null : n.clone());
    });
    control.providerProperty().addListener((v, o, n) -> {
      highField.setProvider(n == null ? null : n.clone());
    });
    control.itemsProperty().addListener(getItemsHandler());
    updateMultipleButton();
    if (control.itemsProperty().isEmpty())
      updateOptionButton();
    else
      bindRangeItem(control.itemsProperty().get(0));
    lowField.valueProperty().addListener((v, o, n) -> handleLowChanged(n));
    highField.valueProperty().addListener((v, o, n) -> handleHighChanged(n));
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
      lowField.valueProperty().unbindBidirectional(bounding.lowProperty());
      highField.valueProperty().unbindBidirectional(bounding.highProperty());
    }
    bounding = range;
    updateOptionButton();
    if (bounding == null) {
      lowField.setValue(null);
      highField.setValue(null);
    } else {
      bounding.signProperty().addListener(getSignHandler());
      bounding.optionProperty().addListener(getOptionHandler());
      lowField.valueProperty().bindBidirectional(bounding.lowProperty());
      highField.valueProperty().bindBidirectional(bounding.highProperty());
    }
  }

  private void updateOptionButton() {
    ((ImageView) optionButton.getGraphic()).setImage(XRangeItem.getOptionImage(bounding));
  }

  private void updateMultipleButton() {
    Image icon = control.itemsProperty().size() > 1
        ? XImageLoader.get("MULT_MULT.png")
        : XImageLoader.get("MULT_NONE.png");
    ((ImageView) multipleButton.getGraphic()).setImage(icon);
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
        if (bounding.isEmpty())
          control.getItems().remove(0);
        else
          updateOptionButton();
      };
    }
    return optionHandler;
  }

  private void handleLowChanged(T value) {
    if (bounding == null && !XRangeItem.isEmpty(value))
      control.itemsProperty().add(new XRangeItem<>(value));
  }

  private void handleHighChanged(T value) {
    if (bounding == null && !XRangeItem.isEmpty(value))
      control.itemsProperty().add(new XRangeItem<>((T) null, value));
  }

  private void showOptionMenu() {
    if (optionMenu == null) {
      optionMenu = new XRangeOptionMenu();
      optionMenu.setOnAction(event -> handleOptionMenu(event));
    }
    optionMenu.show(optionButton, bounding);
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
    if (editor.show(control, null))
      control.setItems(editor.getRanges());
  }

  @Override
  protected void layoutChildren(double x, double y, double w, double h) {
    layoutInArea(optionButton, x, 0, w, h, 0, Insets.EMPTY, false, false, HPos.LEFT, VPos.CENTER);
    x += optionButton.prefWidth(h) + 2;
    layoutInArea(lowField, x, 0, w, h, 0, Insets.EMPTY, false, false, HPos.LEFT, VPos.CENTER);
    x += lowField.prefWidth(h) + 20;
    layoutInArea(toLabel, x, 0, w, h, 0, Insets.EMPTY, false, false, HPos.LEFT, VPos.CENTER);
    x += toLabel.prefWidth(h) + 20;
    layoutInArea(highField, x, 0, w, h, 0, Insets.EMPTY, false, false, HPos.LEFT, VPos.CENTER);
    x += highField.prefWidth(h) + 5;
    layoutInArea(multipleButton, x, 0, w, h, 0, Insets.EMPTY, false, false, HPos.LEFT, VPos.CENTER);
  }

  @Override
  protected double computePrefWidth(double h, double topInset, double rightInset,
      double bottomInset, double leftInset) {
    return leftInset + rightInset + optionButton.prefWidth(h) + 2 + lowField.prefWidth(h) + 20
        + toLabel.prefWidth(h) + 20 + highField.prefWidth(h) + 5 + multipleButton.prefWidth(h);
  }

  @Override
  protected double computePrefHeight(double w, double topInset, double rightInset,
      double bottomInset, double leftInset) {
    return topInset + bottomInset + Math.max(optionButton.prefHeight(-1), lowField.prefHeight(-1));
  }
}