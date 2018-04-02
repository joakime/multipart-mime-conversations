package org.eclipse.jetty.demo;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.resource.PathResource;

public class FormCaptureServer
{
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(9090);

        HandlerList handlers = new HandlerList();

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.setBaseResource(new PathResource(MavenTestingUtils.getProjectDirPath("src/main/resources/base-browser-capture")));

        context.addServlet(FormCaptureServlet.class, "/capture/*");

        ServletHolder defHolder = new ServletHolder("default", DefaultServlet.class);
        context.addServlet(defHolder, "/");

        handlers.addHandler(context);
        handlers.addHandler(new DefaultHandler());

        server.setHandler(handlers);
        server.start();
        server.join();
    }

    @SuppressWarnings("Duplicates")
    public static class FormCaptureServlet extends HttpServlet
    {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            String name = req.getPathInfo();
            while (name.startsWith("/"))
                name = name.substring(1);

            Path outputDir = MavenTestingUtils.getTestResourcesPath();

            String browserId = toBrowserId(req);
            String prefix = "browser-capture-" + name + "-" + browserId;
            Path outputRaw = outputDir.resolve(prefix + ".raw");
            try (InputStream in = req.getInputStream();
                 OutputStream out = Files.newOutputStream(outputRaw))
            {
                IO.copy(in, out);
            }

            System.err.println("Saved " + outputRaw);

            Path outputTxt = outputDir.resolve(prefix + ".expected.txt");
            try (BufferedWriter writer = Files.newBufferedWriter(outputTxt);
                 PrintWriter out = new PrintWriter(writer))
            {
                Collections.list(req.getHeaderNames())
                        .stream()
                        .sorted()
                        .forEach((headerName) ->
                                out.printf("Request-Header|%s|%s%n", headerName, req.getHeader(headerName))
                        );
            }

            resp.setContentType("text/html");
            req.getRequestDispatcher("/thanks.html").forward(req, resp);
        }

        private String toBrowserId(HttpServletRequest request)
        {
            // A client provided ID
            String id = request.getHeader("X-BrowserId");
            if (id != null)
            {
                return id;
            }

            // A browser?
            String ua = request.getHeader("User-Agent");

            if (ua.contains("Edge/"))
            {
                return "edge";
            }

            if (ua.contains("Safari/"))
            {
                if (ua.contains("Chrome/"))
                {
                    if (ua.contains("Mobile"))
                    {
                        if (ua.contains("Android"))
                            return "android-chrome";
                        return "mobile-chrome";
                    }
                    return "chrome";
                }

                if (ua.contains("Mobile"))
                    return "ios-safari";
                return "safari";
            }

            if (ua.contains("Trident/"))
            {
                return "msie";
            }

            if (ua.contains("Firefox/"))
            {
                if (ua.contains("Mobile"))
                {
                    if (ua.contains("Android"))
                        return "android-firefox";
                    return "mobile-firefox";
                }
                return "firefox";
            }

            if (ua.contains("Chrome/"))
            {
                if (ua.contains("Mobile"))
                {
                    if (ua.contains("Android"))
                        return "android-chrome";
                    return "mobile-chrome";
                }
                return "chrome";
            }

            StringBuilder safe = new StringBuilder();
            for (char c : ua.toCharArray())
            {
                if (Character.isDigit(c) || Character.isLetter(c) || c == '.')
                    safe.append(c);
                else
                    safe.append('_');
            }
            return safe.toString();
        }
    }
}
