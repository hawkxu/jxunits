package win.zqxu.jxunits.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import win.zqxu.jxunits.jre.XObjectUtils;
import win.zqxu.jxunits.web.jnlp.XJNLPAbstractStore;
import win.zqxu.jxunits.web.jnlp.XJNLPDefaultStore;
import win.zqxu.jxunits.web.jnlp.XJNLPDeployUtils;
import win.zqxu.jxunits.web.jnlp.XJNLPLibrary;
import win.zqxu.jxunits.web.jnlp.XJNLPLibraryDeploy;
import win.zqxu.jxunits.web.jnlp.XJNLPProject;
import win.zqxu.jxunits.web.jnlp.XJNLPProjectDeploy;

/**
 * JNLP deployment servlet
 * 
 * <p>
 * Can set custom store class by initial parameter <code>store-class-name</code>, the
 * store class must be extended from
 * <code>win.zqxu.jxunits.web.jnlp.XJNLPAbstractStore</code>, default use
 * <code>win.zqxu.jxunits.web.jnlp.XJNLPDefaultStore</code>
 * </p>
 * <p>
 * Access the JNLP deploy servlet through URL
 * http://host.domain/context_path/servlet_path/URI, available URIs described below,
 * replace the <b>project</b> with actual project name
 * </p>
 * <p>
 * The parameter followed (?) means optional
 * </p>
 * <table summary="Servlet URIs">
 * <tr>
 * <td>URI</td>
 * <td>Method</td>
 * <td>Parameter</td>
 * <td>Response</td>
 * <td>Description</td>
 * </tr>
 * <tr>
 * <td>/</td>
 * <td>GET</td>
 * <td></td>
 * <td>HTML</td>
 * <td>Home page</td>
 * </tr>
 * <tr>
 * <td>/_list</td>
 * <td>GET</td>
 * <td></td>
 * <td>JSON: project list</td>
 * <td>Get deployed projects</td>
 * </tr>
 * <tr>
 * <td>/deploy/<b>project</b>/</td>
 * <td>GET</td>
 * <td></td>
 * <td>HTML</td>
 * <td>Deployment page for the project</td>
 * </tr>
 * <tr>
 * <td>/deploy/<b>project</b>/_start</td>
 * <td rowspan="3">POST</td>
 * <td>header: forceDeploy (?)</td>
 * <td>deploy task number(?)</td>
 * <td>Start project deploy</td>
 * </tr>
 * <tr>
 * <td>/deploy/<b>project</b>/_finish</td>
 * <td>header: deployTask</td>
 * <td>null</td>
 * <td>Finish project deploy</td>
 * </tr>
 * <tr>
 * <td>/deploy/<b>project</b>/_cancel</td>
 * <td>header: deployTask</td>
 * <td>null</td>
 * <td>Cancel project deploy</td>
 * </tr>
 * <tr>
 * <td rowspan="2">/deploy/<b>project</b>/_project</td>
 * <td>GET</td>
 * <td>header: deployTask (?)</td>
 * <td>JSON: project</td>
 * <td>Get project</td>
 * </tr>
 * <tr>
 * <td>POST</td>
 * <td>header: deployTask<br>
 * content: JSON<br>
 * &nbsp;&nbsp;{project, jnlpContent}</td>
 * <td>JSON: project</td>
 * <td>Update project</td>
 * </tr>
 * <tr>
 * <td rowspan="2">/deploy/<b>project</b>/_icons</td>
 * <td>GET</td>
 * <td>header: deployTask (?)</td>
 * <td>JSON: icons</td>
 * <td>Get project icons</td>
 * </tr>
 * <tr>
 * <td>POST</td>
 * <td>header: deployTask<br>
 * content: JSON<br>
 * &nbsp;&nbsp;{kind, iconContent}</td>
 * <td>null</td>
 * <td>Update project icon</td>
 * </tr>
 * <tr>
 * <td>/deploy/<b>project</b>/_libs</td>
 * <td>GET</td>
 * <td>header: deployTask</td>
 * <td>JSON</td>
 * <td>Get libraries of the project</td>
 * </tr>
 * <tr>
 * <td>/deploy/<b>project</b>/_lib</td>
 * <td>POST</td>
 * <td>header: deployTask<br>
 * content: JSON<br>
 * &nbsp;&nbsp;{library, jarContent}</td>
 * <td>JSON: library</td>
 * <td>Update library</td>
 * </tr>
 * <tr>
 * <td>/<b>project</b>/</td>
 * <td rowspan="2">GET</td>
 * <td rowspan="2"></td>
 * <td rowspan="2">project jnlp file</td>
 * <td rowspan="2">The two URIs has same effect<br>
 * Note: the jnlp file name does not include version number</td>
 * </tr>
 * <tr>
 * <td>/<b>project</b>/<b>project</b>.jnlp</td>
 * </tr>
 * <tr>
 * <td>/<b>project</b>/_modules</td>
 * <td>GET</td>
 * <td></td>
 * <td>JSON: modules</td>
 * <td>Get modules in the project</td>
 * </tr>
 * <tr>
 * <td>/<b>project</b>/path_to/jar_file</td>
 * <td>GET</td>
 * <td></td>
 * <td>jar file</td>
 * <td>Get jar file of the project</td>
 * </tr>
 * </table>
 * <br>
 * 
 * @author zqxu
 */
