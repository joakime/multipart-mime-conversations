package org.eclipse.jetty.demo;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.demo.apachecli.MultipartCreator;

public class ApacheHttpCompSamples
{
    private static final URI serverURI = URI.create("http://localhost:9090/");

    public static void main(String[] args) throws Exception
    {
        try (CloseableHttpClient httpclient = HttpClients.createDefault())
        {
            performPost(httpclient, "/capture/text-files", MultipartCreator.createTextFiles());
            performPost(httpclient, "/capture/nested", MultipartCreator.createNested());
            performPost(httpclient, "/capture/nested-binary", MultipartCreator.createNestedBinary());
            performPost(httpclient, "/capture/base64", MultipartCreator.createBase64());
            performPost(httpclient, "/capture/base64-long", MultipartCreator.createBase64Long());
            performPost(httpclient, "/capture/number-only", MultipartCreator.createNumberOnly());
            performPost(httpclient, "/capture/number-only2", MultipartCreator.createNumberOnly2());
            performPost(httpclient, "/capture/unicode-names", MultipartCreator.createUnicodeNames());
            performPost(httpclient, "/capture/strange-quoting", MultipartCreator.createStrangeQuoting());
            performPost(httpclient, "/capture/zalgo-text-plain", MultipartCreator.createZalgoTextPlain());
            performPost(httpclient, "/capture/encoding-mess", MultipartCreator.createEncodingMess());
            performPost(httpclient, "/capture/duplicate-names", MultipartCreator.createDuplicateNames());
            performPost(httpclient, "/capture/company-urlencoded", MultipartCreator.createCompany());
            performPost(httpclient, "/capture/sjis", MultipartCreator.createSjis());
            // TODO: performPost(httpclient, "/capture/uppercase", MultipartCreator.createUppercase());
            performPost(httpclient, "/capture/complex", MultipartCreator.createComplex());
        }
    }

    public static void performPost(CloseableHttpClient httpclient, String path, HttpEntity entity)
    {
        HttpPost httppost = new HttpPost(serverURI.resolve(path));
        httppost.setHeader("X-BrowserId", "apache-httpcomp");
        httppost.setEntity(entity);

        System.out.println(httppost);
        try(CloseableHttpResponse response = httpclient.execute(httppost))
        {
            System.out.println(response.getStatusLine());
            HttpEntity resEntity = response.getEntity();
            EntityUtils.consume(resEntity);
        }
        catch (ClientProtocolException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
