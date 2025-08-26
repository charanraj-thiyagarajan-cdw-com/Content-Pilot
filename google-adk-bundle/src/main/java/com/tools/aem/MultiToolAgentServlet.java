package com.tools.aem;

import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;

import com.google.genai.types.Content;
import com.google.genai.types.Part;

import io.reactivex.rxjava3.core.Flowable;

import org.osgi.service.component.annotations.Component;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Simple servlet to test Google ADK Agent in AEM.
 */

@Component(service = { Servlet.class })
@SlingServletPaths("/bin/testADKAgent")
public class MultiToolAgentServlet extends SlingSafeMethodsServlet {

    @Override
    protected void doGet(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {
        try {
            LlmAgent agent = LlmAgent.builder()
                    .name("aem-test-agent")
                    .description("Simple test agent in AEM")
                    .model("gemini-2.0-flash")
                    .instruction("You are a helpful assistant.")
                    .build();

            InMemoryRunner runner = new InMemoryRunner(agent);
            Session session = runner
                    .sessionService()
                    .createSession(runner.appName(), "test-user")
                    .blockingGet();

            Content userMsg = Content.fromParts(Part.fromText("What can you do?"));
            Flowable<Event> events = runner.runAsync("test-user", session.id(), userMsg);

            events.blockingForEach(event -> System.out.println(event.stringifyContent()));

        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("Error: " + e.getMessage());
        }
    }
}