public class XJNLPDeployServlet extends HttpServlet {
  /**
   * Initial parameter for store class name
   */
  public static final String STORE_CLASS_NAME = "store-class-name";
  private static final long serialVersionUID = 1L;
  private static final Charset CHARSET = StandardCharsets.UTF_8;
  private static final ObjectMapper JSONMapper = XJNLPDeployUtils.getJSONMapper();
  private Class<? extends XJNLPAbstractStore> storeClass;

  @Override
  public void init() throws ServletException {
    initializeStore();
  }

  @SuppressWarnings("unchecked")
  private void initializeStore() throws ServletException {
    String storeClassName = getInitParameter(STORE_CLASS_NAME);
    if (XObjectUtils.isEmpty(storeClassName))
      storeClassName = XJNLPDefaultStore.class.getName();
    try {
      storeClass = (Class<? extends XJNLPAbstractStore>) Class.forName(storeClassName);
      createStore(null).initialize();
    } catch (Exception ex) {
      throw new ServletException("Can not initialize store", ex);
    }
  }

  private XJNLPAbstractStore createStore(HttpServletRequest req) throws IOException {
    try {
      String task = req == null ? null : req.getHeader("deployTask");
      return storeClass.getConstructor(String.class).newInstance(task);
    } catch (ReflectiveOperationException ex) {
      throw new IOException("Can not create store for request", ex);
    }
  }

  private RequestPath parseRequestPath(HttpServletRequest req) {
    RequestPath path = new RequestPath();
    String pathInfo = req.getPathInfo();
    if (!XObjectUtils.isEmpty(pathInfo)) {
      List<String> pathList = new ArrayList<>();
      Pattern p = Pattern.compile("/([^/]*)");
      Matcher m = p.matcher(pathInfo);
      while (m.find()) {
        pathList.add(m.group(1));
      }
      if (pathList.get(0).equals("deploy")) {
        pathList.remove(0);
        path.isDeploy = true;
      }
      if (pathList.size() > 1) path.projectName = pathList.remove(0);
      if (!pathList.isEmpty()) path.resource = pathList.get(pathList.size() - 1);
    }
    return path;
  }

  private String getHomPath(HttpServletRequest req) {
    return req.getContextPath() + req.getServletPath() + "/";
  }

  @Override
  protected long getLastModified(HttpServletRequest req) {
    RequestPath path = parseRequestPath(req);
    if (!path.isNull()) {
      if (path.isDeploy) return getDeployLastModified(req, path);
      if (path.projectName != null) return getRuntimeLastModified(req, path);
      if (path.resource.isEmpty()) return getResourceLastModified("index.html");
      return getResourceLastModified(path.resource);
    }
    return -1;
  }

  private long getDeployLastModified(HttpServletRequest req, RequestPath path) {
    if (path.resource != null) {
      if (path.resource.isEmpty())
        return getResourceLastModified("deploy.html");
      if (!path.resource.startsWith("_"))
        return getResourceLastModified(path.resource);
    }
    return -1;
  }

