package org.eclipse.jetty.demo;

import java.net.URI;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.MultiPartContentProvider;
import org.eclipse.jetty.demo.jettycli.MultiPartContentCreator;
import org.eclipse.jetty.http.HttpMethod;

public class JettyClientSamples
{
    private static final URI serverURI = URI.create("http://localhost:9090/");

    public static void main(String[] args) throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        try
        {
            performPost(httpClient, "/capture/text-files", MultiPartContentCreator.createTextFiles());
            performPost(httpClient, "/capture/nested", MultiPartContentCreator.createNested());
            performPost(httpClient, "/capture/number-only", MultiPartContentCreator.createNumberOnly());
            performPost(httpClient, "/capture/whitespace-only", MultiPartContentCreator.createWhiteSpaceOnly());
            performPost(httpClient, "/capture/unicode-names", MultiPartContentCreator.createUnicodeNames());
            performPost(httpClient, "/capture/encoding-mess", MultiPartContentCreator.createEncodingMess());
            performPost(httpClient, "/capture/duplicate-names", MultiPartContentCreator.createDuplicateNames());
            performPost(httpClient, "/capture/sjis", MultiPartContentCreator.createSjis());
            performPost(httpClient, "/capture/complex", MultiPartContentCreator.createComplex());
        }
        finally
        {
            httpClient.stop();
        }
    }

    private static void performPost(HttpClient httpClient, String path, MultiPartContentProvider content)
    {
        try
        {
            Request httpPost = httpClient.newRequest(serverURI.resolve(path));
            httpPost.header("X-BrowserId", "jetty-client");
            httpPost.header("Connection", "close");
            httpPost.method(HttpMethod.POST);
            httpPost.content(content);
            System.out.println(httpPost);
            ContentResponse response = httpPost.send();
            System.out.println(response);
            response.getContentAsString(); // read the entire response
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }
}
