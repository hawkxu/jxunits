package win.zqxu.jxunits.test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

public class XBaseTest {
  /**
   * output indent
   * 
   * @param indent
   *          the numbers of indent
   * @param stream
   *          the stream to output
   */
  protected static void outputIndent(int indent, OutputStream stream) {
    try {
      if (indent <= 0) return;
      String format = String.format("%%%ds", indent);
      stream.write(String.format(format, "").getBytes());
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}