  private long getRuntimeLastModified(HttpServletRequest req, RequestPath path) {
    try {
      if (!path.resource.isEmpty()) {
        XJNLPAbstractStore store = createStore(req);
        XJNLPProject project = store.getRuntimeProject(path.projectName);
        if (project == null || !store.isAccessAllowed(req, project.getName()))
          return -1;
        if (path.resource.equals(path.projectName + ".jnlp"))
          return project.getLastModified().atZone(ZoneId.systemDefault())
              .toInstant().toEpochMilli();
        if (path.resource.matches(".*\\.jar")) {
          XJNLPLibrary library = store.getRuntimeLibrary(path.projectName, path.resource);
          if (library != null) return library.getLastModified()
              .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
      }
    } catch (Exception ex) {
      // safely ignore any exceptions
    }
    return -1;
  }

  private long getResourceLastModified(String resource) {
    URL got = XJNLPDeployServlet.class.getResource(resource);
    try {
      if (got != null) return got.openConnection().getLastModified();
    } catch (Exception ex) {
      // safely ignore any exceptions
    }
    return -1;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setCharacterEncoding(CHARSET.name());
    RequestPath path = parseRequestPath(req);
    if (path.isNull())
      resp.sendRedirect(getHomPath(req));
    else if (path.isDeploy)
      processGetDeploy(req, resp, path);
    else if (path.projectName != null)
      processGetRuntime(req, resp, path);
    else if (path.resource.isEmpty())
      writeResource(resp, "index.html");
    else if (path.resource.equals("_list"))
      writeProjects(req, resp);
    else
      writeResource(resp, path.resource);
  }

  private void processGetDeploy(HttpServletRequest req, HttpServletResponse resp,
      RequestPath path) throws IOException {
    if (XObjectUtils.isEmpty(path.projectName)) {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }
    if (path.resource == null) {
      resp.sendRedirect(getHomPath(req) + "deploy/" + path.projectName);
      return;
    }
    XJNLPAbstractStore store = createStore(req);
    if (!store.isDeployAllowed(req, path.projectName)) {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }
    if (path.resource.isEmpty()) {
      writeResource(resp, "deploy.html");
    } else if (path.resource.equals("_project")) {
      writeDeployingProject(store, path.projectName, resp);
    } else if (path.resource.equals("_icons")) {
      writeDeployingIcons(store, path.projectName, resp);
    } else if (path.resource.equals("_libs")) {
      writeDeployingLibraries(store, path.projectName, resp);
    } else {
      writeResource(resp, path.resource);
    }
  }

  private void writeProjects(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    List<XJNLPProjectDeploy> deployList = new ArrayList<>();
    XJNLPAbstractStore store = createStore(req);
    for (XJNLPProject project : store.getProjects()) {
      if (store.isAccessAllowed(req, project.getName())) {
        XJNLPProjectDeploy deploy = new XJNLPProjectDeploy(project);
        deploy.setDeployAllowed(store.isDeployAllowed(req, project.getName()));
        deployList.add(deploy);
      }
    }
    if (store.isDeployAllowed(req, null)) {
      XJNLPProjectDeploy deploy = new XJNLPProjectDeploy();
      deploy.setName("$add");
      deploy.setDeployAllowed(true);
      deployList.add(deploy);
    }
    writeJSONData(resp, deployList);
  }

  private void writeDeployingProject(XJNLPAbstractStore store, String projectName,
      HttpServletResponse resp) throws IOException {
    writeJSONData(resp, store.getDeployingProject(projectName));
  }

  private void writeDeployingIcons(XJNLPAbstractStore store, String projectName,
      HttpServletResponse resp) throws IOException {
    writeJSONData(resp, store.getDeployingIcons(projectName));
  }

  private void writeDeployingLibraries(XJNLPAbstractStore store, String projectName,
      HttpServletResponse resp) throws IOException {
    writeJSONData(resp, store.getDeployingLibraries(projectName));
  }

  private void processGetRuntime(HttpServletRequest req, HttpServletResponse resp,
      RequestPath path) throws IOException {
    String jnlpName = path.projectName + ".jnlp";
    if (path.resource.isEmpty()) {
      resp.sendRedirect(getHomPath(req) + path.projectName + "/" + jnlpName);
      return;
    }
    XJNLPAbstractStore store = createStore(req);
    XJNLPProject project = store.getRuntimeProject(path.projectName);
    if (project == null) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    } else if (!store.isAccessAllowed(req, path.projectName)) {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN);
    } else if (path.resource.equals(jnlpName)) {
      resp.setContentType("application/x-java-jnlp-file");
      String baseURL = XJNLPDeployUtils.getBaseURL(req);
      resp.getWriter().write(store.readRuntimeJNLP(baseURL, path.projectName));
    } else if (path.resource.equals("_modules")) {
      writeJSONData(resp, store.readRuntimeModules(path.projectName));
    } else if (path.resource.matches(".*\\.jar")) {
      resp.setContentType("application/java-archive");
      byte[] jarContent = store.readRuntimeJar(path.projectName, path.resource);
      if (jarContent != null)
        resp.getOutputStream().write(jarContent);
      else
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    } else if (path.resource.matches(".*\\.(gif|jpg|ico|png)")) {
      String iconContent = store.readRuntimeIcon(path.projectName, path.resource);
      String mimeType = XJNLPDeployUtils.getMimeTypeFromContent(iconContent);
      if (mimeType != null) {
        resp.setContentType(mimeType);
        resp.getOutputStream().write(XJNLPDeployUtils.decodeBase64(iconContent));
      } else {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
    } else {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  private void writeResource(HttpServletResponse resp, String name) throws IOException {
    URL resource = XJNLPDeployServlet.class.getResource(name);
    if (resource != null)
      resp.getOutputStream().write(loadResource(name));
    else
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
  }

  private byte[] loadResource(String name) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    byte[] temp = new byte[4096];
    try (InputStream reader = XJNLPDeployServlet.class.getResourceAsStream(name)) {
      int size;
      do {
        size = reader.read(temp);
        if (size > 0) buffer.write(temp, 0, size);
      } while (size != -1);
    }
    return buffer.toByteArray();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setCharacterEncoding(CHARSET.name());
    RequestPath path = parseRequestPath(req);
    if (!path.isNull() && path.isDeploy
        && !XObjectUtils.isEmpty(path.projectName))
      processPostDeploy(req, resp, path);
    else
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
  }

  private void processPostDeploy(HttpServletRequest req,
      HttpServletResponse resp, RequestPath path) throws IOException {
    XJNLPAbstractStore store = createStore(req);
    if (!store.isDeployAllowed(req, path.projectName)) {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN);
    } else if (path.resource.equals("_start")) {
      requestStartDeploying(store, path.projectName, req, resp);
    } else if (path.resource.equals("_finish")) {
      requestFinishDeploying(store, path.projectName, resp);
    } else if (path.resource.equals("_cancel")) {
      requestCancelDeploying(store, path.projectName, resp);
    } else if (path.resource.equals("_project")) {
      requestUpdateProject(store, path.projectName, req, resp);
    } else if (path.resource.equals("_lib")) {
      requestUpdateLibrary(store, path.projectName, req, resp);
    } else if (path.resource.equals("_icons")) {
      requestUpdateIcon(store, path.projectName, req, resp);
    } else {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  private void requestStartDeploying(XJNLPAbstractStore store, String projectName,
      HttpServletRequest req, HttpServletResponse resp) throws IOException {
    Boolean force = Boolean.valueOf(req.getHeader("forceDeploy"));
    writeJSONData(resp, store.startDeployingTask(projectName, force));
  }

  private void requestFinishDeploying(XJNLPAbstractStore store, String projectName,
      HttpServletResponse resp) throws IOException {
    resp.getWriter().write("null");
    store.finishDeployingTask(projectName);
  }

  private void requestCancelDeploying(XJNLPAbstractStore store, String projectName,
      HttpServletResponse resp) throws IOException {
    resp.getWriter().write("null");
    store.cancelDeployingTask(projectName);
  }

  private void requestUpdateProject(XJNLPAbstractStore store, String projectName,
      HttpServletRequest req, HttpServletResponse resp) throws IOException {
    JsonNode content = JSONMapper.readTree(req.getInputStream());
    XJNLPProjectDeploy project = JSONMapper.treeToValue(
        content.get("project"), XJNLPProjectDeploy.class);
    project.setName(projectName);
    JsonNode jnlpNode = content.get("jnlpContent");
    String jnlpContent = jnlpNode == null
        ? null
        : jnlpNode.textValue();
    project = store.updateDeployingProject(project, jnlpContent);
    writeJSONData(resp, project);
  }

  private void requestUpdateLibrary(XJNLPAbstractStore store, String projectName,
      HttpServletRequest req, HttpServletResponse resp) throws IOException {
    JsonNode content = JSONMapper.readTree(req.getInputStream());
    XJNLPLibraryDeploy library = JSONMapper.treeToValue(
        content.get("library"), XJNLPLibraryDeploy.class);
    JsonNode jarNode = content.get("jarContent");
    byte[] jarContent = null;
    if (jarNode != null) {
      String jarBase64 = content.get("jarContent").textValue();
      jarContent = XJNLPDeployUtils.decodeBase64(jarBase64);
    }
    library = store.updateDeployingLibrary(projectName, library, jarContent);
    writeJSONData(resp, library);
  }

  private void requestUpdateIcon(XJNLPAbstractStore store, String projectName,
      HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.getWriter().write("null");
    JsonNode content = JSONMapper.readTree(req.getInputStream());
    String kind = content.get("kind").textValue();
    String iconContent = content.get("iconContent").textValue();
    store.updateDeployingIcon(projectName, kind, iconContent);
  }

  private void writeJSONData(HttpServletResponse resp, Object value) throws IOException {
    resp.setContentType("application/json");
    resp.getWriter().write(JSONMapper.writeValueAsString(value));
  }

  private static final class RequestPath {
    private boolean isDeploy;
    private String projectName;
    private String resource;

    public boolean isNull() {
      return !isDeploy && projectName == null && resource == null;
    }
  }
}
