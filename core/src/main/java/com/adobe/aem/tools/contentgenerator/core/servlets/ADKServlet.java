package com.adobe.aem.tools.contentgenerator.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

@Component(service = { Servlet.class })
@SlingServletPaths("/bin/adk")
public class ADKServlet extends SlingAllMethodsServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        resp.getWriter().write("Hello from SimpleServlet!");
    }

    @Override
    protected void doPost(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.getWriter().write("{\n" +
                "  \"jcr:title\": \"Go to Google\",\n" +
                "  \"id\": \"gotoBtn\",\n" +
                "  \"linkURL\": \"https://www.google.com\",\n" +
                "  \"linkTarget\": \"_blank\",\n" +
                "  \"accessibilityLabel\": \"Go to Google\",\n" +
                "  \"sling:resourceType\": \"contentgenerator/components/button\"\n" +
                "}");
    }
}
