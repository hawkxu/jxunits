package win.zqxu.jxunits.jfx;

import com.sun.javafx.scene.control.skin.TextFieldSkin;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.StackPane;

@SuppressWarnings("restriction")
class XValueFieldSkin<T> extends TextFieldSkin {
  private XValueField<T> field;
  private StackPane providerPane;

  protected XValueFieldSkin(XValueField<T> field) {
    super(field);
    this.field = field;
    handleFormatterChanged(null, field.getFormatter());
    handleProviderChanged(null, field.getProvider());
    field.formatterProperty().addListener((v, o, n) -> handleFormatterChanged(o, n));
    field.providerProperty().addListener((v, o, n) -> handleProviderChanged(o, n));
  }

  private ChangeListener<T> formatterValueHandler = (v, o, n) -> {
    if (!field.valueProperty().isBound()) field.setValue(n);
  };

  private void handleFormatterChanged(TextFormatter<T> o, TextFormatter<T> n) {
    if (o != null) {
      o.valueProperty().removeListener(formatterValueHandler);
    }
    if (n != null) {
      n.setValue(field.getValue());
      n.valueProperty().addListener(formatterValueHandler);
    }
  }

  private void handleProviderChanged(XValueProvider<T> o, XValueProvider<T> n) {
    if (o != null) {
      o.unbindFromField();
      getChildren().remove(providerPane);
      o.getNode().disableProperty().unbind();
    }
    if (n != null) {
      n.bindToField(field);
      providerPane = new StackPane(n.getNode());
      providerPane.setManaged(false);
      providerPane.setAlignment(Pos.CENTER_RIGHT);
      providerPane.getStyleClass().add("provider");
      getChildren().add(providerPane);
      n.getNode().disableProperty().bind(field.disabledProperty()
          .or(field.editableProperty().not()));
    }
  }

  @Override
  protected void layoutChildren(double x, double y, double w, double h) {
    double fullHeight = h + snappedTopInset() + snappedBottomInset();
    double providerWidth = 0.0;
    if (providerPane != null) {
      w += snappedRightInset();
      providerWidth = snapSize(providerPane.prefWidth(fullHeight));
    }
    double textFieldWidth = w - snapSize(providerWidth);
    super.layoutChildren(snapPosition(x), 0, textFieldWidth, fullHeight);
    if (providerPane != null) {
      double providerStartX = field.getWidth() - providerWidth;
      providerPane.resizeRelocate(providerStartX, 0, providerWidth, fullHeight);
    }
  }

  @Override
  protected double computePrefHeight(double w, double topInset, double rightInset,
      double bottomInset, double leftInset) {
    final double ph = super.computePrefHeight(w, topInset, rightInset, bottomInset, leftInset);
    return Math.max(ph, providerPane == null ? 0.0 : snapSize(providerPane.prefHeight(-1)));
  }
}
