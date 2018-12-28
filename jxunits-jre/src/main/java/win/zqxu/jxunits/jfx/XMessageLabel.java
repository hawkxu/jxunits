package win.zqxu.jxunits.jfx;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

/**
 * A label with predefined icon and text color for various message type
 * 
 * @author zqxu
 */
public class XMessageLabel extends Label {
  /**
   * Message Type
   * 
   * @author zqxu
   */
  public static enum Type {
    /**
     * Information Message
     */
    INFO,
    /**
     * Busy Message
     */
    BUSY,
    /**
     * Warning Message
     */
    WARN,
    /**
     * Error Message
     */
    ERROR;
  }

  private static final ImageView IMV_INFO;
  private static final ProgressIndicator PGI_BUSY;
  private static final ImageView IMV_WARN;
  private static final ImageView IMV_ERROR;

  static {
    IMV_INFO = new ImageView(XImageLoader.get("information.png"));
    PGI_BUSY = new ProgressIndicator();
    PGI_BUSY.setPrefSize(16, 16);
    PGI_BUSY.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
    IMV_WARN = new ImageView(XImageLoader.get("warning.png"));
    IMV_ERROR = new ImageView(XImageLoader.get("error.png"));
  }

  private ObjectProperty<Type> type = new ObjectPropertyBase<Type>() {
    @Override
    public Object getBean() {
      return XMessageLabel.this;
    }

    @Override
    public String getName() {
      return "type";
    }

    @Override
    protected void invalidated() {
      updateType(getValue());
    }
  };

  /**
   * type property
   * 
   * @return type property
   */
  public final ObjectProperty<Type> typeProperty() {
    return type;
  }

  /**
   * get message type
   * 
   * @return message type
   */
  public final Type getType() {
    return type.get();
  }

  /**
   * set message type
   * 
   * @param type
   *          message type
   */
  public final void setType(Type type) {
    this.type.set(type);
  }

  private void updateType(Type value) {
    if (value == null) {
      setGraphic(null);
      return;
    }
    switch (value) {
    case INFO:
      setGraphic(IMV_INFO);
      setTextFill(Color.BLACK);
      break;
    case BUSY:
      setGraphic(PGI_BUSY);
      setTextFill(Color.BLUE);
      break;
    case WARN:
      setGraphic(IMV_WARN);
      setTextFill(Color.ORANGE);
      break;
    default:
      setGraphic(IMV_ERROR);
      setTextFill(Color.RED);
    }
  }

  /**
   * Clear message, can be safely called outside the JavaFX Application Thread
   */
  public void clear() {
    showMessage(null, null);
  }

  /**
   * show information message, can be safely called outside the JavaFX Application Thread
   * 
   * @param message
   *          the message text
   */
  public void showInfo(String message) {
    showMessage(Type.INFO, message);
  }

  /**
   * show busy message, can be safely called outside the JavaFX Application Thread
   * 
   * @param message
   *          the message text
   */
  public void showBusy(String message) {
    showMessage(Type.BUSY, message);
  }

  /**
   * show warning message, can be safely called outside the JavaFX Application Thread
   * 
   * @param message
   *          the message text
   */
  public void showWarn(String message) {
    showMessage(Type.WARN, message);
  }

  /**
   * show error message, can be safely called outside the JavaFX Application Thread
   * 
   * @param message
   *          the message text
   */
  public void showError(String message) {
    showMessage(Type.ERROR, message);
  }

  /**
   * show message, can be safely called outside the JavaFX Application Thread
   * 
   * @param message
   *          the message text
   */
  public void showMessage(Type type, String message) {
    if (Platform.isFxApplicationThread()) {
      updateMessage(type, message);
    } else {
      Platform.runLater(() -> updateMessage(type, message));
    }
  }

  private void updateMessage(Type type, String message) {
    setType(type);
    setText(message);
  }
}
