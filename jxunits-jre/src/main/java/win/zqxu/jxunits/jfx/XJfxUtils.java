package win.zqxu.jxunits.jfx;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import win.zqxu.jxunits.jre.XObjectUtils;
import win.zqxu.jxunits.jre.XResource;

public class XJfxUtils {
  private static final Map<ButtonType, Image> BUTTON_ICONS = new HashMap<>();
  private static final Logger logger = Logger.getLogger(XJfxUtils.class.getName());

  static {
    setButtonIcon(ButtonType.OK, XImageLoader.get("accept.png"));
    setButtonIcon(ButtonType.YES, XImageLoader.get("accept.png"));
    setButtonIcon(ButtonType.CANCEL, XImageLoader.get("cross.png"));
    setButtonIcon(ButtonType.NO, XImageLoader.get("cross.png"));
  }

  /**
   * Set button icon for dialog created by FXUtils
   * 
   * @param button
   *          the button type
   * @param url
   *          the icon URL for the button
   */
  public static void setButtonIcon(ButtonType button, Image url) {
    BUTTON_ICONS.put(button, url);
  }

  /**
   * show information message
   * 
   * @param owner
   *          a reference node in the owner window
   * @param message
   *          the message text
   */
  public static void showMessage(Node owner, String message) {
    showAlert(owner, AlertType.INFORMATION, message);
  }

  /***
   * show warning message
   * 
   * @param owner
   *          a reference node in the owner window
   * @param message
   *          the message text
   */
  public static void showWarn(Node owner, String message) {
    showAlert(owner, AlertType.WARNING, message);
  }

  /**
   * show error message
   * 
   * @param owner
   *          a reference node in the owner window
   * @param message
   *          the message text
   */
  public static void showError(Node owner, String message) {
    showAlert(owner, AlertType.ERROR, message);
  }

  /**
   * show a confirmation message with Yes/No buttons
   * 
   * @param owner
   *          a reference node in the owner window
   * @param message
   *          the message text
   * @return true if the Yes button clicked
   */
  public static boolean showYesNo(Node owner, String message) {
    return showAlert(owner, AlertType.CONFIRMATION, message, ButtonType.YES,
        ButtonType.NO) == ButtonType.YES;
  }

  /**
   * show a confirmation message with OK/Cancel buttons
   * 
   * @param owner
   *          a reference node in the owner window
   * @param message
   *          the message text
   * @return true if the OK button clicked
   */
  public static boolean showOkCancel(Node owner, String message) {
    return showAlert(owner, AlertType.CONFIRMATION, message, ButtonType.OK,
        ButtonType.CANCEL) == ButtonType.OK;
  }

