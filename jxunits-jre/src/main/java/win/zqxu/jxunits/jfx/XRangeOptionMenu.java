package win.zqxu.jxunits.jfx;

import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import win.zqxu.jxunits.jre.XResource;

class XRangeOptionMenu extends ContextMenu {
  private XRangeItem<?> range;

  public XRangeOptionMenu() {
    setAutoHide(true);
    getItems().add(createMenuItem("RM"));
    getItems().add(createMenuItem("EQ"));
    getItems().add(createMenuItem("NE"));
    getItems().add(createMenuItem("LT"));
    getItems().add(createMenuItem("LE"));
    getItems().add(createMenuItem("GT"));
    getItems().add(createMenuItem("GE"));
    getItems().add(createMenuItem("CP"));
    getItems().add(createMenuItem("NP"));
    getItems().add(createMenuItem("BT"));
    getItems().add(createMenuItem("NB"));
  }

  public void show(Node owner, XRangeItem<?> range) {
    this.range = range;
    updateMenuItems();
    show(owner, Side.BOTTOM, 0, 0);
  }

  private MenuItem createMenuItem(String menuId) {
    MenuItem item = new MenuItem();
    item.setId(menuId);
    item.setGraphic(new ImageView());
    item.setText(XResource.getString("RANGE.OPTION." + menuId));
    return item;
  }

  private void updateMenuItems() {
    for (MenuItem item : getItems()) {
      String menuId = item.getId();
      item.setVisible(decideOptionMenuVisible(menuId));
      if (item.isVisible()) {
        ImageView graphic = (ImageView) item.getGraphic();
        graphic.setImage(getMenuImage(menuId));
      }
    }
  }

  private boolean decideOptionMenuVisible(String menuId) {
    boolean empty = XRangeItem.isEmpty(range);
    XRangeOption option = empty ? null : range.getOption();
    boolean between = option == XRangeOption.BT || option == XRangeOption.NB;
    switch (menuId) {
    case "RM":
      return !empty;
    case "EQ":
    case "NE":
      return empty || !between;
    case "LT":
    case "LE":
    case "GT":
    case "GE":
      return !empty && !between && !XRangeItem.isEmpty(range.getLow());
    case "CP":
    case "NP":
      return !empty && !between && XRangeItem.containsWildcard(range.getLow());
    case "BT":
    case "NB":
      return between;
    default:
      throw new IllegalArgumentException("unkown menu id: " + menuId);
    }
  }

  private Image getMenuImage(String id) {
    if (id.equals("RM")) return XImageLoader.get("OPTION_RMV.png");
    XRangeSign sign = range == null ? null : range.getSign();
    return XRangeItem.getOptionImage(sign, XRangeOption.valueOf(id));
  }
}
