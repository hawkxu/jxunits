package win.zqxu.jxunits.jre;

import java.beans.Introspector;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Support read and write multiple objects to XML file
 * 
 * <p>
 * Please note: <b>this class is not thread safe</b>
 * </p>
 * 
 * @author zqxu
 */
public class XStoreXML<T> {

  private File fileStore;
  private Class<T> objectType;
  private String objectTag;
  private Document document;
  private Element rootNode;
  private Marshaller marshaller;
  private Unmarshaller unmarshaller;

  public XStoreXML(String fileName, Class<T> objectType) {
    this(new File(fileName), objectType);
  }

  public XStoreXML(File fileStore, Class<T> objectType) {
    this.fileStore = fileStore;
    this.objectType = objectType;
    XmlRootElement annoXML = objectType.getAnnotation(XmlRootElement.class);
    if (annoXML == null)
      throw new IllegalArgumentException("the object type "
          + objectType + " not annotated by XmlRootElement");
    objectTag = annoXML.name();
    if (objectTag.equals("##default"))
      objectTag = Introspector.decapitalize(objectType.getSimpleName());
    initializeDocument();
  }

  private void initializeDocument() {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      if (fileStore.exists())
        document = builder.parse(fileStore);
      else {
        document = builder.newDocument();
        document.appendChild(document.createElement("root"));
      }
      rootNode = document.getDocumentElement();
      JAXBContext context = JAXBContext.newInstance(objectType);
      marshaller = context.createMarshaller();
      unmarshaller = context.createUnmarshaller();
    } catch (Exception ex) {
      throw new IllegalStateException("initlize failed", ex);
    }
  }

  /**
   * Determine whether is empty (contains no any object node)
   * 
   * @return true if empty
   */
  public boolean isEmpty() {
    return getCount() == 0;
  }

  /**
   * Returns objects count
   * 
   * @return objects count
   */
  public int getCount() {
    return getObjectNodes().getLength();
  }

  /**
   * Determine whether the document contains node with the tag name
   * 
   * @param tagName
   *          the tag name
   * @return true if the document contains node with the tag name
   */
  public boolean contains(String tagName) {
    return getTagNode(tagName) != null;
  }

  /**
   * read object at specified index, null if index unavailable
   * 
   * @param index
   *          the index
   * @return the object
   * @throws Exception
   *           if read error or negative index
   */
  public T read(int index) throws Exception {
    NodeList nodeList = getObjectNodes();
    if (index >= nodeList.getLength()) return null;
    return read(nodeList, index);
  }

  /**
   * read all objects
   * 
   * @return objects or empty list
   * @throws Exception
   *           if read failed
   */
  public List<T> readAll() throws Exception {
    List<T> objects = new ArrayList<>();
    NodeList nodeList = getObjectNodes();
    for (int i = 0; i < nodeList.getLength(); i++)
      objects.add(read(nodeList, i));
    return objects;
  }

  /**
   * Returns index of the object, search using Objects.equals method, so the object class
   * must implemented equals method, otherwise always returns -1.
   * 
   * @param object
   *          the object to search
   * @return index of the object, or -1 if the object not found
   * @throws JAXBException
   *           if search failed
   */
  public int indexOf(T object) throws JAXBException {
    NodeList nodeList = getObjectNodes();
    for (int i = 0; i < nodeList.getLength(); i++)
      if (Objects.equals(object, read(nodeList, i))) return i;
    return -1;
  }

  @SuppressWarnings("unchecked")
  private T read(NodeList nodeList, int index) throws JAXBException {
    return (T) unmarshaller.unmarshal(nodeList.item(index));
  }

  /**
   * replace with object for the node index, create new node if the index equal to current
   * objects count
   * 
   * @param index
   *          the index
   * @param object
   *          the object to write
   * @throws Exception
   *           if write failed or invalid index
   */
  public void write(int index, T object) throws Exception {
    NodeList nodeList = getObjectNodes();
    if (index == nodeList.getLength()) {
      rootNode.appendChild(createObjectNode(object));
    } else {
      Node oldNode = nodeList.item(index);
      rootNode.replaceChild(createObjectNode(object), oldNode);
    }
    saveStore();
  }

  /**
   * if the object exist in document (search by Objects.equals), update document with the
   * object, if the object not exist in document, append it to document.
   * 
   * @param object
   *          the object
   * @throws Exception
   *           if write failed
   */
  public void modify(T object) throws Exception {
    int index = indexOf(object);
    if (index == -1) index = getCount();
    write(index, object);
  }

  /**
   * replace all exist objects with the object list
   * 
   * @param objects
   *          the object list
   * @throws Exception
   *           if write failed
   */
  public void writeAll(List<T> objects) throws Exception {
    NodeList nodeList = getObjectNodes();
    for (int i = 0; i < nodeList.getLength(); i++)
      rootNode.removeChild(nodeList.item(i));
    for (T object : objects)
      rootNode.appendChild(createObjectNode(object));
    saveStore();
  }

  /**
   * Remove object at specified index
   * 
   * @param index
   *          the index
   * @throws Exception
   *           if remove failed or invalid index
   */
  public void remove(int index) throws Exception {
    NodeList nodeList = getObjectNodes();
    rootNode.removeChild(nodeList.item(index));
    saveStore();
  }

  /**
   * Remove the specified object from document (search by Objects.equals), no-operation if
   * the object not exists in document
   * 
   * @param object
   *          the object to remove
   * @throws Exception
   *           if write failed
   */
  public void remove(T object) throws Exception {
    int index = indexOf(object);
    if (index != -1) remove(index);
  }

  /**
   * Remove all the objects from document
   * 
   * @throws Exception
   *           if write failed
   */
  public void removeAll() throws Exception {
    NodeList nodeList = getObjectNodes();
    for (int i = 0; i < nodeList.getLength(); i++)
      rootNode.removeChild(nodeList.item(i));
    saveStore();
  }

  /**
   * Remove all the objects and top level data
   * 
   * @throws Exception
   *           if write failed
   */
  public void clear() throws Exception {
    NodeList nodeList = rootNode.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++)
      rootNode.removeChild(nodeList.item(i));
    saveStore();
  }

  private NodeList getObjectNodes() {
    NodeList nodeList = rootNode.getChildNodes();
    List<Node> tempList = new ArrayList<>();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node child = nodeList.item(i);
      if (child.getNodeName().equals(objectTag))
        tempList.add(child);
    }
    return new ObjectNodeList(tempList);
  }

  private Node createObjectNode(T object) throws Exception {
    Element temp = document.createElement("temp");
    marshaller.marshal(object, temp);
    return temp.getFirstChild();
  }

  /**
   * Read top level string value, returns null if the tag not exists
   * 
   * @param tagName
   *          the tag name for the top level value
   * @return top level string value
   */
  public String readString(String tagName) {
    Node node = getTagNode(tagName);
    return node == null ? null : node.getTextContent();
  }

  /**
   * Write top level string value, the tagName can't be same as the object
   * 
   * @param tagName
   *          the tag name for the top level value
   * @param value
   *          the value to write
   * @throws Exception
   *           if write failed
   */
  public void writeString(String tagName, String value) throws Exception {
    checkAdditionalTagName(tagName);
    Node node = getTagNode(tagName);
    if (value != null) {
      if (node == null) {
        node = document.createElement(tagName);
        rootNode.appendChild(node);
      }
      node.setTextContent(value);
    } else if (node != null)
      rootNode.removeChild(node);
    saveStore();
  }

  /**
   * Read top level boolean value, returns false if the tag not exists
   * 
   * @param tagName
   *          the tag name for the top level value
   * @return top level boolean value
   */
  public boolean readBool(String tagName) {
    String value = readString(tagName);
    return value == null ? false : Boolean.valueOf(value);
  }

  /**
   * Write top level boolean value, the tagName can't be same as the object
   * 
   * @param tagName
   *          the tag name for the top level value
   * @param value
   *          the value to write
   * @throws Exception
   *           if write failed
   */
  public void writeBool(String tagName, boolean value) throws Exception {
    writeString(tagName, String.valueOf(value));
  }

  /**
   * Read top level integer value, returns 0 if the tag not exists
   * 
   * @param tagName
   *          the tag name for the top level value
   * @return top level int value
   */
  public int readInt(String tagName) {
    String value = readString(tagName);
    return value == null ? 0 : Integer.valueOf(value);
  }

  /**
   * Write top level int value, the tagName can't be same as the object
   * 
   * @param tagName
   *          the tag name for the top level value
   * @param value
   *          the value to write
   * @throws Exception
   *           if write failed
   */
  public void writeInt(String tagName, int value) throws Exception {
    writeString(tagName, String.valueOf(value));
  }

  /**
   * Read top level double value, returns 0 if the node not exists
   * 
   * @param tagName
   *          the tag name for the top level value
   * @return top level double value
   */
  public double readDouble(String tagName) {
    String value = readString(tagName);
    return value == null ? 0 : Double.valueOf(value);
  }

  /**
   * Write top level double value, the tagName can't be same as the object
   * 
   * @param tagName
   *          the tag name for the top level value
   * @param value
   *          the value to write
   * @throws Exception
   *           if write failed
   */
  public void writeDobule(String tagName, double value) throws Exception {
    writeString(tagName, String.valueOf(value));
  }

  /**
   * Read top level date value, returns null if the node not exists
   * 
   * @param tagName
   *          the tag name for the top level value
   * @return top level date value
   */
  public Date readDate(String tagName) {
    String value = readString(tagName);
    return value == null ? null : new Date(Long.valueOf(value));
  }

  /**
   * Write top level date value, the tagName can't be same as the object
   * 
   * @param tagName
   *          the tag name for the top level value
   * @param value
   *          the value to write
   * @throws Exception
   *           if write failed
   */
  public void writeDate(String tagName, Date value) throws Exception {
    writeString(tagName, value == null ? null : String.valueOf(value.getTime()));
  }

  private void checkAdditionalTagName(String tagName) {
    if (tagName.equals(objectTag))
      throw new IllegalArgumentException(
          "The top level tag name can not be same as the object: " + tagName);
  }

  private Node getTagNode(String tagName) {
    NodeList nodeList = rootNode.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++)
      if (nodeList.item(i).getNodeName().equals(tagName))
        return nodeList.item(i);
    return null;
  }

  private void saveStore() throws Exception {
    if (!fileStore.getParentFile().exists()) fileStore.getParentFile().mkdirs();
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.transform(new DOMSource(document), new StreamResult(fileStore));
  }

  private static class ObjectNodeList implements NodeList {
    private List<Node> nodeList;

    public ObjectNodeList(List<Node> nodeList) {
      this.nodeList = nodeList;
    }

    @Override
    public Node item(int index) {
      return nodeList.get(index);
    }

    @Override
    public int getLength() {
      return nodeList.size();
    }
  }
}
