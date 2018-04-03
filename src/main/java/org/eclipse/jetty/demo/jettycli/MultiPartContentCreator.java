package org.eclipse.jetty.demo.jettycli;

import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.eclipse.jetty.demo.Util.newSpecBoundaryString;
import static org.eclipse.jetty.demo.Util.pngPath;
import static org.eclipse.jetty.demo.Util.sha1sum;
import static org.eclipse.jetty.demo.Util.urlEncode;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;

import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.client.util.MultiPartContentProvider;
import org.eclipse.jetty.client.util.PathContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.demo.Sjis;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.toolchain.test.MavenTestingUtils;

public class MultiPartContentCreator
{
    public static MultiPartContentProvider createTextFiles() throws IOException
    {
        MultiPartContentProvider content = new MultiPartContentProvider();

        String text = "text default\r\n";
        byte[] aTextFile = "Content of a.txt\n".getBytes(UTF_8);
        byte[] aHtmlFile = "<!DOCTYPE html><title>Content of a.html.</title>\r".getBytes(UTF_8);

        content.addFieldPart("text", new StringContentProvider(text), null);

        HttpFields aTextFileFields = new HttpFields();
        aTextFileFields.add("X-SHA1", sha1sum(aTextFile));
        content.addFilePart("file1", "a.txt", new BytesContentProvider(aTextFile), aTextFileFields);

        HttpFields aHtmlFileFields = new HttpFields();
        aHtmlFileFields.add("X-SHA1", sha1sum(aHtmlFile));
        content.addFilePart("file2", "a.html", new BytesContentProvider(aHtmlFile), aHtmlFileFields);

        return content;
    }

    public static MultiPartContentProvider createNested() throws IOException
    {
        MultiPartContentProvider contentInner = new MultiPartContentProvider();

        // Inner MultiPart

        String exampleBoundary = newSpecBoundaryString();

        contentInner.addFieldPart("fruit", new StringContentProvider("banana"), null);
        contentInner.addFieldPart("color", new StringContentProvider("yellow"), null);
        contentInner.addFieldPart("cost", new StringContentProvider("$0.12 USD"), null);
        contentInner.addFieldPart("comments", new StringContentProvider("--" + exampleBoundary), null);

        // Outer MultiPart

        MultiPartContentProvider contentOuter = new MultiPartContentProvider();

        contentOuter.addFieldPart("reporter", new StringContentProvider("<user@company.com>"), null);
        contentOuter.addFieldPart("timestamp", new StringContentProvider("2018-03-21T18:52:18+00:00"), null);
        contentOuter.addFieldPart("comments", new StringContentProvider("this couldn't be parsed"), null);
        contentOuter.addFilePart("attachment", "sample", contentInner, null);

        return contentOuter;
    }

    public static MultiPartContentProvider createNumberOnly() throws IOException
    {
        MultiPartContentProvider content = new MultiPartContentProvider();

        Path pi10k = MavenTestingUtils.getProjectFilePath("src/main/resources/pi-10k.txt");
        PathContentProvider piContent = new PathContentProvider(pi10k);

        content.addFieldPart("pi", piContent, null);

        return content;
    }

    public static MultiPartContentProvider createWhiteSpaceOnly() throws IOException
    {
        MultiPartContentProvider content = new MultiPartContentProvider();

        byte buf[] = new byte[1024 * 1024];

        final byte CHARS[] = new byte[]{'\r', '\n', '\t', '\f', ' '};
        for (int i = 0; i < buf.length; i++)
        {
            buf[i] = CHARS[ThreadLocalRandom.current().nextInt(CHARS.length)];
        }

        ContentProvider whitespaceProvider = new BytesContentProvider(buf);

        HttpFields fields = new HttpFields();
        fields.add("X-SHA1", sha1sum(buf));

        content.addFilePart("whitespace", "whitespace.txt", whitespaceProvider, fields);

        return content;
    }

    public static MultiPartContentProvider createUnicodeNames() throws IOException
    {
        MultiPartContentProvider content = new MultiPartContentProvider();

        content.addFieldPart("こんにちは世界",
                new StringContentProvider("text/plain", "Greetings 1", UTF_8),
                null);
        content.addFieldPart(urlEncode("こんにちは世界", UTF_8),
                new StringContentProvider("text/plain", "Greetings 2", UTF_8),
                null);

        return content;
    }

