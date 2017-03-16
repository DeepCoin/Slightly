package org.slightly.servlets.filters;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.slightly.SlightlyTemplateTransformer;
import org.slightly.servlets.wrappers.HtmlResponseWrapper;

public class SlightlyServletFilter implements javax.servlet.Filter {
	private FilterConfig filterConfig;

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws java.io.IOException, javax.servlet.ServletException {
		PrintWriter out = response.getWriter();
		HtmlResponseWrapper capturingResponseWrapper = new HtmlResponseWrapper((HttpServletResponse) response);
		chain.doFilter(request, capturingResponseWrapper);

		String htmlContent = capturingResponseWrapper.getCaptureAsString();
	      
		try {
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("request", request);
			
			SlightlyTemplateTransformer slightlyTemplateTransformer = new SlightlyTemplateTransformer("nashorn", htmlContent, parameters);
			out.write(slightlyTemplateTransformer.getProcessedContent());
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void init(final FilterConfig filterConfig) {
		this.filterConfig = filterConfig;
	}

	public void destroy() {
		filterConfig = null;
	}
}
