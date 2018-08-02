package win.zqxu.jxunits.web.jnlp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import win.zqxu.jxunits.jre.XObjectUtils;

/**
 * Utility class
 * 
 * @author zqxu
 */
public class XJNLPDeployUtils {
  private static ObjectMapper JSONMapper;

  public static ObjectMapper getJSONMapper() {
    if (JSONMapper == null) {
      JSONMapper = new ObjectMapper().registerModule(new JavaTimeModule())
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    return JSONMapper;
  }

  /**
   * Read file content as String using UTF-8 charset
   * 
   * @param file
   *          the file to read
   * @return file content
   * @throws IOException
   *           if IO exception occurs
   */
  public static String readFileString(File file) throws IOException {
    return new String(readFileBinary(file), StandardCharsets.UTF_8);
  }

  /**
   * Read file content as byte array
   * 
   * @param file
   *          the file to read
   * @return file content
   * @throws IOException
   *           if IO exception occurs
   */
  public static byte[] readFileBinary(File file) throws IOException {
    try (FileInputStream fis = new FileInputStream(file)) {
      return XJNLPDeployUtils.readInputStream(fis);
    }
  }

  /**
   * Read input stream content as string using UTF-8 charset
   * 
   * @param stream
   *          the input stream
   * @return input stream content
   * @throws IOException
   *           if IO exception occurs
   */
  public static String readStreamString(InputStream stream) throws IOException {
    byte[] buffer = readInputStream(stream);
    if (XObjectUtils.isEmpty(buffer)) return null;
    return new String(buffer, StandardCharsets.UTF_8);
  }

  /**
   * read content from input stream
   * 
   * @param stream
   *          the input stream
   * @return content
   * @throws IOException
   *           if IO exception occurs
   */
  public static byte[] readInputStream(InputStream stream) throws IOException {
    ByteArrayOutputStream content = new ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    int size = stream.read(buffer);
    while (size != -1) {
      content.write(buffer, 0, size);
      size = stream.read(buffer);
    }
    return content.toByteArray();
  }

  /**
   * Get mime type from file content
   * 
   * @param content
   *          file content (base64 formatted)
   * @return mime type or null if content not well formatted
   */
  public static String getMimeTypeFromContent(String content) {
    if (XObjectUtils.isEmpty(content)) return null;
    Pattern p = Pattern.compile("data:([^;]+);");
    Matcher m = p.matcher(content);
    return m.find() ? m.group(1) : null;
  }

  /**
   * decode base64 formatted content
   * 
   * @param content
   *          the content
   * @return decoded content
   */
  public static byte[] decodeBase64(String content) {
    return Base64.getDecoder().decode(content.replaceFirst(".*?;base64,", ""));
  }

  /**
   * Write content to file (overwrite) using UTF-8 charset
   * 
   * @param file
   *          the file to write
   * @param content
   *          the content to write
   * @throws IOException
   *           if IO exception occurs
   */
  public static void writeFileContent(File file, String content) throws IOException {
    writeFileContent(file, content.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Write content to file (overwrite)
   * 
   * @param file
   *          the file to write
   * @param content
   *          the content to write
   * @throws IOException
   *           if IO exception occurs
   */
  public static void writeFileContent(File file, byte[] content) throws IOException {
    File folder = file.getParentFile();
    if (!folder.exists()) folder.mkdirs();
    try (FileOutputStream fos = new FileOutputStream(file)) {
      fos.write(content);
    }
  }

  /**
   * Move source file to target by <code>File.renameTo</code> method, first make target
   * folder if it does not exist, if the target file was existed, delete it before moving
   * 
   * @param source
   *          the source file
   * @param target
   *          the target file
   * @return true if and only if the moving succeeded; false otherwise
   */
  public static boolean moveFile(File source, File target) {
    if (target.exists()) {
      if (!target.delete()) return false;
    }
    File folder = target.getParentFile();
    if (!folder.exists()) folder.mkdirs();
    return source.renameTo(target);
  }

  /**
   * Remove all files and sub-directory under the directory
   * 
   * @param directory
   *          the directory to clear
   * @param remove
   *          whether remove the directory after clear
   * @throws IOException
   *           if IO exception occurs
   */
  public static void clearDirectory(File directory, boolean remove) throws IOException {
    File[] files = directory.listFiles();
    if (files == null) return;
    for (File file : files) {
      if (file.isDirectory())
        clearDirectory(file, true);
      else if (!file.delete())
        throw new IOException("Can not delete " + file);
    }
    if (remove) directory.delete();
  }

  /**
   * Create Document instance from XML content
   * 
   * @param content
   *          the XML content
   * @return Document instance
   * @throws IOException
   *           If any IO errors occur
   * @throws SAXException
   *           If any parse errors occur
   * @throws ParserConfigurationException
   *           if Document instance can not be created
   */
  public static Document parseXMLDocument(String content) throws IOException,
      SAXException, ParserConfigurationException {
    return DocumentBuilderFactory.newInstance().newDocumentBuilder()
        .parse(new ByteArrayInputStream(content.getBytes()));
  }

  /**
   * Get content from XML Document instance
   * 
   * @param document
   *          the XML Document instance
   * @return XML content
   * @throws TransformerException
   *           if transformation failed
   */
  public static String getXMLContent(Document document) throws TransformerException {
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    StringWriter writer = new StringWriter();
    transformer.transform(new DOMSource(document), new StreamResult(writer));
    return writer.toString();
  }

  /**
   * Calculate hash code for content, use SHA-256 algorithm and base64 encoded
   * 
   * @param content
   *          the content
   * @return base64 encoded hash code
   * @throws NoSuchAlgorithmException
   *           if no Provider supports SHA-256
   */
  public static String calcHashSHA256(byte[] content) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    return Base64.getEncoder().encodeToString(digest.digest(content));
  }

  /**
   * Get base URL from request, means the URL include context path and servlet path only
   * 
   * @param req
   *          the request
   * @return base URL end with a "/"
   */
  public static String getBaseURL(HttpServletRequest req) {
    String root = req.getRequestURL().toString().replaceFirst("^(.*?//[^/]*).*", "$1");
    return root + req.getContextPath() + req.getServletPath() + "/";
  }
}
