package com.adobe.aem.tools.contentgenerator.core.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component(service = Servlet.class, property = {
        "sling.servlet.paths=/bin/core-comp-dialog",
        "sling.servlet.methods=GET"
})
public class CoreComponentDialogServlet extends SlingAllMethodsServlet {

    private static final String CORE_BASE_PATH = "/libs/core/wcm/components/";

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String relativePath = request.getParameter("componentPath");

        if (relativePath == null || relativePath.isEmpty()) {
            response.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Missing componentPath parameter\"}");
            return;
        }

        ResourceResolver resolver = request.getResourceResolver();
        Resource dialogRoot = resolver.getResource(CORE_BASE_PATH + relativePath + "/cq:dialog/content/items");

        if (dialogRoot == null) {
            response.setStatus(SlingHttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\": \"Dialog not found for component: " + relativePath + "\"}");
            return;
        }

        Map<String, Object> props = new HashMap<>();
        collectDialogFields(dialogRoot, props);

        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getWriter(), props);
    }

  private void collectDialogFields(Resource resource, Map<String, Object> props) {
    for (Resource child : resource.getChildren()) {
        ValueMap vm = child.getValueMap();

        // Field name can be 'name' or 'fieldName'
        String fieldName = vm.get("name", vm.get("fieldName", null));
        Object defaultValue = vm.get("value", vm.get("defaultValue", null));

        if (fieldName != null) {
            // Normalize field name: remove './' prefix
            if (fieldName.startsWith("./")) {
                fieldName = fieldName.substring(2);
            }

            // Only store if there is a default or empty value
            if (defaultValue == null) {
                defaultValue = "";
            }
            props.put(fieldName, defaultValue);
        }

        // Recurse into all children, not just 'items'
        if (child.hasChildren()) {
            collectDialogFields(child, props);
        }
    }
}


}
