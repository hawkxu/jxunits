package win.zqxu.jxunits.jfx;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.image.Image;

class XImageLoader {
  private static Map<String, Image> cache = new HashMap<>();

  /**
   * Get image for the name, Get image from cache if the image already loaded or load the
   * image and cache it for later use
   * 
   * @param name
   *          the image resource name
   * @return image for the name
   * @see Class#getResource(String)
   */
  public static Image get(String name) {
    if (!cache.containsKey(name)) {
      cache.put(name, new Image(XImageLoader.class.getResource(name).toExternalForm()));
    }
    return cache.get(name);
  }
}
