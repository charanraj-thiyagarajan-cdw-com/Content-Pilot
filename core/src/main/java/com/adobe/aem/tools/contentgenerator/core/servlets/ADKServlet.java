package com.adobe.aem.tools.contentgenerator.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.osgi.service.component.annotations.Component;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component(service = { Servlet.class })
@SlingServletPaths("/bin/adk")
public class ADKServlet extends SlingAllMethodsServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {
        String htmlElements = "";
        try {
            Document doc = Jsoup.connect(req.getParameter("externalURL")).get();
            Elements elements = doc.select("div,h1");
            StringBuilder inputBuilder = new StringBuilder();
            for (Element el : elements) {
                inputBuilder.append(el.outerHtml()).append("\n");
            }
            htmlElements = inputBuilder.toString();
            System.out.println(htmlElements);

            HttpClient client = HttpClient.newHttpClient();

            // Step 1: Create session
            HttpRequest createSessionRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/apps/aem_agent/users/charan/sessions/adk_session"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            client.send(createSessionRequest, HttpResponse.BodyHandlers.discarding());

            // Step 2: Send htmlElements to /run
            JsonObject root = new JsonObject();
            root.addProperty("appName", "aem_agent");
            root.addProperty("userId", "charan");
            root.addProperty("sessionId", "adk_session");
            JsonObject newMessage = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", htmlElements);
            parts.add(part);
            newMessage.add("parts", parts);
            newMessage.addProperty("role", "user");
            root.add("newMessage", newMessage);
            root.addProperty("streaming", false);
            String jsonBody = root.toString();
            HttpRequest runRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/run"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            HttpResponse<String> runResponse = client.send(runRequest, HttpResponse.BodyHandlers.ofString());

            // Step 3: Delete session
            HttpRequest deleteSessionRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/apps/aem_agent/users/charan/sessions/adk_session"))
                    .DELETE()
                    .build();
            client.send(deleteSessionRequest, HttpResponse.BodyHandlers.discarding());

            // Parse the response and extract the last block's content.parts[0].text
            String responseBody = runResponse.body();
            JsonArray arr = JsonParser.parseString(responseBody).getAsJsonArray();
            String finalJson = null;
            JsonObject lastBlock = arr.get(arr.size() - 1).getAsJsonObject();
            JsonObject content = lastBlock.getAsJsonObject("content");
            JsonArray partsArr = content.getAsJsonArray("parts");
            JsonElement partElem = partsArr.get(0);
            finalJson = partElem.getAsJsonObject().get("text").getAsString();
            if (finalJson != null) {
                String jsonToSend = finalJson;
                if (jsonToSend.startsWith("```json")) {
                    int startIdx = jsonToSend.indexOf('[');
                    int endIdx = jsonToSend.lastIndexOf(']');
                    if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                        jsonToSend = jsonToSend.substring(startIdx, endIdx + 1);
                    }
                }
                resp.setContentType("application/json");
                resp.getWriter().write(jsonToSend);
            } else {
                resp.getWriter().write(responseBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
