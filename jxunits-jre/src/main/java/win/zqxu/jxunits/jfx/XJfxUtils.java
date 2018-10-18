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

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    initOwnerModality(alert, owner);
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
    Dialog<ButtonType> dialog = createDialog(owner, content);
    dialog.setTitle(XResource.getString("INPUT_TITLE"));
    dialog.setHeaderText(header);
    try {
      formatter.setValue(defaultValue);
      field.setTextFormatter(formatter);
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
   * @return created dialog
   * @see #createDialog(Node, Node, ButtonType...)
   */
  public static Dialog<ButtonType> createDialog(Node owner, Node content) {
    return createDialog(owner, content, ButtonType.OK, ButtonType.CANCEL);
  }

  /**
   * Create a dialog with specified content and buttons, this function will initialize
   * dialog owner and dialog modality type, and set icon for buttons
   * 
   * @param owner
   *          a reference node in the owner window
   * @param content
   *          content node for the created dialog
   * @param buttons
   *          buttons will be created in dialog
   * @return created dialog
   * @see #setButtonIcon(ButtonType, Image)
   */
  public static Dialog<ButtonType> createDialog(Node owner, Node content,
      ButtonType... buttons) {
    Dialog<ButtonType> dialog = new Dialog<>();
    initOwnerModality(dialog, owner);
    DialogPane dialogPane = new DialogPane();
    dialog.setDialogPane(dialogPane);
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
      Platform.runLater(() -> showException(null, null, real));
    });
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
    initOwnerModality(alert, owner);
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
      if (initialFile.isFile()) {
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
    Dialog<ButtonType> dialog = new Dialog<>();
    initOwnerModality(dialog, owner);
    dialog.setTitle(title);
    dialog.setDialogPane(pane);
    dialog.showAndWait();
    return dialog.getResult();
  }

  private static void initOwnerModality(Dialog<?> dialog, Node owner) {
    Scene scene = owner == null ? null : owner.getScene();
    Window window = scene == null ? null : scene.getWindow();
    if (window != null) {
      dialog.initOwner(window);
      dialog.initModality(Modality.WINDOW_MODAL);
    } else {
      dialog.initModality(Modality.APPLICATION_MODAL);
    }
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
   * resize column to fit its content if possible
   * 
   * @param column
   *          the table column
   */
  public static void resizeColumnToFitContent(TableColumn<?, ?> column) {
    Skin<?> skin = column.getTableView().getSkin();
    if (skin == null) return;
    try {
      Method method = XObjectUtils.getMethod(skin, "resizeColumnToFitContent",
          TableColumn.class, int.class);
      method.setAccessible(true);
      method.invoke(skin, column, -1);
    } catch (Exception ex) {
      // safely ignored any exception
    }
  }

  /**
   * Grow column width to fit table if possible
   * 
   * @param column
   *          the table column
   */
  public static void resizeColumnToFitTable(TableColumn<?, ?> column) {
    TableView<?> table = column.getTableView();
    Node container = table.lookup(".clipped-container");
    if (container == null) return;
    double freeWidth = container.prefWidth(-1);
    for (TableColumn<?, ?> item : table.getColumns())
      if (item.isVisible()) freeWidth -= item.getWidth();
    if (freeWidth == 0) return;
    try {
      Method method = XObjectUtils.getMethod(column, "setWidth", double.class);
      method.setAccessible(true);
      method.invoke(column, column.getWidth() + freeWidth);
    } catch (Exception ex) {
      // completely ignored any exceptions
    }
  }
}