  /**
   * show a confirmation message with Yes/No/Cancel buttons
   * 
   * @param owner
   *          a reference node in the owner window
   * @param message
   *          the message text
   * @return the button which close the confirmation
   */
  public static ButtonType showYesNoCancel(Node owner, String message) {
    return showAlert(owner, AlertType.CONFIRMATION, message,
        ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
  }

  /**
   * show an alert dialog with specified type and message and buttons
   * 
   * @param owner
   *          a reference node in the owner window
   * @param alertType
   *          the alert type
   * @param message
   *          the message text
   * @param buttonTypes
   *          buttons on alert dialog
   * @return the alert dialog closed by which button
   */
  public static ButtonType showAlert(Node owner, AlertType alertType,
      String message, ButtonType... buttonTypes) {
    Alert alert = new Alert(alertType);
    initOwnerModality(alert, owner, false);
    alert.setHeaderText(message);
    if (!XObjectUtils.isEmpty(buttonTypes)) {
      alert.getButtonTypes().clear();
      alert.getButtonTypes().addAll(buttonTypes);
    }
    updateDialogButtonIcons(alert.getDialogPane());
    alert.showAndWait();
    return alert.getResult();
  }

  /**
   * show an input dialog and returns user input text
   * 
   * @param owner
   *          a reference node in the owner window
   * @param header
   *          the header text
   * @return user input text or null if dialog has being cancelled
   */
  public static String showInput(Node owner, String header) {
    return showInput(owner, header, "");
  }

  /**
   * show an input dialog and returns user input text
   * 
   * @param owner
   *          a reference node in the owner window
   * @param header
   *          the header text
   * @param defaultValue
   *          the default value
   * @return user input text or null if dialog has being cancelled
   */
  public static String showInput(Node owner, String header, String defaultValue) {
    return showInput(owner, header, defaultValue, XPatternFormatter.STRING(".*"));
  }

  /**
   * show an input dialog and returns user input value
   * 
   * @param <T>
   *          the input value type
   * @param owner
   *          a reference node in the owner window
   * @param header
   *          the header text
   * @param defaultValue
   *          the default value
   * @param formatter
   *          the formatter for input
   * @return user input value or null if dialog has being cancelled
   */
  public static <T> T showInput(Node owner, String header,
      T defaultValue, TextFormatter<T> formatter) {
    TextField field = new TextField();
    VBox content = new VBox(field);
    content.setPadding(new Insets(10, 10, 0, 30));
    Dialog<ButtonType> dialog = createDialog(owner, content,
        XResource.getString("INPUT_TITLE"));
    dialog.setHeaderText(header);
    try {
      formatter.setValue(defaultValue);
      field.setTextFormatter(formatter);
      Platform.runLater(() -> {
        field.requestFocus();
      });
      dialog.showAndWait();
    } finally {
      field.setTextFormatter(null); // release formatter
    }
    ButtonType result = dialog.getResult();
    return result == ButtonType.OK ? formatter.getValue() : null;
  }

  /**
   * Create a dialog with specified content and OK, Cancel buttons
   * 
   * @param owner
   *          a reference node in the owner window
   * @param content
   *          content node for the created dialog
   * @param title
   *          dialog title
   * @return created dialog
   * @see #createDialog(Node, Node, String, ButtonType...)
   */
  public static Dialog<ButtonType> createDialog(Node owner, Node content, String title) {
    return createDialog(owner, content, title, ButtonType.OK, ButtonType.CANCEL);
  }

  /**
   * Create a dialog with specified content and buttons, this function will initialize
   * dialog owner and dialog modality type, and set icon for buttons
   * 
   * @param owner
   *          a reference node in the owner window
   * @param content
   *          content node for the created dialog
   * @param title
   *          dialog title
   * @param buttons
   *          buttons will be created in dialog
   * @return created dialog
   * @see #setButtonIcon(ButtonType, Image)
   */
  public static Dialog<ButtonType> createDialog(Node owner, Node content, String title,
      ButtonType... buttons) {
    Dialog<ButtonType> dialog = new Dialog<>();
    initOwnerModality(dialog, owner, false);
    DialogPane dialogPane = new DialogPane();
    dialog.setDialogPane(dialogPane);
    dialog.setTitle(title);
    dialogPane.setContent(content);
    dialogPane.getButtonTypes().addAll(buttons);
    updateDialogButtonIcons(dialogPane);
    return dialog;
  }

  /**
   * update dialog button icons, use {@link #setButtonIcon(ButtonType, Image)} to set icon
   * for button type.
   * 
   * @param pane
   *          the dialog pane
   */
  public static void updateDialogButtonIcons(DialogPane pane) {
    for (ButtonType type : pane.getButtonTypes()) {
      Image icon = BUTTON_ICONS.get(type);
      if (icon == null) continue;
      Node button = pane.lookupButton(type);
      if (!(button instanceof Button)) continue;
      ((Button) button).setGraphic(new ImageView(icon));
    }
  }

  /**
   * apply class to node
   * 
   * @param node
   *          the node
   * @param className
   *          the class name
   */
  public static void applyClass(Node node, String className) {
    ObservableList<String> classList = node.getStyleClass();
    if (!classList.contains(className)) classList.add(className);
  }

  /**
   * remove class from node
   * 
   * @param node
   *          the node
   * @param className
   *          the class name
   */
  public static void removeClass(Node node, String className) {
    node.getStyleClass().remove(className);
  }

  /**
   * catch uncaught exception, show exception dialog
   */
  public static void catchUncaughtException() {
    Thread.setDefaultUncaughtExceptionHandler((t, ex) -> {
      Throwable cause = ex;
      if (ex instanceof RuntimeException
          && ex.getCause() instanceof InvocationTargetException)
        cause = ex.getCause();
      while (cause instanceof InvocationTargetException) {
        cause = cause.getCause();
      }
      final Throwable real = cause == null ? ex : cause;
      // to avoid animation and layout processing
      Platform.runLater(() -> showUncaughtException(real));
    });
  }

  private static void showUncaughtException(Throwable cause) {
    try {
      showException(null, null, cause);
    } catch (Throwable ex) {
      logger.log(Level.WARNING, "exception in showUncaughtException", ex);
      logger.log(Level.WARNING, "original uncaughted exception", cause);
    }
  }

  /**
   * show exception dialog
   * 
   * @param owner
   *          a reference node in the owner window
   * @param ex
   *          exception object
   */
  public static void showException(Node owner, Throwable ex) {
    showException(owner, null, ex);
  }

  /**
   * show exception dialog with specific dialog title
   * 
   * @param owner
   *          a reference node in the owner window
   * @param title
   *          the dialog title
   * @param ex
   *          exception object
   */
  public static void showException(Node owner, String title, Throwable ex) {
    if (Platform.isFxApplicationThread())
      internalShowException(owner, title, ex);
    else
      Platform.runLater(() -> internalShowException(owner, title, ex));
  }

  private static void internalShowException(Node owner, String title, Throwable ex) {
    StringBuilder message = new StringBuilder();
    Throwable cause = ex;
    while (cause != null) {
      String msg = cause.getMessage();
      if (msg != null && !msg.isEmpty()) {
        if (msg.length() > 80)
          msg = msg.substring(0, 80) + " ...";
        if (message.indexOf(msg) == -1) {
          if (message.length() > 0)
            message.append("\n");
          message.append(msg);
        }
      }
      cause = cause.getCause();
    }
    StringWriter stacks = new StringWriter();
    ex.printStackTrace(new PrintWriter(stacks));
    TextArea expand = new TextArea(stacks.toString());
    expand.setEditable(false);
    expand.setPrefHeight(300);
    Alert alert = new Alert(AlertType.ERROR);
    initOwnerModality(alert, owner, false);
    if (title != null) alert.setTitle(title);
    alert.setHeaderText(message.toString());
    alert.getDialogPane().setExpandableContent(expand);
    alert.showAndWait();
  }

  /**
   * show directory selection dialog and get user selected directory
   * 
   * @param owner
   *          a reference node in the owner window
   * @param initialDirectory
   *          initial directory
   * @return the selected directory or null if no directory has been selected
   */
  public static File chooseDirectory(Node owner, File initialDirectory) {
    return chooseDirectory(owner, null, initialDirectory);
  }

  /**
   * show directory selection dialog and get user selected directory
   * 
   * @param owner
   *          a reference node in the owner window
   * @param title
   *          the dialog title
   * @param initialDirectory
   *          initial directory
   * @return the selected directory or null if no directory has been selected
   */
  public static File chooseDirectory(Node owner, String title, File initialDirectory) {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle(title);
    chooser.setInitialDirectory(initialDirectory);
    return chooser.showDialog(owner == null ? null : owner.getScene().getWindow());
  }

  /**
   * show file open dialog and get user selected file
   * 
   * @param owner
   *          a reference node in the owner window
   * @param initialFile
   *          initial file
   * @param filters
   *          extension filters
   * @return the selected file or null if no file has been selected
   */
  public static File chooseOpenFile(Node owner, File initialFile, ExtensionFilter... filters) {
    return chooseOpenFile(owner, null, initialFile, filters);
  }

  /**
   * show file open dialog and get user selected file
   * 
   * @param owner
   *          a reference node in the owner window
   * @param title
   *          the dialog title
   * @param initialFile
   *          initial file
   * @param filters
   *          extension filters
   * @return the selected file or null if no file has been selected
   */
  public static File chooseOpenFile(Node owner, String title, File initialFile,
      ExtensionFilter... filters) {
    FileChooser chooser = createFileChooser(title, initialFile, filters);
    return chooser.showOpenDialog(owner == null ? null : owner.getScene().getWindow());
  }

  /**
   * show file open dialog allow select multiple files and get user selected files
   * 
   * @param owner
   *          a reference node in the owner window
   * @param initialFile
   *          initial file
   * @param filters
   *          extension filters
   * @return the selected file or null if no file has been selected
   */
  public static List<File> chooseOpenMultiFiles(Node owner, File initialFile,
      ExtensionFilter... filters) {
    return chooseOpenMultiFiles(owner, null, initialFile, filters);
  }

  /**
   * show file open dialog allow select multiple files and get user selected files
   * 
   * @param owner
   *          a reference node in the owner window
   * @param title
   *          the dialog title
   * @param initialFile
   *          initial file
   * @param filters
   *          extension filters
   * @return the selected file or null if no file has been selected
   */
  public static List<File> chooseOpenMultiFiles(Node owner, String title, File initialFile,
      ExtensionFilter... filters) {
    FileChooser chooser = createFileChooser(title, initialFile, filters);
    return chooser.showOpenMultipleDialog(owner == null ? null : owner.getScene().getWindow());
  }

  /**
   * show file save dialog and get user selected file
   * 
   * @param owner
   *          a reference node in the owner window
   * @param initialFile
   *          initial file
   * @param filters
   *          extension filters
   * @return the selected file or null if no file has been selected
   */
  public static File chooseSaveFile(Node owner, File initialFile, ExtensionFilter... filters) {
    return chooseSaveFile(owner, null, initialFile, filters);
  }

  /**
   * show file save dialog and get user selected file
   * 
   * @param owner
   *          a reference node in the owner window
   * @param title
   *          the dialog title
   * @param initialFile
   *          initial file
   * @param filters
   *          extension filters
   * @return the selected file or null if no file has been selected
   */
  public static File chooseSaveFile(Node owner, String title, File initialFile,
      ExtensionFilter... filters) {
    FileChooser chooser = createFileChooser(title, initialFile, filters);
    return chooser.showSaveDialog(owner == null ? null : owner.getScene().getWindow());
  }

  private static FileChooser createFileChooser(String title, File initialFile,
      ExtensionFilter... filters) {
    FileChooser chooser = new FileChooser();
    if (title != null) chooser.setTitle(title);
    if (filters.length > 0) {
      chooser.getExtensionFilters().addAll(filters);
      chooser.setSelectedExtensionFilter(filters[0]);
    }
    if (initialFile != null) {
      if (!initialFile.isDirectory()) {
        String name = initialFile.getName();
        chooser.setInitialFileName(name);
        name = name.replaceFirst("^.*\\.", "");
        for (ExtensionFilter filter : filters) {
          for (String extension : filter.getExtensions())
            if (name.equalsIgnoreCase(extension))
              chooser.setSelectedExtensionFilter(filter);
        }
        initialFile = initialFile.getParentFile();
      }
      chooser.setInitialDirectory(initialFile);
    }
    return chooser;
  }

  /**
   * Returns id of the node, null if the parameter node isn't Node instance
   * 
   * @param node
   *          the node, usually event.getSource()
   * @return id of the node
   */
  public static String getNodeId(Object node) {
    return node instanceof Node ? ((Node) node).getId() : null;
  }

  /**
   * show dialog pane as modaless dialog
   * 
   * @param owner
   *          a reference node in the owner window
   * @param pane
   *          the dialog pane
   * @param title
   *          the dialog title
   */
  public static void showModaless(Node owner, DialogPane pane, String title) {
    showDialog(owner, pane, title, true);
  }

  /**
   * show dialog pane as modal dialog
   * 
   * @param owner
   *          a reference node in the owner window
   * @param pane
   *          the dialog pane
   * @param title
   *          the dialog title
   * @return the button type of the dialog closed by
   */
  public static ButtonType showDialog(Node owner, DialogPane pane, String title) {
    return showDialog(owner, pane, title, false);
  }

  private static ButtonType showDialog(Node owner, DialogPane pane, String title,
      boolean modaless) {
    Dialog<ButtonType> dialog = new Dialog<>();
    initOwnerModality(dialog, owner, modaless);
    dialog.setTitle(title);
    dialog.setDialogPane(pane);
    if (modaless)
      dialog.show();
    else
      dialog.showAndWait();
    return dialog.getResult();
  }

  private static void initOwnerModality(Dialog<?> dialog, Node owner, boolean modaless) {
    Scene scene = owner == null ? null : owner.getScene();
    Window window = scene == null ? null : scene.getWindow();
    if (window != null) dialog.initOwner(window);
    if (modaless)
      dialog.initModality(Modality.NONE);
    else if (window != null)
      dialog.initModality(Modality.WINDOW_MODAL);
    else
      dialog.initModality(Modality.APPLICATION_MODAL);
  }

  /**
   * close window which own the reference node, fire WindowEvent.WINDOW_CLOSE_REQUEST for
   * the window
   * 
   * @param reference
   *          any node in the closing window
   */
  public static void closeWindow(Node reference) {
    Window window = reference.getScene().getWindow();
    Event.fireEvent(window, new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
  }

  /**
   * Create an read only observable list for the observable map
   * 
   * @param <K>
   *          key type of the map
   * @param <V>
   *          value type of the map
   * @param map
   *          the observable map
   * @return an read only observable list for the observable map
   */
  public static <K, V> ObservableList<Entry<K, V>> observableMapList(ObservableMap<K, V> map) {
    return new XObservableMapList<>(map);
  }

  /**
   * resize all visible leaf columns width to fit its content, search up to 20,000 rows
   * for each column. no any exception throws even if resize failed
   * 
   * @param table
   *          the table view contains columns to be resized
   */
  public static void optimizeColumnsWidth(TableView<?> table) {
    optimizeColumnsWidth(table, null);
  }

  /**
   * resize all visible leaf columns width to fit its content, search up to 20,000 rows
   * for each column, grow or shrink the specified column width to fit table if possible.
   * no any exception throws even if resize failed
   * 
   * @param table
   *          the table view contains columns to be resized
   * @param fitTable
   *          the specified column to fit table free space
   * @see #resizeColumnToFitTable(TableColumn)
   */
  public static void optimizeColumnsWidth(TableView<?> table, TableColumn<?, ?> fitTable) {
    try {
      Skin<?> skin = table.getSkin();
      Method resize = getResizeColumnMethod(skin);
      if (resize == null) return;
      for (TableColumn<?, ?> column : table.getVisibleLeafColumns())
        resize.invoke(skin, column, 20000);
      if (fitTable != null) resizeColumnToFitTable(fitTable);
    } catch (Throwable ex) {
      // completely ignored any exceptions
    }
  }

  /**
   * resize column to fit its content, search up to 20,000 rows, no any exception throws
   * even resize failed
   * 
   * @param column
   *          the table column
   */
  public static void resizeColumnToFitContent(TableColumn<?, ?> column) {
    try {
      TableView<?> table = column.getTableView();
      if (table == null) return;
      Skin<?> skin = table.getSkin();
      Method resize = getResizeColumnMethod(skin);
      if (resize != null)
        resize.invoke(skin, column, 20000);
    } catch (Exception ex) {
      // completely ignored any exceptions
    }
  }

  private static Method getResizeColumnMethod(Skin<?> skin) {
    if (skin == null) return null;
    Method resize = XObjectUtils.getMethod(skin,
        "resizeColumnToFitContent",
        TableColumn.class, int.class);
    if (resize != null) resize.setAccessible(true);
    return resize;
  }

  /**
   * Grow or shrink the column width to fit table, the column will resize to minimum 10
   * pixels even if minWidth of the column was more less. no any exception throws even
   * resize failed
   * 
   * @param column
   *          the table column
   */
  @SuppressWarnings("deprecation")
  public static void resizeColumnToFitTable(TableColumn<?, ?> column) {
    try {
      TableView<?> table = column.getTableView();
      if (table == null) return;
      Region container = (Region) table.lookup(".clipped-container");
      if (container == null) return;
      double freeWidth = container.getWidth();
      boolean found = false;
      for (TableColumn<?, ?> c : table.getVisibleLeafColumns()) {
        freeWidth -= c.getWidth();
        if (c == column) found = true;
      }
      if (!found || freeWidth == 0) return;
      double width = column.getWidth() + freeWidth;
      width = Math.max(width, Math.min(10, column.getMinWidth()));
      column.impl_setWidth(Math.min(width, column.getMaxWidth()));
    } catch (Throwable ex) {
      // completely ignored any exceptions
    }
  }

  /**
   * Scroll to make the node visible if needed, no operation if the node not in the scroll
   * pane
   * 
   * @param pane
   *          the scroll pane
   * @param node
   *          the node to make visible
   */
  public static void scrollToVisible(ScrollPane pane, Node node) {
    scrollToVertical(pane, node);
    scrollToHorizontal(pane, node);
  }

  /**
   * Scroll vertical to make the node visible if needed, no operation if the node not in
   * the scroll pane
   * 
   * @param pane
   *          the scroll pane
   * @param node
   *          the node to make visible
   */
  public static void scrollToVertical(ScrollPane pane, Node node) {
    new Thread(() -> scrollToNodeV(pane, node)).start();
  }

  /**
   * Scroll horizontal to make the node visible if needed, no operation if the node not in
   * the scroll pane
   * 
   * @param pane
   *          the scroll pane
   * @param node
   *          the node to make visible
   */
  public static void scrollToHorizontal(ScrollPane pane, Node node) {
    new Thread(() -> scrollToNodeH(pane, node)).start();
  }

  private static void scrollToNodeV(ScrollPane pane, Node node) {
    // getBounds returns zero if called too soon after node added
    // and sometimes it throws ArrayIndexOutOfBoundsException
    int tryCounter = 0;
    while (tryCounter++ < 10) {
      try {
        Thread.sleep(100); // wait 100 milliseconds first
        Node content = pane.getContent();
        Bounds bounds = getBoundsInRoot(content, node);
        if (bounds.getMaxY() == 0) return;
        double vVal = pane.getVvalue(), vMax = pane.getVmax();
        double yMin = bounds.getMinY(), yMax = bounds.getMaxY();
        double cHeight = content.getBoundsInLocal().getHeight();
        double vHeight = pane.getViewportBounds().getHeight();
        double vTop = vMax * yMin / (cHeight - vHeight);
        double vBot = vMax * (yMax - vHeight) / (cHeight - vHeight);
        if (vVal < vTop && vVal > vBot) return;
        Platform.runLater(() -> pane.setVvalue(vVal > vBot ? vTop : vBot));
        return; // return after successfully scrolled
      } catch (Exception ex) {
        // ignore any exception to continue try scroll
      }
    }
  }

  private static void scrollToNodeH(ScrollPane pane, Node node) {
    // getBounds returns zero if called too soon after node added
    // and sometimes it throws ArrayIndexOutOfBoundsException
    int tryCounter = 0;
    while (tryCounter++ < 10) {
      try {
        Thread.sleep(100); // wait 100 milliseconds first
        Node content = pane.getContent();
        Bounds bounds = getBoundsInRoot(content, node);
        if (bounds.getMaxY() == 0) continue;
        double hVal = pane.getHvalue(), hMax = pane.getHmax();
        double xMin = bounds.getMinX(), xMax = bounds.getMaxX();
        double cWidth = content.getBoundsInLocal().getWidth();
        double vWidth = pane.getViewportBounds().getWidth();
        double vLft = hMax * xMin / (cWidth - vWidth);
        double vRht = hMax * (xMax - vWidth) / (cWidth - vWidth);
        if (hVal < vLft && hVal > vRht) return;
        Platform.runLater(() -> pane.setHvalue(hVal > vLft ? vRht : vLft));
        return; // return after successfully scrolled
      } catch (Exception ex) {
        // ignore any exception to continue try scroll
      }
    }
  }

  private static Bounds getBoundsInRoot(Node root, Node node) {
    Bounds bounds = node.getBoundsInParent();
    // if returns zero just return to next try
    if (bounds.getMaxY() == 0) return bounds;
    double minX = bounds.getMinX();
    double minY = bounds.getMinY();
    double width = bounds.getWidth();
    double height = bounds.getHeight();
    while (true) {
      node = node.getParent();
      if (node == null || node == root) break;
      bounds = node.getBoundsInParent();
      minX += bounds.getMinX();
      minY += bounds.getMinY();
    }
    return node == null ? new BoundingBox(0, 0, 0, 0)
        : new BoundingBox(minX, minY, width, height);
  }

  /**
   * scroll to table row if the row not visible
   * 
   * @param table
   *          the table view
   * @param row
   *          the row index to make visible
   */
  public static void scrollToRow(TableView<?> table, int row) {
    Region container = (Region) table.lookup(".clipped-container");
    double height = container.getHeight();
    for (Node node : table.lookupAll(".table-row-cell")) {
      TableRow<?> tableRow = (TableRow<?>) node;
      if (tableRow.getIndex() == row) {
        Bounds bounds = tableRow.getBoundsInParent();
        if (bounds.getMinY() >= 0 && bounds.getMaxY() <= height)
          return;
      }
    }
    table.scrollTo(row);
  }

  /**
   * Determine whether the node is focus owner, that means the node or its descendant has
   * focus.
   * 
   * @param node
   *          the node
   * @return true or false
   */
  public static boolean isFocusOwner(Node node) {
    return isFocusOwner(node, false);
  }

  /**
   * Determine whether the node is focus ancestor, that means itself not has focus but its
   * descendant has focus.
   * 
   * @param node
   *          the node
   * @return true or false
   */
  public static boolean isFocusAncestor(Node node) {
    return isFocusOwner(node, true);
  }

  private static boolean isFocusOwner(Node node, boolean descendant) {
    Scene scene = node.getScene();
    if (scene == null) return false;
    Node focused = scene.getFocusOwner();
    if (descendant && focused == node)
      return false;
    while (focused != null) {
      if (focused == node) return true;
      focused = focused.getParent();
    }
    return false;
  }

  private static class ResultProxy<V> {
    private V result;
    private Exception exception;
  }

  /**
   * run a call able task and wait the task to complete
   * 
   * @param <V>
   *          result type of the task
   * @param task
   *          the call able task
   * @return result of the task
   * @throws Exception
   *           if any errors occurred
   */
  public static <V> V runAndWait(Callable<V> task) throws Exception {
    if (Platform.isFxApplicationThread())
      return task.call();
    final CountDownLatch doneLatch = new CountDownLatch(1);
    final ResultProxy<V> proxy = new ResultProxy<>();
    Platform.runLater(() -> {
      try {
        proxy.result = task.call();
      } catch (Exception ex) {
        proxy.exception = ex;
      } finally {
        doneLatch.countDown();
      }
    });
    try {
      doneLatch.await();
    } catch (InterruptedException ex) {
      throw ex;
    }
    if (proxy.exception != null) throw proxy.exception;
    return proxy.result;
  }

  /**
   * run a runnable task and wait the task to complete
   * 
   * @param task
   *          the runnable task
   * @throws InterruptedException
   *           if thread was interrupted
   */
  public static void runAndWait(Runnable task)
      throws InterruptedException {
    if (Platform.isFxApplicationThread()) {
      task.run();
      return;
    }
    final CountDownLatch doneLatch = new CountDownLatch(1);
    Platform.runLater(() -> {
      try {
        task.run();
      } finally {
        doneLatch.countDown();
      }
    });
    try {
      doneLatch.await();
    } catch (InterruptedException ex) {
      throw ex;
    }
  }
}
