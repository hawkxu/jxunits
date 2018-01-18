package win.zqxu.jxunits.jfx;

import com.sun.javafx.scene.control.skin.TextFieldSkin;

import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

@SuppressWarnings("restriction")
class XValueFieldSkin<T> extends TextFieldSkin {
  private static final PseudoClass HAS_PROVIDER = PseudoClass.getPseudoClass("provider-visible");
  private XValueField<T> field;
  private StackPane providerPane;

  protected XValueFieldSkin(XValueField<T> field) {
    super(field);
    this.field = field;
    handleProviderChanged(null, field.getProvider());
    field.providerProperty().addListener((v, o, n) -> handleProviderChanged(o, n));
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
      providerPane.setAlignment(Pos.CENTER_RIGHT);
      providerPane.getStyleClass().add("provider");
      getChildren().add(providerPane);
      n.getNode().disableProperty().bind(field.disabledProperty()
          .or(field.editableProperty().not()));
    }
    field.pseudoClassStateChanged(HAS_PROVIDER, providerPane != null);
  }

  @Override
  protected void layoutChildren(double x, double y, double w, double h) {
    double fullHeight = h + snappedTopInset() + snappedBottomInset();
    double providerWidth = 0.0;
    if (providerPane != null)
      providerWidth = snapSize(providerPane.prefWidth(fullHeight));
    double textFieldWidth = w - snapSize(providerWidth);
    super.layoutChildren(snapPosition(x), 0, textFieldWidth, fullHeight);
    if (providerPane != null) {
      double providerStartX = w - providerWidth + snappedLeftInset();
      providerPane.resizeRelocate(providerStartX, 0, providerWidth, fullHeight);
    }
  }

  @Override
  protected double computePrefWidth(double h, double topInset, double rightInset,
      double bottomInset, double leftInset) {
    double pw = super.computePrefWidth(h, topInset, rightInset, bottomInset, leftInset);
    return pw + (providerPane == null ? 0.0 : snapSize(providerPane.prefWidth(h)));
  }

  @Override
  protected double computePrefHeight(double w, double topInset, double rightInset,
      double bottomInset, double leftInset) {
    final double ph = super.computePrefHeight(w, topInset, rightInset, bottomInset, leftInset);
    return Math.max(ph, providerPane == null ? 0.0 : snapSize(providerPane.prefHeight(-1)));
  }
}
