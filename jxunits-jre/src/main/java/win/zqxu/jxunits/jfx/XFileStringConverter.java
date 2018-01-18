package win.zqxu.jxunits.jfx;

import java.io.File;

import javafx.util.StringConverter;
import win.zqxu.jxunits.jre.XObjectUtils;

public class XFileStringConverter extends StringConverter<File> {

  @Override
  public String toString(File file) {
    return file == null ? "" : file.getAbsolutePath();
  }

  @Override
  public File fromString(String string) {
    return XObjectUtils.isEmpty(string) ? null : new File(string);
  }
}
