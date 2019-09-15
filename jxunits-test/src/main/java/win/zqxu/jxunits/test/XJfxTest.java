package win.zqxu.jxunits.test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import javafx.scene.Node;
import javafx.scene.Parent;

/**
 * test utilities for JavaFX
 * 
 * @author zqxu
 */
public class XJfxTest {
  /**
   * output the root node and its descendants to standard output
   * 
   * @param root
   *          the root node
   */
  public static void outputJfxTree(Node root) {
    outputJfxTree(root, System.out);
  }

  /**
   * output the root node and its descendants to specified output stream
   * 
   * @param root
   *          the root node
   * @param stream
   *          the output stream
   */
  public static void outputJfxTree(Node root, OutputStream stream) {
    outputJfxTree(stream, 0, root);
  }

  private static void outputJfxTree(OutputStream stream, int indent, Node node) {
    try {
      if (indent > 0) {
        String pattern = "%" + indent + "s";
        stream.write(String.format(pattern, "").getBytes());
      }
      stream.write(node.toString().getBytes());
      stream.write(System.lineSeparator().getBytes());
      if (node instanceof Parent)
        for (Node child : ((Parent) node).getChildrenUnmodifiable())
        outputJfxTree(stream, indent + 4, child);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}
