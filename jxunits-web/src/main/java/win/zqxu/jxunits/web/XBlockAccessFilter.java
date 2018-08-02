package win.zqxu.jxunits.web;

import java.io.IOException;
import java.net.URI;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter to block access to some pages, used in web.xml
 * 
 * @author zqxu
 */
public class XBlockAccessFilter implements Filter {
  private int blockedCode;
  private String blockedPage;

  /**
   * Get HTTP response code when access blocked, default is 404
   * 
   * @return HTTP response code when access blocked
   */
  public int getBlockedCode() {
    return blockedCode;
  }

  /**
   * Set HTTP response code when access blocked
   * 
   * @param blockedCode
   *          HTTP response code when access blocked
   */
  public void setBlockedCode(int blockedCode) {
    this.blockedCode = blockedCode;
  }

  /**
   * Get redirect to page when access blocked, default is null
   * 
   * @return redirect to page when access blocked
   */
  public String getBlockedPage() {
    return blockedPage;
  }

  /**
   * Set redirect to page when access blocked, the blockedCode will be ignored if this
   * property has been set.
   * 
   * @param blockedPage
   *          redirect to page when access blocked
   */
  public void setBlockedPage(String blockedPage) {
    this.blockedPage = blockedPage;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    String code = filterConfig.getInitParameter("blockedCode");
    try {
      blockedCode = Integer.valueOf(code);
    } catch (RuntimeException ex) {
      blockedCode = HttpServletResponse.SC_NOT_FOUND;
    }
    blockedPage = filterConfig.getInitParameter("blockedPage");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (blockedPage == null || blockedPage.isEmpty())
      ((HttpServletResponse) response).sendError(blockedCode);
    else if (isBlockedPage((HttpServletRequest) request))
      chain.doFilter(request, response);
    else
      ((HttpServletResponse) response).sendRedirect(blockedPage);
  }

  private boolean isBlockedPage(HttpServletRequest request) {
    URI uri = URI.create(request.getRequestURI());
    return uri.equals(uri.resolve(blockedPage));
  }

  @Override
  public void destroy() {
  }
}