    public static MultiPartContentProvider createEncodingMess() throws IOException
    {
        MultiPartContentProvider content = new MultiPartContentProvider();

        String persian = "برج بابل";

        int count = 0;
        for (Charset charset : Charset.availableCharsets().values())
        {
            try
            {
                String name = "persian-" + urlEncode(charset.name(), UTF_8);
                StringContentProvider persianContent = new StringContentProvider("text/plain", persian, charset);
                content.addFieldPart(name, persianContent, null);
                count++;
            }
            catch (UnsupportedOperationException e)
            {
                // skip
            }
        }

        content.addFieldPart("count", new StringContentProvider(Integer.toString(count)), null);

        return content;
    }

    public static MultiPartContentProvider createDuplicateNames() throws IOException
    {
        MultiPartContentProvider content = new MultiPartContentProvider();

        content.addFieldPart("pi", new StringContentProvider("3.14159265358979323846264338327950288419716939937510"), null);
        content.addFieldPart("pi", new StringContentProvider("3.14159"), null);
        content.addFieldPart("pi", new StringContentProvider("3"), null);
        content.addFieldPart("pi", new StringContentProvider("π"), null);
        content.addFieldPart("pi", new StringContentProvider("π"), null);
        content.addFieldPart("pi", new StringContentProvider(urlEncode("π", UTF_8)), null);
        content.addFieldPart("pi", new StringContentProvider("π = C/d"), null);
        content.addFieldPart("π", new StringContentProvider("3.14"), null);
        content.addFieldPart(urlEncode("π", UTF_8), new StringContentProvider("Approximately 3.14"), null);
        content.addFieldPart(urlEncode("π", UTF_16), new StringContentProvider("Approximately 3.14"), null);

        return content;
    }

    public static MultiPartContentProvider createSjis() throws IOException
    {
        final String SJIS = "Shift-JIS";
        final Charset SJIS_CHARSET = Charset.forName(SJIS);

        MultiPartContentProvider content = new MultiPartContentProvider();

        HttpFields sjisFields = new HttpFields();
        sjisFields.add(HttpHeader.CONTENT_TYPE, "text/plain; charset=" + SJIS);

        byte sjisA[] = Sjis.OPEN_SOURCE.getBytes(SJIS_CHARSET);
        byte sjisB[] = Sjis.ECLIPSE_JETTY.getBytes(SJIS_CHARSET);

        content.addFieldPart("japanese", new BytesContentProvider("text/plain", sjisA), sjisFields);
        content.addFieldPart("hello", new BytesContentProvider("text/plain", sjisB), sjisFields);

        return content;
    }

    public static MultiPartContentProvider createComplex() throws IOException
    {
        MultiPartContentProvider content = new MultiPartContentProvider();

        content.addFieldPart("pi", new StringContentProvider("3.14159265358979323846264338327950288419716939937510"), null);
        HttpFields wwwFormUrlEncodedFields = new HttpFields();
        wwwFormUrlEncodedFields.add(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
        content.addFieldPart("company", new StringContentProvider("bob & frank's shoe repair"), wwwFormUrlEncodedFields);

        content.addFilePart("upload_file", "filename", new PathContentProvider(pngPath), null);
        content.addFieldPart("power", new StringContentProvider("\uAB35о\uD835\uDDCBⲥ\uD835\uDDBE"), null);

        final String SJIS = "Shift-JIS";
        final Charset SJIS_CHARSET = Charset.forName(SJIS);
        HttpFields sjisFields = new HttpFields();
        sjisFields.add(HttpHeader.CONTENT_TYPE, "text/plain; charset=" + SJIS);

        content.addFieldPart("japanese", new StringContentProvider("text/plain", Sjis.OPEN_SOURCE, SJIS_CHARSET), sjisFields);
        content.addFieldPart("hello", new StringContentProvider("text/plain", Sjis.ECLIPSE_JETTY, SJIS_CHARSET), sjisFields);

        return content;
    }
}
