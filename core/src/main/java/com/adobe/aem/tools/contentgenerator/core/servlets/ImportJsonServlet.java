package com.adobe.aem.tools.contentgenerator.core.servlets;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.PageManagerFactory;
import com.day.cq.wcm.api.WCMException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

@Component(service = Servlet.class, property = {
        "sling.servlet.paths=" + "/bin/importJson",
        "sling.servlet.methods=POST"
})

public class ImportJsonServlet extends SlingAllMethodsServlet {

    @Reference
    private PageManagerFactory pageManagerFactory;

    private static final Logger log = LoggerFactory.getLogger(ImportJsonServlet.class);

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        ResourceResolver resolver = request.getResourceResolver();
        ObjectMapper mapper = new ObjectMapper();

        try {
            // Read JSON string from form field
            String inputJson = request.getParameter("identifiedJSON");
            if (inputJson == null || inputJson.isEmpty()) {
                response.getWriter().write("No JSON data provided.");
                return;
            }
            log.info("Received JSON from form: {}", inputJson);

            JsonNode inputNode = mapper.readTree(inputJson);

            // wrapper JSON
            ObjectNode rootNode = mapper.createObjectNode();
            rootNode.put("pageName", "imported-button-page");
            rootNode.put("title", "Imported Page with Button");
            rootNode.put("template", "/conf/contentgenerator/settings/wcm/templates/page-content");
            rootNode.put("parentPath", "/content/contentgenerator/us");

            ArrayNode componentsArray = mapper.createArrayNode();

            if (!inputNode.isArray()) {
                response.getWriter().write("Input JSON must be an array of button objects.");
                return;
            }
            int idx = 0;
            for (JsonNode buttonNode : inputNode) {
                ObjectNode componentNode = mapper.createObjectNode();
                String buttonId = buttonNode.has("id") ? buttonNode.get("id").asText() : "button-" + idx;
                componentNode.put("path", "root/container/container/" + buttonId);

                ObjectNode props = mapper.createObjectNode();
                buttonNode.fields().forEachRemaining(entry -> {
                    props.put(entry.getKey(), entry.getValue().asText());
                });
                props.put("jcr:primaryType", "nt:unstructured");
                componentNode.set("properties", props);
                componentsArray.add(componentNode);
                idx++;
            }

            rootNode.set("components", componentsArray);
            log.info("Printing rootnode... " + rootNode);
            log.info("JSON for page creation: {}",
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode));

            // Creating page properties
            String parentPath = rootNode.path("parentPath").asText("/content/contentgenerator/us");
            String pageName = rootNode.path("pageName").asText("imported-page");
            String template = rootNode.path("template")
                    .asText("/conf/contentgenerator/settings/wcm/templates/page-content");
            String title = rootNode.path("title").asText("Imported Page");

            PageManager pageManager = pageManagerFactory.getPageManager(resolver);
            Page page = pageManager.create(parentPath, pageName, template, title);

            if (page == null) {
                response.getWriter().write("Page creation failed.");
                return;
            }

            // Create components
            JsonNode components = rootNode.path("components");
            if (components.isArray()) {
                for (JsonNode comp : components) {
                    String compPath = comp.path("path").asText();
                    Resource parentRes = resolver.getResource(page.getPath() + "/jcr:content/" + compPath);
                    if (parentRes == null) {
                        parentRes = createIntermediateNodes(resolver, page.getPath() + "/jcr:content/" + compPath);
                    }
                    if (parentRes != null) {
                        ModifiableValueMap mvm = parentRes.adaptTo(ModifiableValueMap.class);
                        for (Iterator<Map.Entry<String, JsonNode>> it = comp.path("properties").fields(); it
                                .hasNext();) {
                            Map.Entry<String, JsonNode> field = it.next();
                            if (field.getValue().isArray()) {
                                mvm.put(field.getKey(), mapper.convertValue(field.getValue(), String[].class));
                            } else {
                                mvm.put(field.getKey(), field.getValue().asText());
                            }
                        }
                    }
                }
            }

            resolver.commit();
            response.getWriter().write(" Page and components created successfully at: " + page.getPath());

        } catch (PersistenceException | RepositoryException | WCMException e) {
            log.error("Error creating page/components", e);
            response.getWriter().write("Error: " + e.getMessage());
        }
    }

    private Resource createIntermediateNodes(ResourceResolver resolver, String absPath) throws RepositoryException {
        String[] parts = absPath.split("/");
        StringBuilder currentPath = new StringBuilder();
        Resource currentRes = null;

        for (String part : parts) {
            if (part.isEmpty())
                continue;
            currentPath.append("/").append(part);
            Resource res = resolver.getResource(currentPath.toString());
            if (res == null) {
                Resource parentRes = resolver.getResource(currentPath.substring(0, currentPath.lastIndexOf("/")));
                if (parentRes != null) {
                    try {
                        res = resolver.create(parentRes, part, Map.of("jcr:primaryType", "nt:unstructured"));
                    } catch (PersistenceException e) {
                        throw new RepositoryException("Failed creating node: " + part, e);
                    }
                }
            }
            currentRes = res;
        }
        return currentRes;
    }
}
